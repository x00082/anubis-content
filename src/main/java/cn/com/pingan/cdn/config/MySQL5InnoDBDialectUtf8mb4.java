package cn.com.pingan.cdn.config;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * @Classname MySQL5InnoDBDialectUtf8mb4
 * @Description TODO
 * @Date 2020/10/28 16:22
 * @Created by Luj
 */
public class MySQL5InnoDBDialectUtf8mb4 extends MySQL5InnoDBDialect {
    @Override
    public String getTableTypeString() {
        return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci";
    }
}
