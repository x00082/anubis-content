/**   
 * @Project: anubis-content
 * @File: RedisContentCountLuaScriptService.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午9:59:58 
 */
package cn.com.pingan.cdn.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/** 
 * @ClassName: RedisContentCountLuaScriptService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午9:59:58 
 *  
 */
@Slf4j
@Component
public class RedisLuaScriptService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource(name = "contentCountLuaScript")
    private RedisScript<Long> countLuaScript;

    @Resource(name = "vendorQpsLuaScript")
    private RedisScript<Long> qpsLuaScript;

    @Resource(name = "vendorQpsAndSizeLuaScript")
    private RedisScript<Long> qpsAndSizeLuaScript;

    @Resource(name = "getUserLimitLuaScript")
    private RedisScript<String> getUserLimitLuaScript;

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
    public int executeCountScript(List<String> keys, List<String> args) {
        try {
            Long scriptValue = redisTemplate.execute(countLuaScript,keys,args.toArray());
            return scriptValue.intValue();
        } catch (Exception e) {
            log.error("execute script error", e);
            return -1;
        }
    }


    /**
     * 执行脚本
     * @param keys
     * @param args
     * @return
     */
    public int executeQpsScript(List<String> keys, List<String> args) {
        try {
            Long scriptValue = redisTemplate.execute(qpsLuaScript,keys,args.toArray());
            return scriptValue.intValue();
        } catch (Exception e) {
            log.error("execute script error", e);
            return -1;
        }
    }

    /**
     * 执行脚本
     * @param keys
     * @param args
     * @return
     */
    public int executeQpsAndTotalScript(List<String> keys, List<String> args) {
        try {
            Long scriptValue = redisTemplate.execute(qpsAndSizeLuaScript,keys,args.toArray());
            return scriptValue.intValue();
        } catch (Exception e) {
            log.error("execute script error", e);
            return -1;
        }
    }

    /**
     * 执行脚本
     * @param keys
     * @param args
     * @return
     */
    public String executeUserLimitScript(List<String> keys, List<String> args) {
        try {
            String scriptValue = redisTemplate.execute(getUserLimitLuaScript,keys,args.toArray());
            return scriptValue.toString();
        } catch (Exception e) {
            log.error("execute script error", e);
            return null;
        }
    }
}