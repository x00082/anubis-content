package cn.com.pingan.cdn.repository.custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Classname MyJpaRepository
 * @Description TODO
 * @Date 2020/11/24 11:45
 * @Created by Luj
 */
@NoRepositoryBean
public interface MyJpaRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T>, PagingAndSortingRepository<T, ID> {

    public <S extends T> Iterable<S> batchSave(Iterable<S> entities);

    public <S extends T> Iterable<S> batchUpdate(Iterable<S> entities);
}
