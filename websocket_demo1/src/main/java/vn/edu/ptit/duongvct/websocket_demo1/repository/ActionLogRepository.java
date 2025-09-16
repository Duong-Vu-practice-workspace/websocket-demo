package vn.edu.ptit.duongvct.websocket_demo1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.duongvct.websocket_demo1.domain.ActionLog;

import java.util.Optional;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
    Optional<ActionLog> findByEntityId(String entityId);
}
