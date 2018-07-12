package org.pdown.rest.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.proxy.ProxyConfig;
import org.pdown.core.proxy.ProxyType;

public class ServerConfigInfo extends HttpDownConfigInfo implements Serializable {

  private static final long serialVersionUID = 8851967460594279184L;
  /**
   * rest server port
   */
  private long port;
  /**
   * Concurrent task downloads
   */
  private int taskLimit = 3;
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

  public static void main(String[] args) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    ServerConfigInfo serverConfigInfo = new ServerConfigInfo();
    serverConfigInfo.setProxyConfig(new ProxyConfig(ProxyType.SOCKS5, "127.0.0.1", 1080));
    System.out.println(objectMapper.writeValueAsString(new HttpResult<>().data(serverConfigInfo)));
  }
}
