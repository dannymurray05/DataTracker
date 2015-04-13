package datatracker.repositories;

import java.util.Date;
import java.util.List;

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
	List<UsageHistory> findByPhoneNumber(String phoneNumber);
	
	List<UsageHistory> findByDateBetween(Date start, Date end);
}
