package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.common.TaskStatus;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Classname VendorTaskRepository
 * @Description TODO
 * @Date 2020/10/20 11:34
 * @Created by Luj
 */
@Repository
public interface VendorTaskRepository extends JpaRepository<VendorContentTask, Long>, JpaSpecificationExecutor<VendorContentTask> {


    @Modifying
    @Transactional
    @Query(value = "update vendor_task t set t.version = ?3, t.update_time=current_timestamp where t.version =?2 and t.task_id =?1", nativeQuery = true)
    int updateVersion(String id, int oldVersion, int newVersion);

    @Modifying
    @Transactional
    @Query(value = "update vendor_task t set t.status = ?2, t.update_time=current_timestamp where t.task_id =?1", nativeQuery = true)
    int updateStatus(String id, TaskStatus status);

    List<VendorContentTask> findByItemId(String itemId);


    VendorContentTask findByTaskId(String taskId);
}
