package org.pdown.rest.vo;

import java.util.List;

public class ResumeVo {
  private List<String> pauseIds;
  private List<String> resumeIds;

  public List<String> getPauseIds() {
    return pauseIds;
  }

  public ResumeVo setPauseIds(List<String> pauseIds) {
    this.pauseIds = pauseIds;
    return this;
  }

  public List<String> getResumeIds() {
    return resumeIds;
  }

  public ResumeVo setResumeIds(List<String> resumeIds) {
    this.resumeIds = resumeIds;
    return this;
  }
}
