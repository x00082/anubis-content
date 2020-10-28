/**   
 * @Project: anubis-content
 * @File: FreshCommand.java 
 * @Package cn.com.pingan.cdn.validator.content 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:29:53 
 */
package cn.com.pingan.cdn.validator.content;

import cn.com.pingan.cdn.validator.Command;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/** 
 * @ClassName: FreshCommand 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:29:53 
 *  
 */
@Data
public class FreshCommand implements Command {
    @NotEmpty
    private List<String> data;
    private String channel;
    private String account;
    private String subAccount;
    private String uid;
    @NotEmpty
    private String spCode;
}
