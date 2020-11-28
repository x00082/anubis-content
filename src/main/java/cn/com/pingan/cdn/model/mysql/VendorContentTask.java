/**   
 * @Project: anubis-content
 * @File: VendorContentTask.java 
 * @Package cn.com.pingan.cdn.model.mysql 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月19日 上午10:08:51 
 */
package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.common.TaskStatus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/** 
 * @ClassName: VendorContentTask 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月19日 上午10:08:51 
 *  
 */

@Data
@Entity

@Table(name="vendor_task",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"task_id"})
        },
        indexes = {
                @Index(columnList = "request_id"),
                @Index(columnList = "merge_id"),
                @Index(columnList = "history_create_time")
        })

/*
@Table(name="vendor_task",
        indexes = {
                @Index(columnList = "task_id"),
                @Index(columnList = "item_id"),
                @Index(columnList = "request_id"),
                @Index(columnList = "job_id"),
                @Index(columnList = "merge_id"),
                @Index(columnList = "status"),
                @Index(columnList = "create_time")
        })
        */
public class VendorContentTask {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(length=128, name="task_id", nullable = false)
    private String taskId;

    @Column(name = "type", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private RefreshType type;

    @Column(name = "status", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(columnDefinition="TEXT", name="content", nullable = false)
    private String content;

    @Column(name="content_number", nullable = false)
    private Integer contentNumber;

    @Column(name="job_id", length=128)
    private String jobId;

    @Column(name="message")
    private String message;

    @Column(name="history_create_time", nullable = false)
    private Date historyCreateTime;
    
    @Column(name="create_time", nullable = false)
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;
    
    @Column(name="vendor", length=128, nullable = false)
    private String vendor;
    
    @Column(name="version")
    private int version;

    @Column(length=128, name="request_id", nullable = false)
    private String requestId;

    @Column(length=128, name="merge_id")
    private String mergeId;
}
