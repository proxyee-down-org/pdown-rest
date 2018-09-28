package org.pdown.rest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.pdown.rest.form.EventForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class TaskEventHandler extends TextWebSocketHandler {

  private final static Logger LOGGER = LoggerFactory.getLogger(TaskEventHandler.class);
  //websocket对象管理
  private static Map<String, WebSocketSession> CONTENT = new ConcurrentHashMap<>();

  public static void dispatchEvent(EventForm eventForm) {
    try {
      if (eventForm == null) {
        return;
      }
      ObjectMapper objectMapper = new ObjectMapper();
      TextMessage message = new TextMessage(objectMapper.writeValueAsString(eventForm));
      for (Entry<String, WebSocketSession> entry : CONTENT.entrySet()) {
        WebSocketSession session = entry.getValue();
        if (session.isOpen()) {
          synchronized (session) {
            session.sendMessage(message);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("sendMsg", e);
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    CONTENT.put(session.getId(), session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    CONTENT.remove(session.getId());
  }
}
