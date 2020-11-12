/**   
 * @Project: anubis-content
 * @File: RedisContentCountLuaConfig.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午9:59:28 
 */
package cn.com.pingan.cdn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/** 
 * @ClassName: RedisContentCountLuaConfig 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午9:59:28 
 *  
 */
@Component
public class RedisLuaConfig {
    @Bean("contentCountLuaScript")
    public RedisScript<Long> obtainCouponScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/count.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }


    @Bean("vendorQpsLuaScript")
    public RedisScript<Long> vendorQpsLuaScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/qps.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean("vendorQpsAndSizeLuaScript")
    public RedisScript<Long> vendorQpsAndSizeLuaScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/qpsAndSize.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean("getUserLimitLuaScript")
    public RedisScript<String> getUserLimitLuaScript() {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/getContentNum.lua"));
        redisScript.setResultType(String.class);
        return redisScript;
    }
}
