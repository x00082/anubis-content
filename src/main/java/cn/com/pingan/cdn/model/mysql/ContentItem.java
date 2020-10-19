/**   
 * @Project: anubis-content
 * @File: ContentDisplay.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:10:40 
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
 * @ClassName: ContentDisplay 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:10:40 
 *  
 */
@Data
@Entity
@Table(name="content_item",schema="test")
public class ContentItem {
    
    @Id
    //@SequenceGenerator(name="CONTENTHIS_ID_GENERATOR",sequenceName="contenthis_id_seq",allocationSize=1,schema="base")
    //@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="CONTENTHIS_ID_GENERATOR")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="task_id")
    private String taskId;
    
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
    
    @Column(name="dynamic_vendor")
    private String dynamicVendor;
    

}
