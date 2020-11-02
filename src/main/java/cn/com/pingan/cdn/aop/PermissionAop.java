package cn.com.pingan.cdn.aop;

/*
import cn.com.pingan.cdn.exception.SslCertException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * @program: anubis-base
 *
 * @description: 越权校验
 *
 * @author: wangchao535
 *
 * @create: 2019-08-29 16:05
 **/
/*
@Aspect
@Slf4j
@Component
public class PermissionAop {
    @Pointcut("execution(* cn.com.pingan.cdn.controller .*.*(..))")
    private void anyMethod(){ }

    @Before("anyMethod()")
    public void deBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
        log.info("URL : " + request.getRequestURL().toString());
        log.info("HTTP_METHOD : " + request.getMethod());
        log.info("IP : " + request.getRemoteAddr());
        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));

        Enumeration<String> names=request.getHeaderNames();
        while (names.hasMoreElements()){
            String name=(String)names.nextElement();
            log.info(name+":"+request.getHeader(name));
        }
//        GateWayHeaderDTO dto = new GateWayHeaderDTO();
        //校验账户信息
        if(request.getHeader("uid") == null &&StringUtils.isEmpty(request.getHeader("uid"))){
            throw new SslCertException("0x0001");
        }
//        if(request.getHeader("isAdmin")==null &&StringUtils.isEmpty(request.getHeader("isAdmin"))){
//            throw new SslCertException("0x0001");
//        }
        if(request.getHeader("channel")==null &&StringUtils.isEmpty(request.getHeader("channel"))){
            throw new SslCertException("0x0001");
        }
        if(request.getHeader("username")==null &&StringUtils.isEmpty(request.getHeader("username"))){
            throw new SslCertException("0x0001");
        }
        if(request.getHeader("spcode")==null &&StringUtils.isEmpty(request.getHeader("spcode"))){
            throw new SslCertException("0x0001");
        }
    }
    private Map getRequestFirstMap(Object[]argus){
        if(argus!=null &&argus.length>0 && argus[0] instanceof Map){
            return (Map)argus[0];
        }
        return null;
    }

    @AfterReturning(returning = "ret", pointcut = "anyMethod()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        log.debug("方法的返回值 : " + JSON.toJSONString((ret)));
    }

    //后置异常通知
    @AfterThrowing(pointcut = "anyMethod()",throwing="ex")
    public void throwss(JoinPoint jp,Exception ex){
        try {
            log.info("方法异常时执行.....");
//            log.error(getPrintPrefix(jp) + "Failed: ", ex);
            log.error(getPrintPrefix(jp) + "Failed: ",ex);
        }catch (Exception e){
            log.error("异常",e);
        }

    }
    String getPrintPrefix(JoinPoint joinPoint){
        String className=joinPoint.getTarget().getClass().getSimpleName();
        String methodName=joinPoint.getSignature().getName();
        return className+".componentName."+methodName;
    }

//    //后置最终通知,final增强，不管是抛出异常或者正常退出都会执行
//    @After("anyMethod()")
//    public void after(JoinPoint jp){
//        log.info("方法最后执行.....");
//    }

//    //环绕通知,环绕增强，相当于MethodInterceptor
//    @Around("anyMethod()")
//    public Object arround(ProceedingJoinPoint pjp) throws Throwable{
//        log.info("方法环绕start.....");
////        try {
//            Object o =  pjp.proceed();
//            log.info("方法环绕proceed，结果是 :" + JSON.toJSONString(o));
//            return o;
////        } catch (Throwable e) {
////            e.printStackTrace();
////            return null;
////        }
//    }


}
*/
