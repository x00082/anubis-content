/**   
 * @Project: anubis-content
 * @File: ContentHistoryRepository.java 
 * @Package cn.com.pingan.cdn.service.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:21:18 
 */
package cn.com.pingan.cdn.repository.mysql;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cn.com.pingan.cdn.model.mysql.ContentHistory;

/** 
 * @ClassName: ContentHistoryRepository 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午10:21:18 
 *  
 */
@Repository
public interface ContentHistoryRepository extends JpaRepository<ContentHistory, Long>, JpaSpecificationExecutor<ContentHistory> {
    
    /*
    @Transactional
    @Modifying
    @Query(value = "delete from base.content_history c where c.opt_time < ?1", nativeQuery = true)
    Integer clearOldHistory(Date date);
    */
}
