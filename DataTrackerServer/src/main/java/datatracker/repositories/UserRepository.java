package datatracker.repositories;

import java.util.Optional;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import datatracker.entities.User;

@Component
@EnableAutoConfiguration
@Repository
public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
}
