package cn.com.pingan.cdn.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname ContentLimitConfigDTO
 * @Description TODO
 * @Date 2020/11/1 17:41
 * @Created by Luj
 */
@Data
@NoArgsConstructor
public class ContentLimitConfigDTO extends ContentLimitDTO {

    private Boolean setDefault;
}
