package vn.edu.ptit.duongvct.demo.websocket_demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@SQLRestriction("delete_time is null")
@SQLDelete(sql = "update backup set delete_time = now() where id = ?")
public class Backup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private String name;
    private String status;
    private LocalDateTime deleteTime;
}
