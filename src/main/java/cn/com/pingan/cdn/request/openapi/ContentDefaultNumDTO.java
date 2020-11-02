package cn.com.pingan.cdn.request.openapi;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname ContentDefaultNumDTO
 * @Description TODO
 * @Date 2020/11/1 19:37
 * @Created by Luj
 */
@Data
@NoArgsConstructor
public class ContentDefaultNumDTO implements Serializable {

    @NotBlank
    private String spCode;

    private Integer urlRefreshNumber;

    private Integer urlPreloadNumber;

    private Integer dirRefreshNumber;

    private Long updateTime;
}
