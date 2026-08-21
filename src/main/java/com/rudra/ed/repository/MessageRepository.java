package com.rudra.ed.repository;

import com.rudra.ed.domain.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

	@Query("""
			SELECT m FROM Message m
			JOIN FETCH m.sender
			JOIN FETCH m.conversation
			WHERE m.conversation.id = :conversationId
			  AND m.deleted = false
			  AND (:beforeId IS NULL OR m.id < :beforeId)
			ORDER BY m.id DESC
			""")
	List<Message> findHistory(
			@Param("conversationId") Long conversationId,
			@Param("beforeId") Long beforeId,
			Pageable pageable
	);

	@Query("""
			SELECT m FROM Message m
			JOIN FETCH m.sender
			JOIN FETCH m.conversation
			WHERE m.id = :id
			""")
	java.util.Optional<Message> findByIdWithSender(@Param("id") Long id);
}
