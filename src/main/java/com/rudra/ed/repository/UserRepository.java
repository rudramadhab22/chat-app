package com.rudra.ed.repository;

import com.rudra.ed.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	@Query("""
			SELECT u FROM User u
			WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
			   OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
			   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
			ORDER BY u.displayName ASC
			""")
	List<User> search(@Param("query") String query);
}
