package byx.transaction.core;

import byx.util.jdbc.JdbcUtils;
import byx.util.proxy.core.MethodInterceptor;
import byx.util.proxy.core.TargetMethod;

/**
 * 事务增强拦截器
 *
 * @author byx
 */
public class TransactionInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(TargetMethod targetMethod) {
        try {
            JdbcUtils.startTransaction();
            Object ret = targetMethod.invokeWithOriginalParams();
            JdbcUtils.commit();
            return ret;
        } catch (Exception e) {
            JdbcUtils.rollback();
            return null;
        }
    }
}
