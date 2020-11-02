/**   
 * @Project: anubis-content
 * @File: ContentHistoryRepository.java 
 * @Package cn.com.pingan.cdn.service.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:21:18 
 */
package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.ContentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/** 
 * @ClassName: ContentHistoryRepository 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午10:21:18 
 *  
 */
@Repository
public interface ContentHistoryRepository extends JpaRepository<ContentHistory, Long>, JpaSpecificationExecutor<ContentHistory> {



    ContentHistory findByRequestId(String requestId);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = ?2, h.update_time=current_timestamp where h.request_id =?1", nativeQuery = true)
    int updateStatus(String id, String status);


    @Modifying
    @Transactional
    @Query(value = "delete from content_history  where create_time <?1", nativeQuery = true)
    void clear(Date time);

    /*
    @Transactional
    @Modifying
    @Query(value = "delete from base.content_history c where c.opt_time < ?1", nativeQuery = true)
    Integer clearOldHistory(Date date);
    */
}
