package org.pdown.rest.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.test.common.TestDownEnvironment;
import org.pdown.rest.util.ContentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = DownRestServer.class)
@AutoConfigureMockMvc
public class UtilTest {

  private TestDownEnvironment testEnvironment = new TestDownEnvironment();

  @Autowired
  private MockMvc mockMvc;

  @Before
  public void start() throws Exception {
    testEnvironment.start();
  }

  @After
  public void clean() throws IOException {
    testEnvironment.clean();
  }

  @Test
  public void resolve() throws Exception {
    ObjectMapper objectMapper = ContentUtil.getObjectMapper();
    HttpRequestForm httpRequestForm = new HttpRequestForm();
    httpRequestForm.setUrl("http://127.0.0.1:" + testEnvironment.getPort());
    mockMvc.perform(post("/util/resolve").contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(httpRequestForm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").exists())
        .andExpect(jsonPath("$.totalSize").exists())
        .andExpect(jsonPath("$.supportRange").value(true));
  }
}
