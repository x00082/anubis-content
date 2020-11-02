package cn.com.pingan.cdn.request.openapi;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Classname OpenApiFreshCommand
 * @Description TODO
 * @Date 2020/10/30 16:29
 * @Created by Luj
 */
@Data
@NoArgsConstructor
public class OpenApiFreshCommand {
    @NotBlank
    private List<String> data;
}
