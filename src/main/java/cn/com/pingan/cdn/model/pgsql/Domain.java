/**   
 * @Project: anubis-content
 * @File: Domain.java 
 * @Package cn.com.pingan.cdn.model 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 下午2:08:17 
 */
package cn.com.pingan.cdn.model.pgsql;

import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: Domain 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 下午2:08:17 
 *  
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Cacheable(value = false)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "domain", schema = "base")
public class Domain {

        @Id
        @SequenceGenerator(name = "DOMAIN_ID_GENERATOR", sequenceName = "domain_id_seq", allocationSize = 1, schema = "base")
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOMAIN_ID_GENERATOR")
        private Long id;
        @Column(name = "domain")
        private String domain;
        
        @Column(name = "status")
        private String status;
        @Column(name = "locked")
        private boolean locked;
        /**
         * 域名类型: 泛域名和普通域名
         */
        @Column(name = "type")
        private String type;
        @Column(name = "line_id")
        private String lineId;
        
        /**
         * 下发的厂商[ 'qiniu','tencent' ]
         */
        @Column(name = "expected_vendor")
        private String expectedVendor;
        /**
         * 调度的厂商 [ 'qiniu' ]
         */
        @Column(name = "actual_vendor")
        private String acutalVendor;
        @Column(name = "approval_status")
        private String approvalStatus;
        @Column(name = "uuid")
        private String uuid;
        
        @Column(name = "user_code")
        private String userCode;
        @Column(name = "task_name")
        private String taskName;


        @Column(name = "message")
        private String message;

        public void setExpectedBaseLine(List<String> baseLines) throws RestfulException {
            this.expectedVendor = Utils.objectToString(baseLines, new ArrayList<String>().getClass());
        }

        public List<String> getExpectedBaseLine() throws RestfulException {
            return Utils.stringToArrayObject(this.expectedVendor, String.class);
        }

        public void setActualBaseLine(List<String> baseLines) throws RestfulException {
            this.acutalVendor = Utils.objectToString(baseLines, new ArrayList<String>().getClass());
        }

        public List<String> getActualBaseLine() throws RestfulException {
            return Utils.stringToArrayObject(this.acutalVendor, String.class);
        }

}
