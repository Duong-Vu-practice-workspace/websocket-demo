package vn.edu.ptit.duongvct.demo.websocket_demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@SQLRestriction(value = "delete_time is null")
@SQLDelete(sql = "update action_log set delete_time = now() where id = ?")
public class ActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String entityType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
