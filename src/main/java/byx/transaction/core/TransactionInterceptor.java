package byx.transaction.core;

import byx.transaction.annotation.Transactional;
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
        Transactional transactional = targetMethod.getSignature().getAnnotation(Transactional.class);
        Propagation behavior = transactional.propagation();
        return switch (behavior) {
            case PROPAGATION_REQUIRED -> interceptPropagationRequired(targetMethod);
            case PROPAGATION_SUPPORTS -> interceptPropagationSupports(targetMethod);
        };
    }

    /**
     * PROPAGATION_REQUIRED传播特性
     * 如果当前在事务中，则直接使用当前事务
     * 否则新开一个事务
     */
    private Object interceptPropagationRequired(TargetMethod targetMethod) {
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

    /**
     * PROPAGATION_SUPPORTS传播特性
     * 如果当前在事务中，则直接使用当前事务
     * 否则不使用事务
     */
    private Object interceptPropagationSupports(TargetMethod targetMethod) {
        return targetMethod.invokeWithOriginalParams();
    }
}
