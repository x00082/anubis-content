/**   
 * @Project: anubis-content
 * @File: ContentController.java 
 * @Package cn.com.pingan.cdn.controller 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 上午10:09:09 
 */
package cn.com.pingan.cdn.controller;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.current.JxGaga;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;


/**
 * @ClassName: ContentController 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 上午10:09:09 
 *  
 */
@RestController
@RequestMapping("/content/test")
public class TestController {

        private static final Logger log = org.slf4j.LoggerFactory.getLogger(TestController.class);

        private @Autowired ContentServiceFacade facade;

        private @Autowired HttpServletRequest request;


    @GetMapping("/gowork")
    public ApiReceipt test() throws ContentException, DomainException {
        JxGaga gg = JxGaga.of(Executors.newCachedThreadPool(), 10);
        Map<String, Integer> rs = new ConcurrentHashMap<>();
        log.debug("更新用户历史状态->[Success]");
        for (int i=1;i<=10;i++) {
            int a =i;
            gg.work(() -> {
                String s = Integer.toString(a);
                log.info("inter {}", s);
                return s;
            }, j -> rs.put(j, 1), q -> {
                q.getMessage();
            });
        }
        gg.merge(i -> {
            i.forEach(j -> {
                log.info("返回{}", j);
            });
        }, rs.keySet()).exit();

        return ApiReceipt.ok();
    }

}
