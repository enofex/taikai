package com.enofex.taikai.kotlin.fixtures.compatibility;

public class JavaLegacyApi {
  @Deprecated
  public String oldField = "old";

  @Deprecated
  public String oldMethod() {
    return "old";
  }

  @Deprecated
  public JavaLegacyApi(String value) {
  }

  public JavaLegacyApi() {
  }
}
