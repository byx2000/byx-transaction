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
    private final JdbcUtils jdbcUtils;

    public TransactionInterceptor(JdbcUtils jdbcUtils) {
        this.jdbcUtils = jdbcUtils;
    }

    @Override
    public Object intercept(TargetMethod targetMethod) {
        // PROPAGATION_REQUIRED传播特性
        // 如果当前在事务中，则直接使用当前事务
        // 否则新开一个事务
        if (jdbcUtils.inTransaction()) {
            return targetMethod.invokeWithOriginalParams();
        } else {
            try {
                jdbcUtils.startTransaction();
                Object ret = targetMethod.invokeWithOriginalParams();
                jdbcUtils.commit();
                return ret;
            } catch (Exception e) {
                jdbcUtils.rollback();
                return null;
            }
        }
    }
}
