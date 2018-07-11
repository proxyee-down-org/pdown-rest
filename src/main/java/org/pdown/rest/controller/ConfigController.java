package org.pdown.rest.controller;

import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.entity.HttpResult;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.util.RestUtil;
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
  public ResponseEntity<HttpResult> getConfig() throws Exception {
    return RestUtil.buildResponse(ConfigContent.getInstance().get());
  }

  /*
   *  Set server config info.
   */
  @PutMapping
  public ResponseEntity<HttpResult> setConfig(@RequestBody ServerConfigInfo serverConfigInfo) {
    BeanUtils.copyProperties(serverConfigInfo, ConfigContent.getInstance().get());
    ConfigContent.getInstance().save();
    return RestUtil.buildResponse();
  }

}
