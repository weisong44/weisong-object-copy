package com.weisong.common.util;

import org.hibernate.proxy.HibernateProxy;

public class CommonUtil {
    @SuppressWarnings("unchecked")
    static public <T> T unboxProxy(T candidate) {
        if (isProxy(candidate)) {
            return (T) ((HibernateProxy) candidate).getHibernateLazyInitializer().getImplementation();
        }
        return candidate;
    }

    static public boolean isProxy(Object candidate) {
        return candidate != null && candidate instanceof HibernateProxy;
    }

}
