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
@Table(name="content_item",schema="test")
public class ContentItem {
    
    @Id
    //@SequenceGenerator(name="CONTENTHIS_ID_GENERATOR",sequenceName="contenthis_id_seq",allocationSize=1,schema="base")
    //@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="CONTENTHIS_ID_GENERATOR")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="item_id")
    private String itemId;
    
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private HisStatus status;

    @Column(length=4096, name="content")
    private String content;
    
    @Column(name="message")
    private String message;
    
    @Column(name="create_time")
    private Date createTime;
    
    @Column(name="update_time")
    private Date updateTime;

    @Column(name="vendor")
    private String vendor;

    @Column(name="request_id")
    private String requestId;


}
