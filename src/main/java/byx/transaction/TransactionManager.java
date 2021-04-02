package byx.transaction;

import byx.transaction.annotation.Transactional;
import byx.transaction.core.TransactionInterceptor;
import byx.util.jdbc.JdbcUtils;
import byx.util.proxy.ProxyUtils;
import static byx.util.proxy.core.MethodMatcher.*;

/**
 * 事务管理器
 *
 * @author byx
 */
public class TransactionManager {
    /**
     * 创建事务增强代理类
     * @param target 目标类
     */
    public static <T> T getProxy(JdbcUtils jdbcUtils, T target) {
        return ProxyUtils.proxy(target,
                new TransactionInterceptor(jdbcUtils).when(hasAnnotation(Transactional.class)));
    }
}
