/**   
 * @Project: anubis-content
 * @File: VendorContentTask.java 
 * @Package cn.com.pingan.cdn.model.mysql 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月19日 上午10:08:51 
 */
package cn.com.pingan.cdn.model.mysql;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;

/** 
 * @ClassName: VendorContentTask 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月19日 上午10:08:51 
 *  
 */
public class VendorContentTask {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="content_item_id")
    private String contentItemId;
    
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private HisStatus status;

    @Column(name="content")
    private String content;
    
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
}
