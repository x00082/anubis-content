package cn.com.pingan.cdn.model.mysql;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @Classname MergeTask
 * @Description TODO
 * @Date 2020/10/19 14:29
 * @Created by Luj
 */

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
