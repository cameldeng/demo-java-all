package xray;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

  @Test
  public void successfulResponse() {
    App app = new App();
    GatewayResponse result = (GatewayResponse) app.handleRequest(null, null);
    assertEquals(200, result.getStatusCode());
    assertEquals("application/json", result.getHeaders().get("Content-Type"));
    String content = result.getBody();
    assertNotNull(content);
    assertTrue(content.contains("\"message\""));
    assertTrue(content.contains("\"hello world\""));
    assertTrue(content.contains("\"location\""));
  }
}
