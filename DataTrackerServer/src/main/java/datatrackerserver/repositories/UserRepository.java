package datatrackerserver.repositories;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import datatrackerserver.entities.User;

@Component
@EnableAutoConfiguration
@Repository
public interface UserRepository extends JpaRepository<User, String> {
	User findByEmail(String email);
}
