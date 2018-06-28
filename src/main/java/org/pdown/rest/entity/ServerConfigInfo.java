package org.pdown.rest.entity;

import java.io.Serializable;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.proxy.ProxyConfig;

public class ServerConfigInfo extends HttpDownConfigInfo implements Serializable {

  private static final long serialVersionUID = 8851967460594279184L;
  /**
   * rest org.pdown.rest.test.server port
   */
  private long port;
  /**
   * Concurrent task downloads
   */
  private long taskLimit = 3;
  /**
   * Total speed limit,default 0B/S
   */
  private long totalSpeedLimit;
  /**
   * Sec proxy setting
   */
  private ProxyConfig proxyConfig;

  public long getPort() {
    return port;
  }

  public void setPort(long port) {
    this.port = port;
  }

  public long getTaskLimit() {
    return taskLimit;
  }

  public void setTaskLimit(long taskLimit) {
    this.taskLimit = taskLimit;
  }

  public long getTotalSpeedLimit() {
    return totalSpeedLimit;
  }

  public void setTotalSpeedLimit(long totalSpeedLimit) {
    this.totalSpeedLimit = totalSpeedLimit;
  }

  public ProxyConfig getProxyConfig() {
    return proxyConfig;
  }

  public void setProxyConfig(ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
  }
}
