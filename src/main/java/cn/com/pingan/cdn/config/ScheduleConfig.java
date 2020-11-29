package cn.com.pingan.cdn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @Classname ScheduleConfig
 * @Description TODO
 * @Date 2020/11/29 16:22
 * @Created by Luj
 */
@Configuration
public class ScheduleConfig {

    @Value("${task.scheduler.num:10}")
    private Integer schedulerNum;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerNum);
        return scheduler;
    }
}
