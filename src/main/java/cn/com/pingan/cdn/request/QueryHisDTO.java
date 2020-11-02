package cn.com.pingan.cdn.request;

import lombok.Data;

import java.util.List;

/**
 * @Classname QueryHisDTO
 * @Description TODO
 * @Date 2020/10/30 11:37
 * @Created by Luj
 */
@Data
public class QueryHisDTO<T> {
    private int totalRecords;
    private int pageIndex;
    private int pageSize;
    private List<T> data;
}
