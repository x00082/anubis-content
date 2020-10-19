/**   
 * @Project: anubis-content
 * @File: DomainRepository.java 
 * @Package cn.com.pingan.cdn.repository.pgsql 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 下午2:31:19 
 */
package cn.com.pingan.cdn.repository.pgsql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.com.pingan.cdn.model.pgsql.Domain;

/** 
 * @ClassName: DomainRepository 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 下午2:31:19 
 *  
 */
public interface DomainRepository extends JpaRepository<Domain, Long> {
    
    
    public List<Domain> findByDomainIn(List<String> domain);
    
    public List<Domain> findByUserCodeAndDomainIn(String userCode, List<String> domain);
    
    public List<Domain> findAllByDomainInAndStatusNot(List<String> domainNames, String status);
}
