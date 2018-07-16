package org.pdown.rest.base.config;

import java.nio.charset.Charset;
import org.pdown.rest.util.ContentUtil;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class WebConfig {

  @Bean
  public HttpMessageConverters fastJsonMessageConverters() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(ContentUtil.getObjectMapper());
    converter.setDefaultCharset(Charset.forName("UTF-8"));
    return new HttpMessageConverters(converter);
  }
}
