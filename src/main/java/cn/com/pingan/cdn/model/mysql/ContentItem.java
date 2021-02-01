/**   
 * @Project: anubis-content
 * @File: ContentDisplay.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:10:40 
 */
package cn.com.pingan.cdn.model.mysql;

/**
 * @ClassName: ContentDisplay 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:10:40 
 *  
 */


import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity

@Table(name="content_item",

        indexes = {
                @Index(columnList = "request_id"),
                @Index(columnList = "type"),
                @Index(columnList = "status"),
                @Index(columnList = "user_id"),
                @Index(columnList = "sp_code"),
                @Index(columnList = "url"),
                @Index(columnList = "create_time"),
        })

public class ContentItem {
    
    @Id
    @Column(length=128, name="item_id", nullable = false)
    private String itemId;

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

    @Column(name="sp_code", length=128, nullable = false)
    private String spCode;

    @Column(name="url", nullable = false)
    private String url;

    @Column(name="create_time", nullable = false)
    private Date createTime;

    @Column(name="update_time")
    private Date updateTime;

    @Column(name="domain_name", length=128, nullable = false)
    private String domainName;

    @Column(name="is_admin", length=16, nullable = false)
    private String isAdmin;

    @Column(name = "all_task_num")
    private Integer allTaskNum = 0;

    @Column(name = "success_task_num")
    private Integer successTaskNum = 0;

    @Column(name = "version")
    private Integer version= 0;


}

