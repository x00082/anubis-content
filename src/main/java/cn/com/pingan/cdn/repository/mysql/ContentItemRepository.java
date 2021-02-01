/**   
 * @Project: anubis-content
 * @File: contentItemRepository.java 
 * @Package cn.com.pingan.cdn.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:31:19 
 */
package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.ContentItem;
import cn.com.pingan.cdn.request.QueryHisCountDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @ClassName: contentItemRepository
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:31:19 
 *  
 */

public interface ContentItemRepository extends JpaRepository<ContentItem, Long>, JpaSpecificationExecutor<ContentItem> {


    //看板统计
    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetween(Date startTime, Date endTime);

    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 and user_id in ?3 group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetweenAndUserIdIn(Date startTime, Date endTime, List<String> uuids);

    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 and domain_name in ?3 group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetweenAndDomainNameIn(Date startTime, Date endTime, List<String> domains);

    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 and (user_id in ?3 or domain_name in ?4) group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetweenAndUserIdInOrDomainNameIn(Date startTime, Date endTime, List<String> uuids, List<String> domains);
    //看板统计


    ContentItem findByItemId(String itemId);

    List<ContentItem> findByItemIdIn(List<String> itemIds);

    List<ContentItem> findByRequestId(String requestId);

    List<ContentItem> findByRequestIdAndStatus(String requestId, String status);

    List<ContentItem> findByRequestIdInAndStatus(List<String> requestIds, String status);

    List<ContentItem> findByRequestIdAndStatusNot(String requestId, String status);

    List<ContentItem> findByRequestIdIn(List<String> requestIds);


}
