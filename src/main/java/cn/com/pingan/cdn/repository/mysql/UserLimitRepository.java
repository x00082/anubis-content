package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.UserLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @Classname UserLimitRepository
 * @Description TODO
 * @Date 2020/11/1 17:31
 * @Created by Luj
 */
@Repository
public interface UserLimitRepository extends JpaRepository<UserLimit, Long>, JpaSpecificationExecutor<UserLimit> {

    UserLimit findByUserId(String userId);
}
