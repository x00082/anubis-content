/**   
 * @Project: anubis-content
 * @File: RedisContentCountLuaScriptService.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午9:59:58 
 */
package cn.com.pingan.cdn.config;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** 
 * @ClassName: RedisContentCountLuaScriptService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午9:59:58 
 *  
 */
@Slf4j
@Component
public class RedisContentCountLuaScriptService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource(name = "contentCountLuaScript")
    private RedisScript<Long> countLuaScript;

    /**
     * 启动时加载
     */
    @PostConstruct
    public void loadScript() {
        redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection redisConnection = (StringRedisConnection) connection;
                return redisConnection.scriptLoad(countLuaScript.getScriptAsString());
            }
        });
    }

    /**
     * 执行脚本
     * @param keys
     * @param args
     * @return
     */
    public int executeScript(List<String> keys, List<String> args) {
        try {
            Long scriptValue = redisTemplate.execute(countLuaScript,keys,args.toArray());
            return scriptValue.intValue();
        } catch (Exception e) {
            log.error("execute script error", e);
            return -1;
        }
    }
}
