/**   
 * @Project: anubis-content
 * @File: contentItemRepository.java 
 * @Package cn.com.pingan.cdn.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:31:19 
 */
package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.model.mysql.ContentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: contentItemRepository
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:31:19 
 *  
 */

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, String>, JpaSpecificationExecutor<ContentItem> {


    //看板统计
    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 group by type,status", nativeQuery = true)
    List<Map<String,Object>> findByCreateTimeBetween(Date startTime, Date endTime);

    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 and sp_code in ?3 group by type,status", nativeQuery = true)
    List<Map<String,Object>> findByCreateTimeBetweenAndSpCodeIn(Date startTime, Date endTime, List<String> spCodes);

    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 and domain_name in ?3 group by type,status", nativeQuery = true)
    List<Map<String,Object>> findByCreateTimeBetweenAndDomainNameIn(Date startTime, Date endTime, List<String> domains);

    @Query(value = "select count(1) count, status, type from content_item where create_time between ?1 and ?2 and (sp_code in ?3 or domain_name in ?4) group by type,status", nativeQuery = true)
    List<Map<String,Object>> findByCreateTimeBetweenAndSpCodeInOrDomainNameIn(Date startTime, Date endTime, List<String> spCodes, List<String> domains);
    //看板统计


    ContentItem findByItemId(String itemId);

    List<ContentItem> findByItemIdIn(List<String> itemIds);

    List<ContentItem> findByUrl(String url);

    List<ContentItem> findByUrlIn(List<String> url);

    List<ContentItem> findByUrlAndUserIdIn(String url, List<String> uid);

    List<ContentItem> findByRequestId(String requestId);

    List<ContentItem> findByRequestIdAndStatus(String requestId, HisStatus status);

    List<ContentItem> findByRequestIdInAndStatus(List<String> requestIds, HisStatus status);

    List<ContentItem> findByRequestIdAndStatusNot(String requestId, HisStatus status);

    List<ContentItem> findByRequestIdIn(List<String> requestIds);


}
