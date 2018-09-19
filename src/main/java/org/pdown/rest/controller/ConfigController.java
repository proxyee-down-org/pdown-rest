package org.pdown.rest.controller;

import org.pdown.core.constant.HttpDownStatus;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.entity.ServerConfigInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config")
public class ConfigController {

  /*
   *  Get server config info.
   */
  @GetMapping
  public ResponseEntity getConfig() throws Exception {
    return ResponseEntity.ok(ConfigContent.getInstance().get());
  }

  /*
   *  Set server config info.
   */
  @PutMapping
  public ResponseEntity setConfig(@RequestBody ServerConfigInfo serverConfigInfo) {
    ServerConfigInfo beforeConfig = ConfigContent.getInstance().get();
    if (beforeConfig != null) {
      boolean speedChange = beforeConfig.getSpeedLimit() != serverConfigInfo.getSpeedLimit()
          || beforeConfig.getTotalSpeedLimit() != serverConfigInfo.getTotalSpeedLimit();
      BeanUtils.copyProperties(serverConfigInfo, ConfigContent.getInstance().get());
      if (speedChange) {
        PersistenceHttpDownCallback.calcSpeedLimit();
      }
    }
    HttpDownContent.getInstance().get().values().stream()
        .filter(bootstrap -> bootstrap.getTaskInfo().getStatus() != HttpDownStatus.DONE)
        .forEach(bootstrap -> bootstrap.setProxyConfig(serverConfigInfo.getProxyConfig()));
    ConfigContent.getInstance().save();
    return ResponseEntity.ok(null);
  }

}
