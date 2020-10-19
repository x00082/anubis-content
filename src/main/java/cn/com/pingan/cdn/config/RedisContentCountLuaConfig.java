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
public class RedisContentCountLuaConfig {
    @Bean("contentCountLuaScript")
    public RedisScript<Long> obtainCouponScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/count.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
