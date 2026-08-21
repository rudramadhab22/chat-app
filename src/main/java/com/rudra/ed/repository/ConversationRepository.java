package com.rudra.ed.repository;

import com.rudra.ed.domain.entity.Conversation;
import com.rudra.ed.domain.enums.ConversationType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	@EntityGraph(attributePaths = "createdBy")
	@Query("""
			SELECT c FROM Conversation c
			JOIN ConversationMember m ON m.conversation = c
			WHERE m.user.id = :userId
			ORDER BY c.updatedAt DESC
			""")
	List<Conversation> findAllForUser(@Param("userId") Long userId);

	@Query("""
			SELECT c FROM Conversation c
			JOIN FETCH c.createdBy
			WHERE c.id = :id
			""")
	Optional<Conversation> findByIdWithCreator(@Param("id") Long id);

	@EntityGraph(attributePaths = "createdBy")
	@Query("""
			SELECT c FROM Conversation c
			WHERE c.type = :type
			  AND c.id IN (
			      SELECT m1.conversation.id FROM ConversationMember m1
			      WHERE m1.user.id = :userId1
			  )
			  AND c.id IN (
			      SELECT m2.conversation.id FROM ConversationMember m2
			      WHERE m2.user.id = :userId2
			  )
			  AND (
			      SELECT COUNT(m3) FROM ConversationMember m3
			      WHERE m3.conversation = c
			  ) = 2
			""")
	Optional<Conversation> findDirectBetween(
			@Param("userId1") Long userId1,
			@Param("userId2") Long userId2,
			@Param("type") ConversationType type
	);
}
