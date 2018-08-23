package org.pdown.rest.entity;

import java.io.Serializable;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.proxy.ProxyConfig;

public class ServerConfigInfo extends HttpDownConfigInfo implements Serializable {

  private static final long serialVersionUID = 8851967460594279184L;

  public static transient String baseDir;

  /**
   * rest server port
   */
  private int port;
  /**
   * Concurrent task downloads
   */
  private int taskLimit;
  /**
   * Total speed limit,default 0B/S
   */
  private long totalSpeedLimit;
  /**
   * Sec proxy setting
   */
  private ProxyConfig proxyConfig;

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getTaskLimit() {
    return taskLimit;
  }

  public void setTaskLimit(int taskLimit) {
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
