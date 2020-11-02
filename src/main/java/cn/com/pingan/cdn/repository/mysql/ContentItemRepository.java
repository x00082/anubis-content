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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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

    ContentItem findByItemId(String itemId);

    List<ContentItem> findByRequestId(String requestId);

    @Modifying
    @Transactional
    @Query(value = "delete from content_item  where create_time <?1", nativeQuery = true)
    void clear(Date time);
}
