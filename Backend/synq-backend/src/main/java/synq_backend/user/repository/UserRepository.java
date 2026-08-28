package synq_backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import synq_backend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

// Provides database CRUD operations for User.
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

}
