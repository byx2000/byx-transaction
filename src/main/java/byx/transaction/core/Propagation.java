package byx.transaction.core;

/**
 * 事务传播行为
 *
 * @author byx
 */
public enum Propagation {
    /**
     * 如果当前在事务中，则直接使用当前事务，否则新开一个事务
     */
    PROPAGATION_REQUIRED,
    /**
     * 如果当前在事务中，则直接使用当前事务，否则不使用事务
     */
    PROPAGATION_SUPPORTS
}
