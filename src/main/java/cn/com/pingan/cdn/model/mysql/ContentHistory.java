/**   
 * @Project: anubis-content
 * @File: ContentHistory.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:12:01 
 */
package cn.com.pingan.cdn.model.mysql;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

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
    
    @Column(name="content")
    private String content;
    
    @Column(name="create_time")
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;
    
    @Column(name="content_number")
    private Integer contentNumber;
    
    @Column(name="is_admin")
    private String isAdmin;
    
    @Column(name="step")
    private Integer step = 0;//0-初始化， 1-占用， 2-发送
}
