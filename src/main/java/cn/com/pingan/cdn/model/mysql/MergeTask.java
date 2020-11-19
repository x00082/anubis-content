package cn.com.pingan.cdn.model.mysql;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Classname MergeTask
 * @Description TODO
 * @Date 2020/10/19 14:29
 * @Created by Luj
 */
@Data
@Entity
public class MergeTask {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="vendor_url_ids")
    private String vendorUrlIds;

    @Column(name="create_time")
    private Date createTime;

    @Column(name="update_time")
    private Date updateTime;
}
