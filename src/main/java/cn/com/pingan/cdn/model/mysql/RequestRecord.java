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
@Table(name="request_record")
public class RequestRecord {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="request_id", length=128)
    private String requestId;
}
