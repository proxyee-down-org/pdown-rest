package org.pdown.rest.base.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.pdown.rest.util.ContentUtil;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class WebConfig {

  @Bean
  public HttpMessageConverters fastJsonMessageConverters() {
    return new HttpMessageConverters(new MappingJackson2HttpMessageConverter(ContentUtil.getObjectMapper()));
  }

  private static class User {

    private String name;
    private transient int age;
    private WebConfig.Book book;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public WebConfig.Book getBook() {
      return book;
    }

    public void setBook(WebConfig.Book book) {
      this.book = book;
    }
  }

  private static class Book {

    private transient String name;
    private int page;

    public Book(String name, int page) {
      this.name = name;
      this.page = page;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getPage() {
      return page;
    }

    public void setPage(int page) {
      this.page = page;
    }
  }

  public static void main(String[] args) throws JsonProcessingException {
    User user = new User();
    user.setName("aaa");
    user.setAge(11);
    user.setBook(new Book("book",122));
    System.out.println(ContentUtil.getObjectMapper().writeValueAsString(user));
  }
}
