/**   
 * @Project: anubis-content
 * @File: Utils.java 
 * @Package cn.com.pingan.cdn.utils 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 下午2:14:01 
 */
package cn.com.pingan.cdn.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import cn.com.pingan.cdn.common.ErrEnum;
import cn.com.pingan.cdn.exception.RestfulException;

/** 
 * @ClassName: Utils 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 下午2:14:01 
 *  
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String uuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    public static <T> String objectToString(Object o, Class<T> valueType) {
        String res = "";
        try {
            if (o == null) {
                o = valueType.newInstance();
            }
            res = new ObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            throw new RestfulException(ErrEnum.ErrInternal.getCode(), e.getMessage());
        }
        return res;
    }

    public static String objectToString(Object o) {
        String res = "";
        if (o == null) {
            return res;
        }
        try {
            res = new ObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            throw new RestfulException(ErrEnum.ErrInternal.getCode(), e.getMessage());
        }
        return res;
    }

    public static <T> T stringToObject(String v, Class<T> valueType) throws RestfulException {
        T t;
        try {
            if (StringUtils.isEmpty(v)) {
                t = valueType.newInstance();
            } else {
                t = new ObjectMapper().readValue(v, valueType);
            }
        } catch (Exception e) {
            throw new RestfulException(ErrEnum.ErrInternal.getCode(), e.getMessage());
        }
        return t;
    }

    public static <T> ArrayList<T> stringToArrayObject(String v, Class<T> valueType) throws RestfulException {
        ArrayList<T> t;
        try {
            if (StringUtils.isEmpty(v)) {
                t = new ArrayList<T>();
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, valueType);
                t = objectMapper.readValue(v, javaType);
            }
        } catch (Exception e) {
            throw new RestfulException(ErrEnum.ErrInternal.getCode(), e.getMessage());
        }
        return t;
    }

    public static <T> Map<String, T> stringToMapObject(String v, Class<T> valueType) throws RestfulException {
        Map<String, T> t = new HashMap<String, T>();
        try {
            if (!StringUtils.isEmpty(v)) {
                MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, valueType);
                t = new ObjectMapper().readValue(v, type);
            }
        } catch (Exception e) {
            throw new RestfulException(ErrEnum.ErrInternal.getCode(), e.getMessage());
        }
        return t;
    }
}
