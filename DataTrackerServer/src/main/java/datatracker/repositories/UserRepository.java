package datatracker.repositories;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import datatracker.entities.User;

@Component
@EnableAutoConfiguration
@Repository
public interface UserRepository extends CrudRepository<User, String> {

}
