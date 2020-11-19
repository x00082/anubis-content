/**   
 * @Project: anubis-content
 * @File: contentItemRepository.java 
 * @Package cn.com.pingan.cdn.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午11:31:19 
 */
package cn.com.pingan.cdn.repository.mysql;

/**
 * @ClassName: contentItemRepository 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午11:31:19 
 *  
 */
/*
public interface ContentItemRepository extends JpaRepository<ContentItem, Long>, JpaSpecificationExecutor<ContentItem> {

    ContentItem findByItemId(String itemId);

    List<ContentItem> findByRequestId(String requestId);

    @Modifying
    @Transactional
    @Query(value = "select * from content_item i where i.item_id in ?1", nativeQuery = true)
    List<ContentItem> findByItemIdList(List<String> itemIds);

    @Modifying
    @Transactional
    @Query(value = "delete from content_item  where create_time <?1", nativeQuery = true)
    void clear(Date time);

}
*/