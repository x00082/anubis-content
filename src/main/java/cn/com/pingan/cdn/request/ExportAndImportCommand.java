package cn.com.pingan.cdn.request;

import cn.com.pingan.cdn.validator.Command;
import lombok.Data;

/**
 * @Classname ExportAndImportCommand
 * @Description TODO
 * @Date 2021/2/8 10:55
 * @Created by Luj
 */
@Data
public class ExportAndImportCommand implements Command {
    private String startTime;
    private String endTime;
    private int pageSize;
    private String type;
}
