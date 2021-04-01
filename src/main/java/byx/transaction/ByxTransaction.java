package byx.transaction;

import byx.transaction.annotation.Transactional;
import byx.transaction.core.TransactionInterceptor;
import byx.util.proxy.ProxyUtils;
import static byx.util.proxy.core.MethodMatcher.*;

/**
 * 事务增强工具类
 *
 * @author byx
 */
public class ByxTransaction {
    /**
     * 创建事务增强代理类
     * @param target 目标类
     */
    public static <T> T getProxy(T target) {
        return ProxyUtils.proxy(target,
                new TransactionInterceptor().when(hasAnnotation(Transactional.class)));
    }
}
