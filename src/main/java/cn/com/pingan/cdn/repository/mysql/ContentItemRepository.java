/**   
 * @Project: anubis-content
 * @File: contentItemRepository.java 
 * @Package cn.com.pingan.cdn.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:31:19 
 */
package cn.com.pingan.cdn.repository.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import cn.com.pingan.cdn.model.mysql.ContentItem;

/** 
 * @ClassName: contentItemRepository 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:31:19 
 *  
 */
public interface ContentItemRepository extends JpaRepository<ContentItem, Long>, JpaSpecificationExecutor<ContentItem> {

}
