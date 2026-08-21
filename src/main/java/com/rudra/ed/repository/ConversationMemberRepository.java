package com.rudra.ed.repository;

import com.rudra.ed.domain.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

	@Query("""
			SELECT m FROM ConversationMember m
			JOIN FETCH m.user
			WHERE m.conversation.id = :conversationId
			""")
	List<ConversationMember> findByConversationId(@Param("conversationId") Long conversationId);

	@Query("""
			SELECT m FROM ConversationMember m
			JOIN FETCH m.user
			WHERE m.conversation.id = :conversationId AND m.user.id = :userId
			""")
	Optional<ConversationMember> findByConversationIdAndUserId(
			@Param("conversationId") Long conversationId,
			@Param("userId") Long userId
	);

	boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

	@Query("""
			SELECT DISTINCT m2.user.id FROM ConversationMember m1
			JOIN ConversationMember m2 ON m2.conversation = m1.conversation
			WHERE m1.user.id = :userId AND m2.user.id <> :userId
			""")
	Set<Long> findContactUserIds(@Param("userId") Long userId);

	@Modifying
	@Query("DELETE FROM ConversationMember m WHERE m.conversation.id = :conversationId AND m.user.id = :userId")
	void deleteByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
