package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.DomainVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Classname DomainVendorRepository
 * @Description TODO
 * @Date 2020/10/29 18:00
 * @Created by Luj
 */
@Repository
public interface DomainVendorRepository extends JpaRepository<DomainVendor, Long>, JpaSpecificationExecutor<DomainVendor> {

    DomainVendor findByDomain(String domain);


    @Modifying
    @Transactional
    @Query(value = "select * from domain_vendor d  where d.domain in ?1", nativeQuery = true)
    List<DomainVendor> findByDomainList(List<String> domains);
}
