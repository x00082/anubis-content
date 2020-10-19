/**   
 * @Project: anubis-content
 * @File: ApplicationContextProvider.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:00:58 
 */
package cn.com.pingan.cdn.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/** 
 * @ClassName: ApplicationContextProvider 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:00:58 
 *  
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext ctx;

    /**
     * @Method: setApplicationContext
     * @Description: TODO()
     * @param applicationContext
     * @throws BeansException 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext) 
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
    
    public static ApplicationContext l() {
        return ctx;
    }
    
    public static  <T> T l(Class<T> clazz){
        return l().getBean(clazz);
    }

}
