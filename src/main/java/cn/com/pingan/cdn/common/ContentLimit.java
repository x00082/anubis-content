package cn.com.pingan.cdn.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname ContentLimit
 * @Description TODO
 * @Date 2020/10/30 17:01
 * @Created by Luj
 */
@Data
@NoArgsConstructor
public class ContentLimit implements Serializable {
    @NotBlank
    private Item urlRefreshNumber;
    @NotBlank
    private Item urlPreloadNumber;
    @NotBlank
    private Item dirRefreshNumber;
}
