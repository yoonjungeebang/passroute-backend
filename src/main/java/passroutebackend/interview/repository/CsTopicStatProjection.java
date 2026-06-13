package passroutebackend.interview.repository;

import passroutebackend.interview.entity.CsTopic;

public interface CsTopicStatProjection {
  CsTopic getCsTopic();
  Long getQuestionCount();
  Double getAvgPercentage();
}
