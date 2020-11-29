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

    List<ContentHistory> findByIdIn(List<Long> Ids);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = 'FAIL', h.message = 'TimeOut', h.update_time=current_timestamp where status not in ?1 and (h.update_time is null or h.update_time < ?2) limit ?3", nativeQuery = true)
    int updateStatusFailNotINAndUpdateTimeLessThanLimit(List<String> types, Date time, int limit);


    @Modifying
    @Transactional
    @Query(value = "select content_history h where status not in ?1 and create_time < ?2 limit ?3", nativeQuery = true)
    List<ContentHistory> findStatusNotINAndUpdateTimeLessThanAndLimit(List<String> types, Date time, int limit);


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

/*
    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.success_vendor_num = h.success_vendor_num + ?2, h.update_time=current_timestamp where h.id =?1 and h.version = ?2", nativeQuery = true)
    int updateSuccessNumByIdAndVersion(Long id, Integer version);
*/

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.success_task_num = h.success_task_num + ?2, h.update_time=current_timestamp where h.request_id =?1 and h.version = ?3", nativeQuery = true)
    int updateSuccessNumByRequestIdAndVersion(String id, int num, int version);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = ?3, h.update_time=current_timestamp, h.message = ?4 where h.id =?1 and h.version = ?2", nativeQuery = true)
    int updateStatusAndMessageByIdAndVersion(Long id, Integer version, String status, String message);

    @Modifying
    @Transactional
    @Query(value = "update content_history h set h.status = ?2, h.update_time=current_timestamp, h.message = ?3 where h.request_id =?1 and h.version = ?4", nativeQuery = true)
    int updateStatusAndMessageByRequestIdAndVersion(String id, String status, String message, Integer version);

    @Modifying
    @Transactional
    @Query(value = "delete from content_history  where create_time <?1 limit ?2", nativeQuery = true)
    int clear(Date time, int limit);


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
