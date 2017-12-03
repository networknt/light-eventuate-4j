package com.networknt.eventuate.server.common;

public class TopicCleaner {

  public static String clean(String topic) {
    return topic.replace("$", "_DLR_");
  }

}
