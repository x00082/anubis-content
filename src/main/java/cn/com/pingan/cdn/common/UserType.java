package cn.com.pingan.cdn.common;

/**
 * @Classname UserType
 * @Description TODO
 * @Date 2020/10/30 13:54
 * @Created by Luj
 */
public enum UserType {
    /**
     * 主账户
     */
    Account,
    /**
     * 子账户
     */
    User;

    public static UserType of(String value) {
        for(UserType ut : UserType.values()) {
            if(ut.name().equals(value)) return ut;
        }
        return UserType.Account;
    }
}
