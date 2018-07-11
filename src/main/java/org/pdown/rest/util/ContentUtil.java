package org.pdown.rest.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.pdown.core.util.FileUtil;

public class ContentUtil {

  public static void save(Object obj, String path, boolean isHidden, String... ignoreFields) throws IOException {
    byte[] bytes = getObjectMapper(ignoreFields).writeValueAsBytes(obj);
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

  public static void save(Object obj, String path, boolean isHidden) throws IOException {
    save(obj, path, isHidden,null);
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

  public static <T> T get(String path, Class<T> clazz) throws IOException {
    String bakPath = buildBakPath(path);
    if (!FileUtil.existsAny(path, bakPath)) {
      return null;
    }
    ObjectMapper objectMapper = getObjectMapper();
    try {
      return objectMapper.readValue(new FileInputStream(path), clazz);
    } catch (Exception e) {
      return objectMapper.readValue(new FileInputStream(bakPath), clazz);
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
    return getObjectMapper(null);
  }

  public static ObjectMapper getObjectMapper(String... ignoreFields) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper = objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(Visibility.ANY)
        .withIsGetterVisibility(Visibility.NONE)
        .withGetterVisibility(Visibility.NONE)
        .withSetterVisibility(Visibility.NONE)
        .withCreatorVisibility(Visibility.NONE));
    if (ignoreFields != null && ignoreFields.length > 0) {
      SimpleFilterProvider filterProvider = new SimpleFilterProvider();
      SimpleBeanPropertyFilter fieldFilter = SimpleBeanPropertyFilter.serializeAllExcept(ignoreFields);
      filterProvider.addFilter("fieldFilter", fieldFilter);
      objectMapper.setFilterProvider(filterProvider);
    }
    return objectMapper;
  }
}
