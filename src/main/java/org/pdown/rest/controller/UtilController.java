package org.pdown.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.HttpServletRequest;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.exception.BootstrapResolveException;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.rest.base.exception.ParameterException;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.ResolveForm;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("util")
public class UtilController {

  /*
    Resolve request
     */
  @PutMapping("resolve")
  public ResponseEntity resolve(HttpServletRequest request) throws Exception {
    NioEventLoopGroup loopGroup = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      HttpRequestForm requestForm = mapper.readValue(request.getInputStream(), HttpRequestForm.class);
      if (StringUtils.isEmpty(requestForm.getUrl())) {
        throw new ParameterException(4001, "URL can't be empty");
      }
      HttpRequestInfo httpRequestInfo = HttpDownUtil.buildRequest(requestForm.getMethod(), requestForm.getUrl(), requestForm.getHeads(), requestForm.getBody());
      loopGroup = new NioEventLoopGroup(1);
      try {
        HttpResponseInfo httpResponseInfo = HttpDownUtil.getHttpResponseInfo(
            httpRequestInfo,
            null,
            ConfigContent.getInstance().get().getProxyConfig(),
            loopGroup
        );
        ResolveForm resolveForm = new ResolveForm(HttpRequestForm.parse(httpRequestInfo), httpResponseInfo);
        return ResponseEntity.ok(resolveForm);
      } catch (BootstrapResolveException exception) {
        throw new ParameterException(4002, exception.getMessage());
      } catch (TimeoutException exception) {
        throw new ParameterException(4003, exception.getMessage());
      }
    } finally {
      if (loopGroup != null) {
        loopGroup.shutdownGracefully();
      }
    }
  }
}
