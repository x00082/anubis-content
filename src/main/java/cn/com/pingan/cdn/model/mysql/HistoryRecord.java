package cn.com.pingan.cdn.model.mysql;

import lombok.Data;

import javax.persistence.*;

/**
 * @Classname RobinRecord
 * @Description TODO
 * @Date 2020/11/22 16:38
 * @Created by Luj
 */
@Data
@Entity
@Table(name="history_record",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"request_id"})
        })
public class HistoryRecord {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="request_id", length=128)
    private String requestId;

    @Column(name="version")
    private Integer version;
}
