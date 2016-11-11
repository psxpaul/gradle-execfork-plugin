package com.github.psxpaul.example;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.http.client.fluent.Request;
import org.junit.Test;

public class MainTest {

  @Test
  public void testResponse() throws Exception {
    String responseBody = Request.Get("http://localhost:9201/")
      .connectTimeout(1000)
      .socketTimeout(1000)
      .execute().returnContent().asString();

    assertThat(responseBody.trim(), equalTo("PING"));
  }
}
