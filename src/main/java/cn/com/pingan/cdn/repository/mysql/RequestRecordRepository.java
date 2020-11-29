/**   
 * @Project: anubis-content
 * @File: ContentHistoryRepository.java 
 * @Package cn.com.pingan.cdn.service.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:21:18 
 */
package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.RequestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 
 * @ClassName: ContentHistoryRepository 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午10:21:18 
 *  
 */
@Repository
public interface RequestRecordRepository extends JpaRepository<RequestRecord, Long>, JpaSpecificationExecutor<RequestRecord> {

    @Modifying
    @Transactional
    @Query(value = "select * from request_record limit ?1", nativeQuery = true)
    List<RequestRecord> findByLimit(int l);
}