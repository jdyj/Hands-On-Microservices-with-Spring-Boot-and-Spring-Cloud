package se.magnus.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService,
    ReviewService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  public ProductCompositeIntegration(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort) {

    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;

    productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
    recommendationServiceUrl =
        "http://" + recommendationServiceHost + ":" + recommendationServicePort
            + "/recommendation?productId=";
    reviewServiceUrl =
        "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";

  }

  public Product getProduct(int productId) {
    try {
      String url = productServiceUrl + productId;
      return restTemplate.getForObject(url, Product.class);
    } catch (HttpClientErrorException ex) {
      switch (ex.getStatusCode()) {
        case NOT_FOUND:
          throw new NotFoundException(getErrorMessage(ex));
        case UNPROCESSABLE_ENTITY:
          throw new InvalidInputException(getErrorMessage(ex));
        default:
          log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
          log.warn("Error body: {}", ex.getResponseBodyAsString());
          throw ex;
      }
    }
  }

  public List<Recommendation> getRecommendations(int productId) {
    String url = recommendationServiceUrl + productId;
    return restTemplate.exchange(url, HttpMethod.GET, null,
        new ParameterizedTypeReference<List<Recommendation>>() {
        }).getBody();
  }

  public List<Review> getReviews(int productId) {
    String url = reviewServiceUrl + productId;
    return restTemplate.exchange(url, HttpMethod.GET, null,
        new ParameterizedTypeReference<List<Review>>() {
        }).getBody();
  }

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}
