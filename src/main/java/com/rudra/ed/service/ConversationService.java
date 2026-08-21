package com.rudra.ed.service;

import com.rudra.ed.domain.entity.Conversation;
import com.rudra.ed.domain.entity.ConversationMember;
import com.rudra.ed.domain.entity.Message;
import com.rudra.ed.domain.entity.User;
import com.rudra.ed.domain.enums.ConversationType;
import com.rudra.ed.domain.enums.MemberRole;
import com.rudra.ed.dto.request.AddMembersRequest;
import com.rudra.ed.dto.request.CreateDirectConversationRequest;
import com.rudra.ed.dto.request.CreateGroupConversationRequest;
import com.rudra.ed.dto.response.ConversationResponse;
import com.rudra.ed.dto.response.MessageResponse;
import com.rudra.ed.exception.BadRequestException;
import com.rudra.ed.exception.ForbiddenException;
import com.rudra.ed.exception.ResourceNotFoundException;
import com.rudra.ed.repository.ConversationMemberRepository;
import com.rudra.ed.repository.ConversationRepository;
import com.rudra.ed.repository.MessageRepository;
import com.rudra.ed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConversationService {

	private final ConversationRepository conversationRepository;
	private final ConversationMemberRepository memberRepository;
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final UserService userService;

	@Transactional
	public ConversationResponse createDirect(Long currentUserId, CreateDirectConversationRequest request) {
		if (currentUserId.equals(request.getOtherUserId())) {
			throw new BadRequestException("Cannot create a direct chat with yourself");
		}
		User current = userService.requireUser(currentUserId);
		User other = userService.requireUser(request.getOtherUserId());

		return conversationRepository
				.findDirectBetween(currentUserId, other.getId(), ConversationType.DIRECT)
				.map(existing -> toResponse(existing, currentUserId))
				.orElseGet(() -> {
					Conversation conversation = Conversation.builder()
							.type(ConversationType.DIRECT)
							.createdBy(current)
							.build();
					Conversation saved = conversationRepository.save(conversation);
					memberRepository.save(ConversationMember.builder()
							.conversation(saved)
							.user(current)
							.role(MemberRole.MEMBER)
							.build());
					memberRepository.save(ConversationMember.builder()
							.conversation(saved)
							.user(other)
							.role(MemberRole.MEMBER)
							.build());
					return toResponse(saved, currentUserId);
				});
	}

	@Transactional
	public ConversationResponse createGroup(Long currentUserId, CreateGroupConversationRequest request) {
		User current = userService.requireUser(currentUserId);
		Set<Long> memberIds = new HashSet<>(request.getMemberIds());
		memberIds.remove(currentUserId);
		if (memberIds.isEmpty()) {
			throw new BadRequestException("Group must include at least one other member");
		}

		List<User> members = userRepository.findAllById(memberIds);
		if (members.size() != memberIds.size()) {
			throw new ResourceNotFoundException("One or more members were not found");
		}

		Conversation conversation = Conversation.builder()
				.type(ConversationType.GROUP)
				.name(request.getName().trim())
				.createdBy(current)
				.build();
		Conversation saved = conversationRepository.save(conversation);

		List<ConversationMember> memberships = new ArrayList<>();
		memberships.add(ConversationMember.builder()
				.conversation(saved)
				.user(current)
				.role(MemberRole.ADMIN)
				.build());
		for (User member : members) {
			memberships.add(ConversationMember.builder()
					.conversation(saved)
					.user(member)
					.role(MemberRole.MEMBER)
					.build());
		}
		memberRepository.saveAll(memberships);
		return toResponse(saved, currentUserId);
	}

	@Transactional(readOnly = true)
	public List<ConversationResponse> listForUser(Long userId) {
		return conversationRepository.findAllForUser(userId).stream()
				.map(c -> toResponse(c, userId))
				.toList();
	}

	@Transactional(readOnly = true)
	public ConversationResponse getById(Long conversationId, Long userId) {
		requireMembership(conversationId, userId);
		Conversation conversation = requireConversation(conversationId);
		return toResponse(conversation, userId);
	}

	@Transactional
	public ConversationResponse addMembers(Long conversationId, Long actorId, AddMembersRequest request) {
		Conversation conversation = requireConversation(conversationId);
		if (conversation.getType() != ConversationType.GROUP) {
			throw new BadRequestException("Members can only be added to group conversations");
		}
		ConversationMember actor = requireMembership(conversationId, actorId);
		if (actor.getRole() != MemberRole.ADMIN) {
			throw new ForbiddenException("Only group admins can add members");
		}

		for (Long memberId : request.getMemberIds()) {
			if (memberRepository.existsByConversationIdAndUserId(conversationId, memberId)) {
				continue;
			}
			User user = userService.requireUser(memberId);
			memberRepository.save(ConversationMember.builder()
					.conversation(conversation)
					.user(user)
					.role(MemberRole.MEMBER)
					.build());
		}
		conversationRepository.save(conversation);
		return toResponse(conversation, actorId);
	}

	@Transactional
	public void removeMember(Long conversationId, Long actorId, Long targetUserId) {
		Conversation conversation = requireConversation(conversationId);
		if (conversation.getType() != ConversationType.GROUP) {
			throw new BadRequestException("Members can only be removed from group conversations");
		}
		ConversationMember actor = requireMembership(conversationId, actorId);
		boolean selfLeave = actorId.equals(targetUserId);
		if (!selfLeave && actor.getRole() != MemberRole.ADMIN) {
			throw new ForbiddenException("Only group admins can remove other members");
		}
		if (!memberRepository.existsByConversationIdAndUserId(conversationId, targetUserId)) {
			throw new ResourceNotFoundException("Member not found in conversation");
		}
		memberRepository.deleteByConversationIdAndUserId(conversationId, targetUserId);
	}

	@Transactional(readOnly = true)
	public Conversation requireConversation(Long id) {
		return conversationRepository.findByIdWithCreator(id)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
	}

	@Transactional(readOnly = true)
	public ConversationMember requireMembership(Long conversationId, Long userId) {
		return memberRepository.findByConversationIdAndUserId(conversationId, userId)
				.orElseThrow(() -> new ForbiddenException("You are not a member of this conversation"));
	}

	@Transactional(readOnly = true)
	public List<Long> memberUserIds(Long conversationId) {
		return memberRepository.findByConversationId(conversationId).stream()
				.map(m -> m.getUser().getId())
				.toList();
	}

	private ConversationResponse toResponse(Conversation conversation, Long currentUserId) {
		List<ConversationMember> members = memberRepository.findByConversationId(conversation.getId());
		List<ConversationResponse.MemberResponse> memberResponses = members.stream()
				.map(m -> ConversationResponse.MemberResponse.builder()
						.userId(m.getUser().getId())
						.username(m.getUser().getUsername())
						.displayName(m.getUser().getDisplayName())
						.profilePictureUrl(m.getUser().getProfilePictureUrl())
						.role(m.getRole())
						.online(m.getUser().isOnline())
						.lastSeenAt(m.getUser().getLastSeenAt())
						.build())
				.toList();

		String displayName = conversation.getName();
		String avatarUrl = conversation.getAvatarUrl();
		if (conversation.getType() == ConversationType.DIRECT) {
			User other = members.stream()
					.map(ConversationMember::getUser)
					.filter(u -> !u.getId().equals(currentUserId))
					.findFirst()
					.orElse(null);
			if (other != null) {
				displayName = other.getDisplayName();
				avatarUrl = other.getProfilePictureUrl();
			}
		}

		MessageResponse lastMessage = messageRepository
				.findHistory(conversation.getId(), null, PageRequest.of(0, 1))
				.stream()
				.findFirst()
				.map(this::toSimpleMessage)
				.orElse(null);

		return ConversationResponse.builder()
				.id(conversation.getId())
				.type(conversation.getType())
				.name(displayName)
				.avatarUrl(avatarUrl)
				.createdById(conversation.getCreatedBy().getId())
				.createdAt(conversation.getCreatedAt())
				.updatedAt(conversation.getUpdatedAt())
				.lastMessage(lastMessage)
				.members(memberResponses)
				.build();
	}

	private MessageResponse toSimpleMessage(Message message) {
		return MessageResponse.builder()
				.id(message.getId())
				.conversationId(message.getConversation().getId())
				.senderId(message.getSender().getId())
				.senderUsername(message.getSender().getUsername())
				.senderDisplayName(message.getSender().getDisplayName())
				.content(message.getContent())
				.type(message.getType())
				.clientMessageId(message.getClientMessageId())
				.createdAt(message.getCreatedAt())
				.build();
	}
}
