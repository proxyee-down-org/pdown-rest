package org.pdown.rest.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.pdown.rest.base.exception.NotFoundException;
import org.pdown.rest.base.exception.ParameterException;
import org.pdown.rest.entity.HttpResult;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalErrorExceptionAdvice {

  private static Logger LOGGER = LoggerFactory.getLogger(GlobalErrorExceptionAdvice.class);

  @ExceptionHandler(ParameterException.class)
  @ResponseBody
  public ResponseEntity<HttpResult> handleBad(HttpServletResponse response, Exception e) {
    ParameterException exception = (ParameterException) e;
    return ResponseEntity.badRequest().body(new HttpResult()
        .code(exception.getCode())
        .msg("parameters error:" + exception.getMessage()));
  }

  @ExceptionHandler(JsonProcessingException.class)
  @ResponseBody
  public ResponseEntity<HttpResult> handleJsonParseError(HttpServletResponse response, Exception e) {
    return ResponseEntity.badRequest().body(new HttpResult().code(5001).msg("parameters error"));
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseBody
  public ResponseEntity<HttpResult> handleNotFoundError(HttpServletResponse response, Exception e) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<HttpResult> handleError(HttpServletResponse response, Exception e) {
    LOGGER.error("request error:", e);
    return new ResponseEntity<>(new HttpResult().code(5000).msg("server error"), HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
