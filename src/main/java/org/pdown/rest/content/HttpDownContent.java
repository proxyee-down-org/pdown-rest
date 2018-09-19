package org.pdown.rest.content;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.constant.HttpDownStatus;
import org.pdown.core.entity.ChunkInfo;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.entity.TaskInfo;
import org.pdown.core.util.FileUtil;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.rest.base.content.PersistenceContent;
import org.pdown.rest.controller.PersistenceHttpDownCallback;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.form.TaskForm;
import org.pdown.rest.util.ContentUtil;

public class HttpDownContent extends PersistenceContent<Map<String, HttpDownBootstrap>, HttpDownContent> {

  private static final HttpDownContent INSTANCE = new HttpDownContent();

  public static HttpDownContent getInstance() {
    return INSTANCE;
  }

  @Override
  protected TypeReference type() {
    return new TypeReference<List<TaskForm>>() {
    };
  }

  @Override
  protected String savePath() {
    return ServerConfigInfo.baseDir + File.separator + ".records.dat";
  }

  @Override
  protected boolean isHidden() {
    return true;
  }

  @Override
  protected Map<String, HttpDownBootstrap> defaultValue() {
    return new LinkedHashMap<>();
  }

  public HttpDownContent put(String id, HttpDownBootstrap httpDownBootstrap) {
    content.put(id, httpDownBootstrap);
    return this;
  }

  public HttpDownBootstrap get(String id) {
    return content.get(id);
  }

  public HttpDownContent remove(String id) {
    content.remove(id);
    return this;
  }

  @Override
  public HttpDownContent load() {
    try {
      List<TaskForm> taskForms = ContentUtil.get(savePath(), type());
      if (taskForms != null && taskForms.size() > 0) {
        content = defaultValue();
        for (int i = 0; i < taskForms.size(); i++) {
          TaskForm taskForm = taskForms.get(i);
          HttpRequestInfo request = HttpDownUtil.buildRequest(taskForm.getRequest().getMethod(), taskForm.getRequest().getUrl(), taskForm.getRequest().getHeads(), taskForm.getRequest().getBody());
          TaskInfo taskInfo = null;
          if (taskForm.getInfo().getStatus() == HttpDownStatus.WAIT
              || taskForm.getInfo().getStatus() == HttpDownStatus.DONE) {
            taskInfo = taskForm.getInfo();
          } else {
            //读取任务下载进度
            try {
              taskInfo = ContentUtil.get(progressSavePath(taskForm.getConfig(), taskForm.getResponse()), TaskInfo.class);
            } catch (Exception e) {
            }
            if (taskInfo == null) {
              taskInfo = taskForm.getInfo();
              taskInfo.setStatus(HttpDownStatus.WAIT);
            }
            if (taskForm.getInfo().getStatus() != HttpDownStatus.WAIT
                && taskInfo.getStatus() != HttpDownStatus.DONE) {
              //下载完了但是记录文件没更新到则标记为下载完成，并删除记录文件
              if (taskInfo.getDownSize() >= taskForm.getResponse().getTotalSize()) {
                taskInfo.setStatus(HttpDownStatus.DONE);
                String progressPath = progressSavePath(taskForm.getConfig(), taskForm.getResponse());
                FileUtil.deleteIfExists(progressPath);
                FileUtil.deleteIfExists(ContentUtil.buildBakPath(progressPath));
              } else if (taskInfo.getStatus() != HttpDownStatus.PAUSE) {
                taskInfo.setStatus(HttpDownStatus.PAUSE);
                //暂停时间计算
                taskInfo.setLastPauseTime(System.currentTimeMillis());
                for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
                  chunkInfo.setLastPauseTime(taskInfo.getLastPauseTime());
                }
              }
            }
          }
          HttpDownBootstrap httpDownBootstrap = HttpDownBootstrap.builder()
              .request(request)
              .response(taskForm.getResponse())
              .downConfig(taskForm.getConfig())
              .taskInfo(taskInfo)
              .callback(new PersistenceHttpDownCallback())
              .build();
          content.put(taskForm.getId(), httpDownBootstrap);
        }
      }
    } catch (Exception e) {
      log("load error", e);
    }
    if (content == null) {
      content = defaultValue();
    }
    return this;
  }

  @Override
  public HttpDownContent save() {
    if (content != null) {
      List<TaskForm> taskForms = content.entrySet().stream()
          .map(entry -> TaskForm.parse(entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());
      try {
        synchronized (content) {
          ContentUtil.save(taskForms, savePath(), isHidden());
        }
      } catch (IOException e) {
        log("save error", e);
      }
    }
    return this;
  }

  public String progressSavePath(HttpDownConfigInfo config, HttpResponseInfo response) {
    return config.getFilePath() + File.separator + "." + response.getFileName() + ".dat";
  }

  public HttpDownContent save(HttpDownBootstrap bootstrap) {
    if (bootstrap != null) {
      TaskInfo taskInfo = bootstrap.getTaskInfo();
      if (taskInfo != null) {
        try {
          synchronized (bootstrap) {
            ContentUtil.save(taskInfo, progressSavePath(bootstrap.getDownConfig(), bootstrap.getResponse()), isHidden());
          }
        } catch (IOException e) {
          log("save progress error", e);
        }
      }
    }
    return this;
  }

  public HttpDownContent remove(HttpDownBootstrap bootstrap) {
    if (bootstrap != null) {
      try {
        String progressPath = progressSavePath(bootstrap.getDownConfig(), bootstrap.getResponse());
        synchronized (bootstrap) {
          FileUtil.deleteIfExists(progressPath);
          FileUtil.deleteIfExists(ContentUtil.buildBakPath(progressPath));
        }
      } catch (IOException e) {
        log("remove progress error", e);
      }
    }
    return this;
  }
}
