/**   
 * @Project: anubis-content
 * @File: ContentHistory.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:12:01 
 */
package cn.com.pingan.cdn.model.mysql;

/**
 * @ClassName: ContentHistory 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午10:12:01 
 *  
 */

/*
@Data
@Entity

@Table(name="split_history",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"task_id"})
        },
        indexes = {
                @Index(columnList = "type"),
                @Index(columnList = "status"),
                @Index(columnList = "user_id"),
                @Index(columnList = "create_time"),
                @Index(columnList = "domain_name"),

})

public class SplitHistory {

    @Id
    @Column(length=128, name="task_id", nullable = false)
    private String taskId;

    @Column(length=128, name="request_id", nullable = false)
    private String requestId;
    
    @Column(name = "type", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    
    @Column(name = "status", length=32, nullable = false)
    @Enumerated(EnumType.STRING)
    private HisStatus status;
    
    @Column(name="user_id", length=128, nullable = false)
    private String userId;
    
    @Column(columnDefinition="TEXT", name="content", nullable = false)
    private String content;
    
    @Column(name="create_time", nullable = false)
    private Date createTime;

    @Column(name="domain_name", length=128, nullable = false)
    private String domainName;

    @Column(name="is_admin", length=16, nullable = false)
    private String isAdmin;

}
*/