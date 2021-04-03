package byx.transaction.annotation;

import byx.transaction.core.PropagationBehavior;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解加在service方法上，表示对该方法启用事务
 *
 * @author byx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transactional {
    /**
     * 事务传播行为
     */
    PropagationBehavior propagationBehavior() default PropagationBehavior.PROPAGATION_REQUIRED;
}
