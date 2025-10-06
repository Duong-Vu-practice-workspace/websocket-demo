package vn.edu.ptit.duongvct.websocket_demo1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.duongvct.websocket_demo1.domain.Backup;

@Repository
public interface BackupRepository extends JpaRepository<Backup, Long> {
}
