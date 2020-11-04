package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.LineVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Classname LineToVendorRepository
 * @Description TODO
 * @Date 2020/11/3 19:10
 * @Created by Luj
 */
@Repository
public interface LineToVendorRepository extends JpaRepository<LineVendor, Long>, JpaSpecificationExecutor<LineVendor> {

    LineVendor findByLineId(String lindId);

    @Modifying
    @Transactional
    @Query(value = "select * from line_vendor l  where l.line_id in ?1", nativeQuery = true)
    List<LineVendor> findByLineIdList(List<String> lineIds);
}
