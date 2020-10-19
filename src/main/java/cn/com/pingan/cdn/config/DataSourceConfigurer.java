/**   
 * @Project: anubis-content
 * @File: DataSourceConfigurer.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 上午10:25:08 
 */
package cn.com.pingan.cdn.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** 
 * @ClassName: DataSourceConfigurer 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 上午10:25:08 
 *  
 */
@Configuration
public class DataSourceConfigurer {
    
    @Bean(name = "pgsqlDataSource")
    //@Qualifier("pgsqlDataSource")
    @ConfigurationProperties(prefix="spring.datasource.pgsql")
    @Primary
    public DataSource PgsqlDataSource() {
        DataSource ds = DataSourceBuilder.create().build();
        return ds;
    }

    @Bean(name = "mysqlDataSource")
    @Qualifier("mysqlDataSource")
    @ConfigurationProperties(prefix="spring.datasource.mysql")
    public DataSource MysqlDataSource() {
        return DataSourceBuilder.create().build();
    }
}
