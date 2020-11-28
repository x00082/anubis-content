package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.common.RefreshType;
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
@Table(name="robin_record")
public class RobinRecord {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="robin_id", length=128)
    private String robinId;

    @Column(name = "type", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private RefreshType type;

    @Column(name="vendor", length=128, nullable = false)
    private String vendor;

}
