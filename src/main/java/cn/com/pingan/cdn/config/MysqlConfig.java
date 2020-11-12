/**   
 * @Project: anubis-content
 * @File: mysqlConfig.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 下午2:35:03 
 */
package cn.com.pingan.cdn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

/** 
 * @ClassName: MysqlConfig
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 下午2:35:03 
 *  
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef="entityManagerFactoryMysql",
                        transactionManagerRef="transactionManagerMysql",
                        basePackages= { "cn.com.pingan.cdn.repository.mysql" })
public class MysqlConfig {
    @Autowired @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;

    @Bean(name = "entityManagerMysql")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryMysql(builder).getObject().createEntityManager();
    }


    @Bean(name = "entityManagerFactoryMysql")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryMysql(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mysqlDataSource)
                .properties(getVendorProperties(mysqlDataSource))
                .packages("cn.com.pingan.cdn.model.mysql")
                .persistenceUnit("mysqlPersistenceUnit")
                .build();
    }

    @Autowired(required=false)
    private JpaProperties jpaProperties;

    private Map<String, Object> getVendorProperties(DataSource dataSource) {
        Map<String, Object> ret = jpaProperties.getHibernateProperties(new HibernateSettings());
        ret.put("hibernate.dialect", "cn.com.pingan.cdn.config.MySQL5InnoDBDialectUtf8mb4");
        return ret;
    }

    @Bean(name = "transactionManagerMysql")
    public PlatformTransactionManager transactionManagerMysql(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryMysql(builder).getObject());
    }
}
