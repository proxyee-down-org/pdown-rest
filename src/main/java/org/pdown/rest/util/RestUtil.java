package org.pdown.rest.util;

import org.pdown.rest.entity.HttpResult;
import org.springframework.http.ResponseEntity;

public class RestUtil {

  public static ResponseEntity<HttpResult> buildResponse(String msg, Object data) {
    return ResponseEntity.ok().body(new HttpResult().data(data).msg(msg));
  }

  public static ResponseEntity<HttpResult> buildResponse(Object data) {
    return buildResponse(null, data);
  }

  public static ResponseEntity<HttpResult> buildResponse() {
    return buildResponse("success", null);
  }
}
