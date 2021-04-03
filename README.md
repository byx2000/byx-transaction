# ByxTransaction

ByxTransaction是一个模仿Spring事务管理的声明式事务框架，实现了Spring中的`Transactional`声明式事务注解的使用方式，支持`PROPAGATION_REQUIRED`和`PROPAGATION_SUPPORTS`两种事务传播行为。

## Maven引入

```xml
<repositories>
    <repository>
        <id>byx-maven-repo</id>
        <name>byx-maven-repo</name>
        <url>https://gitee.com/byx2000/maven-repo/raw/master/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>byx.transaction</groupId>
        <artifactId>byx-transaction</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## 使用示例

下面通过一个转账案例来快速了解ByxTransaction的使用。

假设数据库中有一张account表，表中有id和balance两个字段，id表示账户id，balance表示当前账户余额。

表中的数据如下：

|id|balance|
|---|---|
|1|1000|
|2|0|

表中有两个id分别为1和2的账户，账户1的余额为1000，账户2的余额为0。

`AccountDao`和`AccountDaoImpl`：

```java
public interface AccountDao {
    void transferIn(Integer id, Integer amount);
    void transferOut(Integer id, Integer amount);
}

public class AccountDaoImpl implements AccountDao {
    private final JdbcUtils jdbcUtils;

    public AccountDaoImpl(JdbcUtils jdbcUtils) {
        this.jdbcUtils = jdbcUtils;
    }

    @Override
    public void transferIn(Integer id, Integer amount) {
        jdbcUtils.update("update account set balance = balance + ? where id = ?",
                amount, id);
    }

    @Override
    public void transferOut(Integer id, Integer amount) {
        jdbcUtils.update("update account set balance = balance - ? where id = ?",
                amount, id);
    }
}
```

`transferIn`为转入操作，`transferOut`为转出操作。

`AccountDaoImpl`中使用`JdbcUtils`来操作数据库。`JdbcUtils`是一个小巧的JDBC工具类，详情请看[JdbcUtils](https://github.com/byx2000/JdbcUtils)。

`AccountService`和`AccountServiceImpl`：

```java
public interface AccountService {
    void transfer(Integer outId, Integer inId, Integer amount);
}

public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    @Transactional
    public void transfer(Integer outId, Integer inId, Integer amount) {
        accountDao.transferOut(outId, amount);
        accountDao.transferIn(inId, amount);
    }
}
```

`transfer(out, in, amount)`表示从id为out的账户转账amount给id为in的账户。

为简单起见，这里省略了转帐前的验证操作（如判断余额是否充足等）。

`AccountServiceImpl`的`transfer`方法被标注了`Transactional`注解，表示对当前方法开启事务支持。

main函数的代码如下：

```java
public class Main {
    private static final JdbcUtils jdbcUtils = new JdbcUtils(getDataSource());

    private static DataSource getDataSource() {
        // 返回一个DataSource ...
    }

    public static void main(String[] args) {
        AccountService accountService = new AccountServiceImpl(new AccountDaoImpl(jdbcUtils));
        accountService = TransactionManager.getProxy(jdbcUtils, accountService);
        accountService.transfer(1, 2, 100);
    }
}
```

下面这行代码用于创建事务增强的代理对象：

```java
accountService = TransactionManager.getProxy(jdbcUtils, accountService);
```

执行main函数后，可以看到数据库中的数据被成功更新了：

|id|balance|
|---|---|
|1|900|
|2|100|

以上是事务成功提交的情况，接下来演示一下事务回滚的情况。

首先把数据库中的数据恢复成下面这样：

|id|balance|
|---|---|
|1|1000|
|2|0|

然后修改`AccountServiceImpl`中的`transfer`方法：

```java
@Override
@Transactional
public void transfer(Integer outId, Integer inId, Integer amount) {
    accountDao.transferOut(outId, amount);
    int a = 1 / 0;
    accountDao.transferIn(inId, amount);
}
```

`int a = 1 / 0`用于故意引发一个`RuntimeException`，模拟事务中途失败的场景。

再次执行main函数，然后查看数据库：

|id|balance|
|---|---|
|1|1000|
|2|0|

可以发现，数据库中的数据并没有变化，这说明事务已成功回滚。

## 事务传播行为

ByxTransaction支持两种事务传播行为：

* `PROPAGATION_REQUIRED`：如果当前已存在事务，则直接使用当前事务，否则新开一个事务
* `PROPAGATION_SUPPORTS`：如果当前已存在事务，则直接使用当前事务，否则不使用事务

标注`@Transactional`注解时通过设置`propagationBehavior`属性来指定事务传播行为。

```java
// 指定PROPAGATION_REQUIRED传播行为
@Transactional(propagation = Propagation.PROPAGATION_REQUIRED)

// 指定PROPAGATION_SUPPORTS传播行为
@Transactional(propagation = Propagation.PROPAGATION_SUPPORTS)
```