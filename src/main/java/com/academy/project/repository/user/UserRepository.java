package com.academy.project.repository.user;

import com.academy.project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    /**
     * Login can happen via email or phone in the same field ("identifier").
     * Excludes soft-deleted accounts.
     */
    default Optional<User> findActiveByIdentifier(String identifier) {
        Optional<User> byEmail = findByEmailIgnoreCase(identifier);
        if (byEmail.isPresent()) {
            return byEmail.filter(u -> u.getDeletedAt() == null);
        }
        return findByPhone(identifier).filter(u -> u.getDeletedAt() == null);
    }
}