package com.zmy.base.datasource.pooled;

import org.apache.ibatis.datasource.pooled.PooledDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于管理PooledConnection对象状态的组件
 */
public class ZPoolState {

  protected ZPooledDataSource dataSource;
  // 空闲的连接
  protected final List<ZPooledConnection> idleConnections = new ArrayList<>();
  // 活跃的连接
  protected final List<ZPooledConnection> activeConnections = new ArrayList<>();
  protected long requestCount = 0; // 请求数据库连接的次数
  protected long accumulatedRequestTime = 0; // 获取连接累计的时间
  // CheckoutTime 表示应用从连接池中取出来，到归还连接的时长
  // accumulatedCheckoutTime 记录了所有连接累计的CheckoutTime时长
  protected long accumulatedCheckoutTime = 0;
  // 当连接长时间没有归还连接时，会被认为该连接超时
  // claimedOverdueConnectionCount 记录连接超时的个数
  protected long claimedOverdueConnectionCount = 0;
  // 累计超时时间
  protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
  // 累计等待时间
  protected long accumulatedWaitTime = 0;
  // 等待次数
  protected long hadToWaitCount = 0;
  // 无效连接数
  protected long badConnectionCount = 0;

  public ZPoolState(ZPooledDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public synchronized long getRequestCount() {
    return requestCount;
  }

  public synchronized long getAverageRequestTime() {
    return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
  }

  public synchronized long getAverageWaitTime() {
    return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;

  }

  public synchronized long getHadToWaitCount() {
    return hadToWaitCount;
  }

  public synchronized long getBadConnectionCount() {
    return badConnectionCount;
  }

  public synchronized long getClaimedOverdueConnectionCount() {
    return claimedOverdueConnectionCount;
  }

  public synchronized long getAverageOverdueCheckoutTime() {
    return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
  }

  public synchronized long getAverageCheckoutTime() {
    return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
  }


  public synchronized int getIdleConnectionCount() {
    return idleConnections.size();
  }

  public synchronized int getActiveConnectionCount() {
    return activeConnections.size();
  }

  @Override
  public synchronized String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("\n===CONFINGURATION==============================================");
    builder.append("\n jdbcDriver                     ").append(dataSource.getDriver());
    builder.append("\n jdbcUrl                        ").append(dataSource.getUrl());
    builder.append("\n jdbcUsername                   ").append(dataSource.getUsername());
    builder.append("\n jdbcPassword                   ").append(dataSource.getPassword() == null ? "NULL" : "************");
    builder.append("\n poolMaxActiveConnections       ").append(dataSource.poolMaximumActiveConnections);
    builder.append("\n poolMaxIdleConnections         ").append(dataSource.poolMaximumIdleConnections);
    builder.append("\n poolMaxCheckoutTime            ").append(dataSource.poolMaximumCheckoutTime);
    builder.append("\n poolTimeToWait                 ").append(dataSource.poolTimeToWait);
    builder.append("\n poolPingEnabled                ").append(dataSource.poolPingEnabled);
    builder.append("\n poolPingQuery                  ").append(dataSource.poolPingQuery);
    builder.append("\n poolPingConnectionsNotUsedFor  ").append(dataSource.poolPingConnectionsNotUsedFor);
    builder.append("\n ---STATUS-----------------------------------------------------");
    builder.append("\n activeConnections              ").append(getActiveConnectionCount());
    builder.append("\n idleConnections                ").append(getIdleConnectionCount());
    builder.append("\n requestCount                   ").append(getRequestCount());
    builder.append("\n averageRequestTime             ").append(getAverageRequestTime());
    builder.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
    builder.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
    builder.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
    builder.append("\n hadToWait                      ").append(getHadToWaitCount());
    builder.append("\n averageWaitTime                ").append(getAverageWaitTime());
    builder.append("\n badConnectionCount             ").append(getBadConnectionCount());
    builder.append("\n===============================================================");
    return builder.toString();
  }

}
