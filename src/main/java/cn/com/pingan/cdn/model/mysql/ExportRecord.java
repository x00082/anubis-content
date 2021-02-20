package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.common.HisStatus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Classname RobinRecord
 * @Description TODO
 * @Date 2020/11/22 16:38
 * @Created by Luj
 */
@Data
@Entity
@Table(name="export_record")
public class ExportRecord {

    @Id
    @Column(name="export_id", length=128)
    private String exportId;

    @Column(name="count")
    private Long count;

    @Column(name = "status", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private HisStatus status;

    @Column(name="create_time", nullable = false)
    private Date createTime;

    @Column(name="update_time")
    private Date updateTime;

    @Column(name="type")
    private String type;

    @Column(name="page_size")
    private int pageSize;
}
