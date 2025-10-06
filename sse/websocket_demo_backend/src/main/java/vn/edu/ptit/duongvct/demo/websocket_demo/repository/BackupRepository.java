package vn.edu.ptit.duongvct.demo.websocket_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;

@Repository
public interface BackupRepository extends JpaRepository<Backup, Long> {
}
