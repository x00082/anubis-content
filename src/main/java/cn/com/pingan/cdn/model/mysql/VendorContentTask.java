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
@Table(name="vendor_task",schema="test")
public class VendorContentTask {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="task_id")
    private String taskId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RefreshType type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(length=4096, name="content")
    private String content;

    @Column(name="job_id")
    private String jobId;

    @Column(name="message")
    private String message;
    
    @Column(name="create_time")
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;
    
    @Column(name="vendor")
    private String vendor;
    
    @Column(name="version")
    private int version;

    @Column(name="item_id")
    private String itemId;
}
