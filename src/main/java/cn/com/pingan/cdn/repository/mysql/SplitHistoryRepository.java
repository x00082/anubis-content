/**   
 * @Project: anubis-content
 * @File: ContentHistoryRepository.java 
 * @Package cn.com.pingan.cdn.service.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:21:18 
 */
package cn.com.pingan.cdn.repository.mysql;

/**
 * @author lujun
 * @ClassName: ContentHistoryRepository
 * @Description: TODO()
 * @date 2021年01月19日 上午10:21:18
 */
/*@Repository
public interface SplitHistoryRepository extends JpaRepository<SplitHistory, String>, JpaSpecificationExecutor<SplitHistory> {

    List<SplitHistory> findByRequestId(String requestId);

    List<SplitHistory> findByRequestIdIn(List<String> requestIds);

    @Query(value = "select count(1) count, status, type from split_history where create_time between ?1 and ?2 group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetween(Date startTime, Date endTime);

    @Query(value = "select count(1) count, status, type from split_history where create_time between ?1 and ?2 and user_id in ?3 group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetweenAndUserIdIn(Date startTime, Date endTime, List<String> uuids);

    @Query(value = "select count(1) count, status, type from split_history where create_time between ?1 and ?2 and domain_name in ?3 group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetweenAndDomainNameIn(Date startTime, Date endTime, List<String> domains);

    @Query(value = "select count(1) count, status, type from split_history where create_time between ?1 and ?2 and (user_id in ?3 or domain_name in ?4) group by type,status", nativeQuery = true)
    List<QueryHisCountDTO.HisCountResult> findByCreateTimeBetweenAndUserIdInOrDomainNameIn(Date startTime, Date endTime, List<String> uuids, List<String> domains);

}
*/
