package org.pdown.rest.test.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.Callable;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.TaskInfo;
import org.pdown.core.util.ByteUtil;
import org.pdown.rest.entity.HttpResult;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.util.ContentUtil;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class ProgressCallable implements Callable {

  private MockMvc mockMvc;
  private String taskId;

  public ProgressCallable(MockMvc mockMvc, String taskId) {
    this.mockMvc = mockMvc;
    this.taskId = taskId;
  }

  @Override
  public Object call() throws Exception {
    TypeReference progressType = new TypeReference<List<TaskForm>>() {
    };
    ObjectMapper objectMapper = ContentUtil.getObjectMapper();
    while (true) {
      try {
        MvcResult mvcResult = mockMvc.perform(get("/tasks/progress"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andReturn();
        List<TaskForm> list = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), progressType);
        if (list == null || list.size() == 0) {
          MvcResult taskResult = mockMvc.perform(get("/tasks/" + taskId)).andReturn();
          if (taskResult.getResponse().getStatus() == 200) {
            TypeReference taskType = new TypeReference<TaskForm>() {
            };
            TaskForm taskForm = objectMapper.readValue(taskResult.getResponse().getContentAsString(), taskType);
            if (taskForm.getInfo().getStatus() == HttpDownStatus.DONE) {
              break;
            }
          }
        } else {
          TaskInfo taskInfo = list.get(0).getInfo();
          System.out.println("speed:" + ByteUtil.byteFormat(taskInfo.getSpeed()) + "/S");
        }
        Thread.sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
