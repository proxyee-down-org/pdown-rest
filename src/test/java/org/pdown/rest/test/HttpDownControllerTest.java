package org.pdown.rest.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.entity.TaskInfo;
import org.pdown.core.util.ByteUtil;
import org.pdown.core.util.FileUtil;
import org.pdown.core.util.OsUtil;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.entity.HttpResult;
import org.pdown.rest.form.CreateTaskForm;
import org.pdown.rest.form.HttpRequestForm;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.test.server.RangeDownTestServer;
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
public class HttpDownControllerTest {

  private static final String TEST_DIR = System.getProperty("user.dir") + "/target/test";
  private static final String TEST_BUILD_FILE = TEST_DIR + "/build.data";
  private static final String DOWN_FILE_NAME = "测试.data";
  private static final String DOWN_FILE = TEST_DIR + "/" + DOWN_FILE_NAME;

  @Autowired
  private MockMvc mockMvc;

  private int port;

  @Before
  public void init() throws Exception {
    FileUtil.createDirSmart(TEST_DIR);
    //delRecords();
    //build random file
    buildRandomFile(TEST_BUILD_FILE, 1024 * 1024 * 500L);
    //start test http org.pdown.rest.test.server
    port = OsUtil.getFreePort(8866);
    new RangeDownTestServer(TEST_BUILD_FILE).start(port);
    DownRestServer.init(null);
  }

  @After
  public void delRecords() throws IOException {
    String baseDir = System.getProperty("user.dir");
    //delete record
    FileUtil.deleteIfExists(baseDir + "/.records.inf");
    FileUtil.deleteIfExists(baseDir + "/.records.inf.bak");
    FileUtil.deleteIfExists(TEST_BUILD_FILE);
    FileUtil.deleteIfExists(DOWN_FILE);
  }

  @Test
  public void resolve() throws Exception {
    ObjectMapper objectMapper = ContentUtil.getObjectMapper();
    HttpRequestForm httpRequestForm = new HttpRequestForm();
    httpRequestForm.setUrl("http://127.0.0.1:" + port);
    mockMvc.perform(post("/api/resolve").contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(httpRequestForm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.fileName").exists())
        .andExpect(jsonPath("$.data.totalSize").exists())
        .andExpect(jsonPath("$.data.supportRange").value(true));
  }

  @Test
  public void down() throws Exception {
    ObjectMapper objectMapper = ContentUtil.getObjectMapper();
    CreateTaskForm createTaskForm = new CreateTaskForm();
    createTaskForm.setRequest(new HttpRequestForm("http://127.0.0.1:" + port, null, null));
    createTaskForm.setConfig(new HttpDownConfigInfo().setFilePath(TEST_DIR).setConnections(2));
    createTaskForm.setResponse(new HttpResponseInfo(DOWN_FILE_NAME));
    MvcResult result = mockMvc.perform(post("/tasks")
//        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(createTaskForm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").exists())
        .andReturn();
    HttpResult httpResult = objectMapper.readValue(result.getResponse().getContentAsString(), HttpResult.class);
    String taskId = httpResult.getData().toString();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    new Thread(() -> {
      TypeReference progressType = new TypeReference<HttpResult<List<TaskForm>>>() {
      };
      while (true) {
        try {
          MvcResult mvcResult = mockMvc.perform(get("/tasks/progress"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.data").isArray())
              .andReturn();
          HttpResult<List<TaskForm>> httpResultArray = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), progressType);
          List<TaskForm> list = httpResultArray.getData();
          if (list == null || list.size() == 0) {
            MvcResult taskResult = mockMvc.perform(get("/tasks/" + taskId)).andReturn();
            if (taskResult.getResponse().getStatus() == 200) {
              TypeReference taskType = new TypeReference<HttpResult<TaskForm>>() {
              };
              HttpResult<TaskForm> taskResultForm = objectMapper.readValue(taskResult.getResponse().getContentAsString(), taskType);
              TaskForm taskForm = taskResultForm.getData();
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
      countDownLatch.countDown();
    }).start();
    Thread.sleep(233);
    mockMvc.perform(put("/tasks/" + taskId + "/pause"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("success"));
    //Pause for 3 seconds
    Thread.sleep(3000);
    mockMvc.perform(put("/tasks/" + taskId + "/resume"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("success"));

    countDownLatch.await();
    //Compare MD5
    Assert.assertEquals(getMd5ByFile(new File(TEST_BUILD_FILE)), getMd5ByFile(new File(DOWN_FILE)));
  }

  @Test
  public void serverShutdown() throws Exception {
    ObjectMapper objectMapper = ContentUtil.getObjectMapper();
    CreateTaskForm createTaskForm = new CreateTaskForm();
    createTaskForm.setRequest(new HttpRequestForm("http://127.0.0.1:" + port, null, null));
    createTaskForm.setConfig(new HttpDownConfigInfo().setFilePath(TEST_DIR).setConnections(2));
    createTaskForm.setResponse(new HttpResponseInfo(DOWN_FILE_NAME));
    mockMvc.perform(post("/tasks")
        .content(objectMapper.writeValueAsString(createTaskForm)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").exists())
        .andReturn();
    Thread.sleep(2333);
    System.exit(1);
  }

  public static void buildRandomFile(String path, long size) throws IOException {
    File file = new File(path);
    if (file.exists()) {
      file.delete();
    }
    file.createNewFile();
    try (
        FileOutputStream outputStream = new FileOutputStream(file)
    ) {
      byte[] bts = new byte[8192];
      for (int i = 0; i < bts.length; i++) {
        bts[i] = (byte) (Math.random() * 255);
      }
      for (long i = 0; i < size; i += bts.length) {
        outputStream.write(bts);
      }
    }
  }

  private static String getMd5ByFile(File file) {
    InputStream fis;
    byte[] buffer = new byte[2048];
    int numRead;
    MessageDigest md5;

    try {
      fis = new FileInputStream(file);
      md5 = MessageDigest.getInstance("MD5");
      while ((numRead = fis.read(buffer)) > 0) {
        md5.update(buffer, 0, numRead);
      }
      fis.close();
      return md5ToString(md5.digest());
    } catch (Exception e) {
      return null;
    }
  }

  private static String md5ToString(byte[] md5Bytes) {
    StringBuffer hexValue = new StringBuffer();
    for (int i = 0; i < md5Bytes.length; i++) {
      int val = ((int) md5Bytes[i]) & 0xff;
      if (val < 16) {
        hexValue.append("0");
      }
      hexValue.append(Integer.toHexString(val));
    }
    return hexValue.toString();
  }
}
