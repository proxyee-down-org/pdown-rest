package org.pdown.rest.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.entity.HttpResult;
import org.pdown.rest.form.CreateTaskForm;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.test.common.TestDownEnvironment;
import org.pdown.rest.test.server.ProgressCallable;
import org.pdown.rest.test.util.TestUtil;
import org.pdown.rest.util.ContentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = DownRestServer.class)
@AutoConfigureMockMvc
public class HttpDownTest {

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
  public void down() throws Exception {
    ObjectMapper objectMapper = ContentUtil.getObjectMapper();
    CreateTaskForm createTaskForm = new CreateTaskForm();
    createTaskForm.setRequest(new HttpRequestForm("http://127.0.0.1:" + testEnvironment.getPort(), null, null));
    createTaskForm.setConfig(new HttpDownConfigInfo().setFilePath(testEnvironment.getTestDir()).setConnections(2));
    createTaskForm.setResponse(new HttpResponseInfo(testEnvironment.getDownFileName()));
    MvcResult result = mockMvc.perform(post("/tasks")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(createTaskForm)))
        .andExpect(status().isOk())
        .andReturn();
    TypeReference taskFormType = new TypeReference<TaskForm>() {
    };
    TaskForm taskForm = objectMapper.readValue(result.getResponse().getContentAsString(), taskFormType);
    String taskId = taskForm.getId();
    Future future = Executors.newCachedThreadPool().submit(new ProgressCallable(mockMvc, taskId));
    Thread.sleep(233);
    mockMvc.perform(put("/tasks/" + taskId + "/pause"))
        .andExpect(status().isOk());
    //Pause for 3 seconds
    Thread.sleep(3000);
    mockMvc.perform(put("/tasks/" + taskId + "/resume"))
        .andExpect(status().isOk());
    future.get();
    //Compare MD5
    Assert.assertEquals(TestUtil.getMd5ByFile(new File(testEnvironment.getBuildFilePath())),
        TestUtil.getMd5ByFile(new File(testEnvironment.getDownFilePath())));
  }


}
