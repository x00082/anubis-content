package cn.com.pingan.cdn.repository.pgsql;

import cn.com.pingan.cdn.model.pgsql.OldContentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @Classname OldContentHistoryRepository
 * @Description TODO
 * @Date 2020/12/1 11:35
 * @Created by Luj
 */
public interface OldContentHistoryRepository extends JpaRepository<OldContentHistory, Long>, JpaSpecificationExecutor<OldContentHistory>  {

}
