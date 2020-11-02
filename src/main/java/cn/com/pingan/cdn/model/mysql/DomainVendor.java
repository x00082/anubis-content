package cn.com.pingan.cdn.model.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Classname DomainVendor
 * @Description TODO
 * @Date 2020/10/29 11:30
 * @Created by Luj
 */
@Data
@Entity
@Table(name="domain_vendor")
public class DomainVendor {

    @Id
    @Column(length=128)
    private String domain;

    @Column(length=128)
    private String vendors;

    private Date updateTime;
}
