/**   
 * @Project: anubis-content
 * @File: Item.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午10:19:03 
 */
package cn.com.pingan.cdn.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 * @ClassName: Item 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午10:19:03 
 *  
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private int used; //当日已使用

    private int limit; //每日上限
}
