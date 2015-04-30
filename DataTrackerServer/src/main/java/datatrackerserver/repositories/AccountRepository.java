package datatrackerserver.repositories;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import datatrackerserver.entities.Account;

@Component
@EnableAutoConfiguration
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
	Account findByEmail(String email);
}
