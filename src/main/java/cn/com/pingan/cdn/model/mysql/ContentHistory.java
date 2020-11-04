/**   
 * @Project: anubis-content
 * @File: ContentHistory.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:12:01 
 */
package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/** 
 * @ClassName: ContentHistory 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午10:12:01 
 *  
 */
@Data
@Entity
@Table(name="content_history",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"request_id"})
        })
public class ContentHistory {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(length=128, name="request_id", nullable = false)
    private String requestId;
    
    @Column(name = "type", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    
    @Column(name = "status", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private HisStatus status;

    @Column(name="message")
    private String message;
    
    @Column(name="user_id", length=128, nullable = false)
    private String userId;
    
    @Column(length=4096, name="content", nullable = false)
    private String content;
    
    @Column(name="create_time", nullable = false)
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;
    
    @Column(name="content_number", nullable = false)
    private Integer contentNumber;
    
    @Column(name="is_admin", length=16, nullable = false)
    private String isAdmin;

}
