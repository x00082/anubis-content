package cn.com.pingan.cdn.repository.mysql;

import cn.com.pingan.cdn.model.mysql.VendorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @Classname VendorTaskRepository
 * @Description TODO
 * @Date 2020/10/20 11:34
 * @Created by Luj
 */
@Repository
public interface VendorInfoRepository extends JpaRepository<VendorInfo, Long>, JpaSpecificationExecutor<VendorInfo> {

    VendorInfo findByVendor(String vendor);

}
