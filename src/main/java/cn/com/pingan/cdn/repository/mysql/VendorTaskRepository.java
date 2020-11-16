package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.common.TaskStatus;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Classname VendorTaskRepository
 * @Description TODO
 * @Date 2020/10/20 11:34
 * @Created by Luj
 */
@Repository
public interface VendorTaskRepository extends JpaRepository<VendorContentTask, Long>, JpaSpecificationExecutor<VendorContentTask> {

    @Query(value = "select * from vendor_task v where status not in ?1 and create_time < ?2", nativeQuery = true)
    List<VendorContentTask> findByStatusNotINAndUpdateTimeLessThan(List<String> types, Date time);

    @Modifying
    @Transactional
    @Query(value = "update vendor_task t set t.version = ?3, t.update_time=current_timestamp where t.version =?2 and t.task_id =?1", nativeQuery = true)
    int updateVersion(String id, int oldVersion, int newVersion);

    @Modifying
    @Transactional
    @Query(value = "update vendor_task t set t.status = ?2, t.update_time=current_timestamp where t.task_id =?1", nativeQuery = true)
    int updateStatus(String id, TaskStatus status);

    List<VendorContentTask> findByItemId(String itemId);

    List<VendorContentTask> findByRequestId(String requestId);

    List<VendorContentTask> findByMergeId(String mergeId);

    @Modifying
    @Transactional
    @Query(value = "select * from vendor_task t  where t.item_id in ?1", nativeQuery = true)
    List<VendorContentTask> findByItemIdList(List<String> itemIds);


    @Modifying
    @Transactional
    @Query(value = "delete from vendor_task where create_time <?1", nativeQuery = true)
    void clear(Date time);

    @Modifying
    @Transactional
    @Query(value = "select * from vendor_task  where create_time <?1 limit ?2", nativeQuery = true)
    List<VendorContentTask> findByCreateTimeBeforeAndLimit(Date time, int num);

    List<VendorContentTask> findByCreateTimeBefore(Date time);

    VendorContentTask findByTaskId(String taskId);
}
