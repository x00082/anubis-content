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
import java.util.List;

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


    List<ContentHistory> findByRequestIdIn(List<String> requestIds);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = ?2, h.update_time=current_timestamp where h.request_id =?1", nativeQuery = true)
    int updateStatus(String id, String status);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = ?2, h.update_time=current_timestamp, h.message = ?3 where h.request_id =?1", nativeQuery = true)
    int updateStatusAndMessageByRequestId(String id, String status, String message);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = ?2, h.update_time=current_timestamp, h.message = ?3 where h.id =?1", nativeQuery = true)
    int updateStatusAndMessageById(Long id, String status, String message);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.flow_status = ?2, h.update_time=current_timestamp where h.request_id =?1", nativeQuery = true)
    int updateFlowStatusByRequestId(String id, String status);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.flow_status = ?2, h.update_time=current_timestamp where h.id =?1", nativeQuery = true)
    int updateFlowStatusById(Long id, String status);


    @Modifying
    @Transactional
    @Query(value = "delete from content_history  where create_time <?1", nativeQuery = true)
    void clear(Date time);


    @Modifying
    @Transactional
    @Query(value = "select * from content_history  where create_time <?1 limit ?2", nativeQuery = true)
    List<ContentHistory> findByCreateTimeBeforeAndLimit(Date time, int num);

    List<ContentHistory> findByCreateTimeBefore(Date time);

    /*
    @Transactional
    @Modifying
    @Query(value = "delete from base.content_history c where c.opt_time < ?1", nativeQuery = true)
    Integer clearOldHistory(Date date);
    */
}
