/**   
 * @Project: anubis-content
 * @File: ContentDisplay.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:10:40 
 */
package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/** 
 * @ClassName: ContentDisplay 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:10:40 
 *  
 */
@Data
@Entity
@Table(name="content_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"item_id"})
        },
        indexes = {
                @Index(columnList = "request_id")
        })
public class ContentItem {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(length=128, name="item_id", nullable = false)
    private String itemId;
    
    @Column(name = "type", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    
    @Column(name = "status", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private HisStatus status;

    @Column(length=4096, name="content", nullable = false)
    private String content;
    
    @Column(name="message")
    private String message;
    
    @Column(name="create_time", nullable = false)
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;

    @Column(name="vendor", nullable = false)
    private String vendor;

    @Column(length=128, name="request_id", nullable = false)
    private String requestId;


}
