/**   
 * @Project: anubis-content
 * @File: DataSourceConfigurer.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 上午10:25:08 
 */
package cn.com.pingan.cdn.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

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
        DataSource pd = DataSourceBuilder.create().type(org.apache.commons.dbcp2.BasicDataSource.class).build();
        return pd;
    }

    @Bean(name = "mysqlDataSource")
    @Qualifier("mysqlDataSource")
    @ConfigurationProperties(prefix="spring.datasource.mysql")
    public DataSource MysqlDataSource() {
        DataSource md = DataSourceBuilder.create().type(org.apache.commons.dbcp2.BasicDataSource.class).build();
        return md;
    }
}
