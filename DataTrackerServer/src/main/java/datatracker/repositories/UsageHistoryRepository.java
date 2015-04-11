package datatracker.repositories;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import datatracker.entities.UsageHistory;
import datatracker.entities.UsageHistoryId;

@Component
@EnableAutoConfiguration
@Repository
public interface UsageHistoryRepository extends JpaRepository<UsageHistory, UsageHistoryId> {

}
