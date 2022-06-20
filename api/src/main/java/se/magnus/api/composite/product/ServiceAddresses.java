package se.magnus.api.composite.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ServiceAddresses {

  private final String cmp;
  private final String pro;
  private final String rev;
  private final String rec;

  public ServiceAddresses() {
    cmp = null;
    pro = null;
    rev = null;
    rec = null;
  }

}
