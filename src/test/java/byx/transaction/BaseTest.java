package byx.transaction;

import byx.util.jdbc.JdbcUtils;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

public class BaseTest {
    protected static final JdbcUtils jdbcUtils = new JdbcUtils(getDataSource());

    private static DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite::resource:test.db");
        dataSource.setUsername("");
        dataSource.setPassword("");
        dataSource.setTestWhileIdle(false);
        return dataSource;
    }
}
