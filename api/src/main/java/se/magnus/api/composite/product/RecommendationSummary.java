package se.magnus.api.composite.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RecommendationSummary {

  private final int recommendationId;
  private final String author;
  private final int rate;

}
