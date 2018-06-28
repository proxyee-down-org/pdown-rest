package org.pdown.rest.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.util.FileUtil;

public class ContentUtil {

  public static void save(Object obj, String path, boolean isHidden) throws IOException {
    byte[] bytes = getObjectMapper().writeValueAsBytes(obj);
    FileUtil.initFile(path, isHidden);
    try (
        RandomAccessFile raf = new RandomAccessFile(path, "rws")
    ) {
      raf.write(bytes);
    }
    String bakPath = buildBakPath(path);
    FileUtil.initFile(bakPath, true);
    try (
        RandomAccessFile raf2 = new RandomAccessFile(bakPath, "rws")
    ) {
      raf2.write(bytes);
    }
  }

  public static void save(Object obj, String path) throws IOException {
    save(obj, path, false);
  }

  public static <T> T get(String path, TypeReference typeReference) throws IOException {
    String bakPath = buildBakPath(path);
    if (!FileUtil.existsAny(path, bakPath)) {
      return null;
    }
    ObjectMapper objectMapper = getObjectMapper();
    try {
      return (T) objectMapper.readValue(new FileInputStream(path), typeReference);
    } catch (Exception e) {
      return (T) objectMapper.readValue(new FileInputStream(bakPath), typeReference);
    }
  }

  private static String buildBakPath(String path) {
    File saveFile = new File(path);
    StringBuilder sb = new StringBuilder(saveFile.getParent() + File.separator);
    if (saveFile.getName().indexOf(".") != 0) {
      sb.append(".");
    }
    sb.append(saveFile.getName() + ".bak");
    return sb.toString();
  }

  public static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(Visibility.ANY)
        .withIsGetterVisibility(Visibility.NONE)
        .withGetterVisibility(Visibility.NONE)
        .withSetterVisibility(Visibility.NONE)
        .withCreatorVisibility(Visibility.NONE));
  }

  public static void main(String[] args) throws IOException {
    HttpDownBootstrap aaa = HttpDownBootstrap.builder("http://192.168.2.24/static/test.iso")
        .downConfig(new HttpDownConfigInfo().setConnections(12))
        .build();
    aaa.getRequest().setContent(new byte[]{1, 2, 3});
    save(aaa, "f:/test/list.inf");
    HttpDownBootstrap list1 = get("f:/test/list.inf", new TypeReference<HttpDownBootstrap>() {
    });
    System.out.println(list1.getRequest().content().length);
    System.out.println(list1.toString());
  }
}
