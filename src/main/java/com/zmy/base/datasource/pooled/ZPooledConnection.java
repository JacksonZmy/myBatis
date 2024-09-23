package com.zmy.base.datasource.pooled;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
class ZPooledConnection implements InvocationHandler {

  private static final String CLOSE = "close";
  private static final Class<?>[] IFACES = new Class<?>[] { Connection.class };

  private final int hashCode;
  private final ZPooledDataSource dataSource;
  //  真正的数据库连接
  private final Connection realConnection;
  //  数据库连接的代理对象
  private final Connection proxyConnection;
  private long checkoutTimestamp; // 从连接池中取出该连接的时间戳
  private long createdTimestamp; // 该连接创建的时间戳
  private long lastUsedTimestamp; // 最后一次被使用的时间戳
  private int connectionTypeCode; // 又数据库URL、用户名和密码计算出来的hash值，可用于标识该连接所在的连接池
  // 连接是否有效的标志
  private boolean valid;

  /**
   * SimplePooledConnection 的构造函数，它使用传入的 Connection 和 PooledDataSource。
   *
   * @param connection - 要显示为池连接的连接
   * @param dataSource - connectiont 来自那个 dataSource
   */
  public ZPooledConnection(Connection connection, ZPooledDataSource dataSource) {
    this.hashCode = connection.hashCode();
    this.realConnection = connection;
    this.dataSource = dataSource;
    this.createdTimestamp = System.currentTimeMillis();
    this.lastUsedTimestamp = System.currentTimeMillis();
    this.valid = true;
    this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
  }

  /**
   * 使连接失效
   */
  public void invalidate() {
    valid = false;
  }

  /**
   * 查看连接是否可用
   *
   * @return 如果连接可用，则为 True
   */
  public boolean isValid() {
    return valid && realConnection != null && dataSource.pingConnection(this);
  }

  /**
   * 获取真正 connection 连接
   *
   */
  public Connection getRealConnection() {
    return realConnection;
  }

  /**
   * 获取代理的连接
   *
   */
  public Connection getProxyConnection() {
    return proxyConnection;
  }

  /**
   * 获取真实连接的哈希码（如果为 null，则为 0）。
   *
   */
  public int getRealHashCode() {
    return realConnection == null ? 0 : realConnection.hashCode();
  }

  /**
   * 获取 connection 类型 (基于 url + user + password).
   *
   */
  public int getConnectionTypeCode() {
    return connectionTypeCode;
  }

  /**
   * 设置 connection 类型
   *
   */
  public void setConnectionTypeCode(int connectionTypeCode) {
    this.connectionTypeCode = connectionTypeCode;
  }

  /**
   * 获取 connection 的创建时间
   *
   */
  public long getCreatedTimestamp() {
    return createdTimestamp;
  }

  /**
   * 设置 connection 的创建时间
   *
   */
  public void setCreatedTimestamp(long createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  /**
   * 获取 connection 的最后使用时间
   *
   */
  public long getLastUsedTimestamp() {
    return lastUsedTimestamp;
  }

  /**
   * 设置 connection 的最后使用时间
   *
   */
  public void setLastUsedTimestamp(long lastUsedTimestamp) {
    this.lastUsedTimestamp = lastUsedTimestamp;
  }

  /**
   * 获取 connection 最后一次使用到现在的时间间隔
   *
   */
  public long getTimeElapsedSinceLastUse() {
    return System.currentTimeMillis() - lastUsedTimestamp;
  }

  /**
   * 获取 connection 的存活时间
   *
   */
  public long getAge() {
    return System.currentTimeMillis() - createdTimestamp;
  }

  /**
   * 获取此连接 checkout 的时间戳。
   *
   */
  public long getCheckoutTimestamp() {
    return checkoutTimestamp;
  }

  /**
   * 设置此连接 checkout 的时间戳。
   *
   */
  public void setCheckoutTimestamp(long timestamp) {
    this.checkoutTimestamp = timestamp;
  }

  /**
   * 获取 connection 已经 checkout 多长时间
   *
   */
  public long getCheckoutTime() {
    return System.currentTimeMillis() - checkoutTimestamp;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  /**
   * 允许将此连接与另一个连接进行比较。
   *
   * @param obj - 另一个测试的连接
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ZPooledConnection) {
      return realConnection.hashCode() == ((ZPooledConnection) obj).realConnection.hashCode();
    } else if (obj instanceof Connection) {
      return hashCode == obj.hashCode();
    } else {
      return false;
    }
  }

  /**
   * 对于 InvocationHandler 实现是必需的。
   *
   * @param proxy  - not used
   * @param method - 要执行的方法
   * @param args   - 要传递给 Method 的参数
   * @see InvocationHandler#invoke(Object, Method, Object[])
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    if (CLOSE.equals(methodName)) {
      // 如果是 close 方法被执行则将连接放回连接池中，而不是真正的关闭数据库连接
      dataSource.pushConnection(this);
      return null;
    }
    try {
      if (!Object.class.equals(method.getDeclaringClass())) {
        // 通过上面的 valid 字段来检测 连接是否有效
        checkConnection();
      }
      // 调用真正数据库连接对象的对应方法
      return method.invoke(realConnection, args);
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }

  }

  private void checkConnection() throws SQLException {
    if (!valid) {
      throw new SQLException("访问 PooledConnection 时出错。连接无效。");
    }
  }

}
