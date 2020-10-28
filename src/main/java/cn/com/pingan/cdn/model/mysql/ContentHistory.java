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
@Table(name="content_history",schema="test")
public class ContentHistory {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="request_id")
    private String requestId;
    
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private HisStatus status;
    
    @Column(name="user_id")
    private String userId;
    
    @Column(length=4096, name="content")
    private String content;
    
    @Column(name="create_time")
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;
    
    @Column(name="content_number")
    private Integer contentNumber;
    
    @Column(name="is_admin")
    private String isAdmin;

}
