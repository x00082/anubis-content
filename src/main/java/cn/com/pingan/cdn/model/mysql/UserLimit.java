package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Classname UserLimit
 * @Description TODO
 * @Date 2020/11/1 16:11
 * @Created by Luj
 */
@Data
@Entity

@Table(name="uesr_limit", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
})
/*
@Table(name="uesr_limit", indexes = {
        @Index(columnList = "user_id")
        })
*/
public class UserLimit {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", length=128, nullable = false)
    private String userId;

    @Column(name = "default_url_limit", nullable = false)
    private Integer defaultUrlLimit;

    @Column(name = "default_dir_limit", nullable = false)
    private Integer defaultDirLimit;

    @Column(name = "default_preload_limit", nullable = false)
    private Integer defaultPreloadLimit;

    @Column(name = "update_time")
    private Date updateTime;

    public ContentDefaultNumDTO response(){
        ContentDefaultNumDTO re = new ContentDefaultNumDTO();
        re.setSpCode(userId);
        re.setUrlRefreshNumber(defaultUrlLimit);
        re.setDirRefreshNumber(defaultDirLimit);
        re.setUrlPreloadNumber(defaultPreloadLimit);
        re.setUpdateTime(updateTime.getTime());
        return re;
    }
}
