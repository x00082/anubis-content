/**   
 * @Project: anubis-content
 * @File: ErrorCode.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:24:44 
 */
package cn.com.pingan.cdn.exception;

/** 
 * @ClassName: ErrorCode 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:24:44 
 *  
 */

/*
0x0000=请求参数异常
0x0001=禁止越权浏览信息
0x0002=内部错误
0x0003=重复的配置
0x0004=重复的操作

0x001003=域名不能为空
0x001004=域名类型不正确

0x004001=缓存刷新用户ID违法
0x004002=缓存刷新类型违法
0x004003=缓存刷新输入值违法
0x004004=缓存预取用户ID违法
0x004005=缓存预取输入值违法
0x004006=不支持跨用户刷新路径
0x004007=header缺失
//0x004008=内容为空,请正确填写
0x004009=全站加速域名默认全局不缓存，您可指定静态资源的缓存时间
0x004010=禁止越权操作
0x004011=请求参数异常，请指定操作用户
0x004012=超过每日数量上限
0x004013=超过单次提交上限
0x004014=url应以http://或https://开头
0x004015=文件刷新不能以/结尾
0x004016=预取不能以/结尾
0x004017=目录刷新须以/结尾

0x007001=数据不存在
*/
public enum ErrorCode {

    PARAMILLEGAL("0x0000", "请求参数异常"),
    INTERERR("0x0002", "内部错误"),
    DUPLICATE("0x0003", "重复的配置"),
    DUPLICATEOPT("0x0004", "重复的操作"),

    DMNAME("0x001003", "域名不能为空"),
    DMTYPE("0x001004", "域名类型不正确"),

    NOHEADER("0x004007","header缺失"),

    OUTLIMITDAY("0x004012", "超过每日数量上限"),
    OUTLIMITSINGLE("0x004013", "超过单次提交上限"),
    STARTURL("0x004014", "url应以http://或https://开头"),
    ENDURL("0x004015", "文件刷新不能以/结尾"),
    ENDPRELOAD("0x004016", "预取不能以/结尾"),
    ENDDIR("0x004017", "目录刷新须以/结尾"),

    FORBIDOPT("0x004010", "禁止越权操作");

    private String code;
    private String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static String parse(String code) {
        for (ErrorCode ec : ErrorCode.values())
            if (ec.getCode().equals(code)) return ec.getDescription();
        return "";
    }
}
