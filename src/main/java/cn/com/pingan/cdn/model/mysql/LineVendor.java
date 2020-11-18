package cn.com.pingan.cdn.model.mysql;

import lombok.Data;

import javax.persistence.*;

/**
 * @Classname LineVendor
 * @Description TODO
 * @Date 2020/11/3 19:03
 * @Created by Luj
 */
@Data
@Entity

@Table(name="line_vendor", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"line_id"})
})

/*
@Table(name="line_vendor",
        indexes = {
                @Index(columnList = "line_id")
        })
*/
public class LineVendor {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="line_id", length=128, nullable = false)
    private String lineId;

    @Column(length=128)
    private String vendors;
}
