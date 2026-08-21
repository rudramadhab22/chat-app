package com.rudra.ed.repository;

import com.rudra.ed.domain.entity.MessageReceipt;
import com.rudra.ed.domain.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, Long> {

	Optional<MessageReceipt> findByMessageIdAndUserId(Long messageId, Long userId);

	List<MessageReceipt> findByMessageId(Long messageId);

	@Query("""
			SELECT r FROM MessageReceipt r
			JOIN FETCH r.user
			WHERE r.message.id = :messageId
			""")
	List<MessageReceipt> findWithUserByMessageId(@Param("messageId") Long messageId);

	@Query("""
			SELECT r FROM MessageReceipt r
			JOIN FETCH r.message m
			JOIN FETCH m.sender
			WHERE r.message.id = :messageId
			""")
	List<MessageReceipt> findDetailedByMessageId(@Param("messageId") Long messageId);

	long countByMessageIdAndStatus(Long messageId, ReceiptStatus status);
}
