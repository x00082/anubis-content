/**   
 * @Project: anubis-content
 * @File: ContentHistoryRepository.java 
 * @Package cn.com.pingan.cdn.service.repository 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:21:18 
 */
package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.SplitHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lujun
 * @ClassName: ContentHistoryRepository
 * @Description: TODO()
 * @date 2021年01月19日 上午10:21:18
 */
@Repository
public interface SplitHistoryRepository extends JpaRepository<SplitHistory, Long>, JpaSpecificationExecutor<SplitHistory> {

    List<SplitHistory> findByRequestId(String requestId);

    List<SplitHistory> findByRequestIdIn(List<String> requestIds);

}
