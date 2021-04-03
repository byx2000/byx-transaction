package byx.transaction;

import byx.transaction.annotation.Transactional;
import byx.transaction.core.PropagationBehavior;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionManagerTest2 extends BaseTest {
    public static class Service {
        @Transactional
        public void service1() {
            jdbcUtils.update("update A set value = value - 10");
            service2();
        }

        @Transactional
        public void service2() {
            jdbcUtils.update("update B set value = value + 10");
        }

        @Transactional
        public void service3() {
            jdbcUtils.update("update A set value = value - 10");
            service4();
        }

        @Transactional
        public void service4() {
            int a = 1 / 0;
            jdbcUtils.update("update B set value = value + 10");
        }

        @Transactional
        public void service5() {
            jdbcUtils.update("update A set value = value - 10");
            service6();
            int a = 1 / 0;
        }

        @Transactional(propagationBehavior = PropagationBehavior.PROPAGATION_REQUIRED)
        public void service6() {
            jdbcUtils.update("update B set value = value + 10");
        }

        @Transactional
        public void service7() {
            jdbcUtils.update("update A set value = 100");
            service8();
        }

        @Transactional
        public void service8() {
            jdbcUtils.update("update B set value = 0");
        }
    }

    @Test
    public void test() {
        Service service = TransactionManager.getProxy(jdbcUtils, new Service());

        service.service1();
        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        System.out.println("-------------------------");

        service.service3();
        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        System.out.println("-------------------------");

        service.service5();
        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        System.out.println("-------------------------");

        service.service7();
    }
}
