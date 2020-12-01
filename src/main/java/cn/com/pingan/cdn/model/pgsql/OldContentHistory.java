package cn.com.pingan.cdn.model.pgsql;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Classname OldContentHistory
 * @Description TODO
 * @Date 2020/12/1 11:29
 * @Created by Luj
 */
@Data
@Entity
@Table(name="content_history",schema="base")
public class OldContentHistory {

    @Id
    @SequenceGenerator(name="CONTENTHIS_ID_GENERATOR",sequenceName="contenthis_id_seq",allocationSize=1,schema="base")
    @GeneratedValue(strategy= GenerationType.SEQUENCE,generator="CONTENTHIS_ID_GENERATOR")
    private Long id;
    @Column(name="task_id")
    private String taskId;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private HisStatus status;
    @Column(name="user_id")
    private String userId;
    @Column(name="content")
    private String content;
    @Column(name="opt_time")
    private Date optTime;
    @Column(name="content_number")
    private Integer contentNumber;
    @Column(name="is_admin")
    private String isAdmin;
}
