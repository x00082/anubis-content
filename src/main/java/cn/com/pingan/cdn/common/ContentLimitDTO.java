package cn.com.pingan.cdn.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname ContentLimitDTO
 * @Description TODO
 * @Date 2020/10/30 16:50
 * @Created by Luj
 */
@Data
@NoArgsConstructor
public class ContentLimitDTO extends ContentLimit {

    @NotBlank
    private String spCode;

    private Long lastModify;
}
