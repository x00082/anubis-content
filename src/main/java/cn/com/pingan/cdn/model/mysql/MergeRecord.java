package cn.com.pingan.cdn.model.mysql;

import lombok.Data;

import javax.persistence.*;

/**
 * @Classname MergeTask
 * @Description TODO
 * @Date 2020/10/19 14:29
 * @Created by Luj
 */
@Data
@Entity
@Table(name="merge_record")
public class MergeRecord {

    @Id
    @Column(name="record_id", length=128)
    private String recordId;

    @Column(name="merge_id", length=128)
    private String mergeId;
}
