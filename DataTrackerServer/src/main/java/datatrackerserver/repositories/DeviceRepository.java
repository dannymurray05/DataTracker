package datatrackerserver.repositories;

import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import datatrackerserver.entities.Device;
import datatrackerserver.entities.User;

@Component
@EnableAutoConfiguration
@Repository
public interface DeviceRepository extends JpaRepository<Device, String>  {
	List<Device> findByUser(User user);
}
