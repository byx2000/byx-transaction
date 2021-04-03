import byx.transaction.BaseTest;
import byx.transaction.TransactionManager;
import byx.transaction.annotation.Transactional;
import byx.transaction.core.PropagationBehavior;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionManagerTest3 extends BaseTest {
    public static class Service {
        @Transactional
        public void service1(boolean raiseException) {
            service2(raiseException);
        }

        @Transactional(propagationBehavior = PropagationBehavior.PROPAGATION_SUPPORTS)
        public void service2(boolean raiseException) {
            jdbcUtils.update("update A set value = value - 10");
            if (raiseException) {
                int a = 1 / 0;
            }
            jdbcUtils.update("update B set value = value + 10");
        }
    }

    @Test
    public void test() {
        Service service = TransactionManager.getProxy(jdbcUtils, new Service());

        service.service1(true);
        assertEquals(100, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(0, jdbcUtils.querySingleValue("select value from B", Integer.class));

        service.service1(false);
        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        jdbcUtils.update("update A set value = 100");
        jdbcUtils.update("update B set value = 0");

        try {
            service.service2(true);
        } catch (Exception ignored) {}

        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(0, jdbcUtils.querySingleValue("select value from B", Integer.class));

        jdbcUtils.update("update A set value = 100");
        jdbcUtils.update("update B set value = 0");

        service.service2(false);
        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        jdbcUtils.update("update A set value = 100");
        jdbcUtils.update("update B set value = 0");
    }
}
