package org.pdown.rest.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdown.core.util.FileUtil;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.HttpDownContent;
import org.pdown.rest.entity.ServerConfigInfo;
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
public class ConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @Before
  public void init(){
    //org.pdown.rest.test.server config
    ConfigContent.getInstance().load();
    //download content
    HttpDownContent.getInstance().load();
  }

  @After
  public void clean() throws IOException {
    String baseDir = System.getProperty("user.dir");
    FileUtil.deleteIfExists(baseDir + "/config.inf");
    FileUtil.deleteIfExists(baseDir + "/.config.inf.bak");
  }

  @Test
  public void setConfig() throws Exception {
    ServerConfigInfo serverConfigInfo = new ServerConfigInfo();
    serverConfigInfo.setConnections(32);
    serverConfigInfo.setTaskLimit(5);
    ObjectMapper objectMapper = new ObjectMapper();
    mockMvc.perform(put("/config").content(objectMapper.writeValueAsString(serverConfigInfo)).contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    mockMvc.perform(get("/config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.connections").value(32))
        .andExpect(jsonPath("$.taskLimit").value(5));
  }
}
