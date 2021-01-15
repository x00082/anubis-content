/**   
 * @Project: anubis-content
 * @File: PgsqlConfig.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 上午10:35:40 
 */
package cn.com.pingan.cdn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

/** 
 * @ClassName: PgsqlConfig 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 上午10:35:40 
 *  
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef="entityManagerFactoryPgsql",
                        transactionManagerRef="transactionManagerPgsql",
                        basePackages= { "cn.com.pingan.cdn.repository.pgsql" })
public class PgsqlConfig {
    
        @Autowired @Qualifier("pgsqlDataSource")
        private DataSource pgsqlDataSource;
    
        @Bean(name = "entityManagerPgsql")
        @Primary
        public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
            return entityManagerFactoryPgsql(builder).getObject().createEntityManager();
        }
    

        @Bean(name = "entityManagerFactoryPgsql")
        @Primary
        public LocalContainerEntityManagerFactoryBean entityManagerFactoryPgsql(EntityManagerFactoryBuilder builder) {
            return builder
                    .dataSource(pgsqlDataSource)
                    .properties(getVendorProperties(pgsqlDataSource))
                    .packages("cn.com.pingan.cdn.model.pgsql") //设置实体类所在位置
                    .persistenceUnit("pgsqlPersistenceUnit")
                    .build();
        }
    
        @Autowired(required=false)
        private JpaProperties jpaProperties;
    
        private Map<String, Object> getVendorProperties(DataSource dataSource) {
            Map<String, Object> ret = jpaProperties.getHibernateProperties(new HibernateSettings());
            //Map<String, String> ret = jpaProperties.getProperties();
            ret.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            ret.remove("hibernate.hbm2ddl.auto");
            return ret;
        }
    
        @Bean(name = "transactionManagerPgsql")
        @Primary
        public PlatformTransactionManager transactionManagerPgsql(EntityManagerFactoryBuilder builder) {
            return new JpaTransactionManager(entityManagerFactoryPgsql(builder).getObject());
        }
}
