package byx.transaction;

import byx.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionManagerTest1 extends BaseTest {
    public interface UserDao {
        void dao1();
        void dao2();
        void dao3();
        void dao4();
    }

    public static class UserDaoImpl implements UserDao {
        @Override
        public void dao1() {
            jdbcUtils.update("update A set value = value - 10");
        }

        @Override
        public void dao2() {
            jdbcUtils.update("update A set value = value + 10");
        }

        @Override
        public void dao3() {
            jdbcUtils.update("update B set value = value - 10");
        }

        @Override
        public void dao4() {
            jdbcUtils.update("update B set value = value + 10");
        }
    }

    public interface UserService {
        Integer service1();
        Integer service2();
        void service3();
        void service4();
    }

    public static class UserServiceImpl implements UserService {
        private final UserDao userDao = new UserDaoImpl();

        @Override
        @Transactional
        public Integer service1() {
            userDao.dao1();
            int a = 1 / 0;
            userDao.dao4();
            return 123;
        }

        @Override
        @Transactional
        public Integer service2() {
            userDao.dao1();
            userDao.dao4();
            return 456;
        }

        @Override
        public void service3() {
            userDao.dao2();
            int a = 1 / 0;
            userDao.dao3();
        }

        @Override
        @Transactional
        public void service4() {
            userDao.dao2();
            int a = 1 / 0;
            userDao.dao3();
        }
    }

    @Test
    public void test() {
        assertEquals(100, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(0, jdbcUtils.querySingleValue("select value from B", Integer.class));

        UserService userService = TransactionManager.getProxy(jdbcUtils, new UserServiceImpl());

        assertNull(userService.service1());
        assertEquals(100, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(0, jdbcUtils.querySingleValue("select value from B", Integer.class));

        assertEquals(456, userService.service2());
        assertEquals(90, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        assertThrows(RuntimeException.class, userService::service3);
        assertEquals(100, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        userService.service4();
        assertEquals(100, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(10, jdbcUtils.querySingleValue("select value from B", Integer.class));

        jdbcUtils.update("update B set value = value - 10");
        assertEquals(100, jdbcUtils.querySingleValue("select value from A", Integer.class));
        assertEquals(0, jdbcUtils.querySingleValue("select value from B", Integer.class));
    }
}
