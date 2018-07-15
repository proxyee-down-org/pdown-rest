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
  public ResponseEntity<HttpResult> handleJsonParseError(HttpServletResponse response,Exception e) {
    return ResponseEntity.badRequest().body(new HttpResult()
        .code(4000)
        .msg("parameters parse error"));
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseBody
  public ResponseEntity handleNotFoundError(HttpServletResponse response, Exception e) {
    return new ResponseEntity(e.getMessage(),HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity handleError(HttpServletResponse response, Exception e) {
    LOGGER.error("request error:", e);
    return new ResponseEntity("server error",HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
