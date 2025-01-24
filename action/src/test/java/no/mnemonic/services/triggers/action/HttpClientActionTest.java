package no.mnemonic.services.triggers.action;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import no.mnemonic.services.triggers.action.exceptions.ParameterException;
import no.mnemonic.services.triggers.action.exceptions.TriggerExecutionException;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpClientActionTest {

  @RegisterExtension
  static WireMockExtension server = WireMockExtension.newInstance()
      .options(options().dynamicPort())
      .build();
  @RegisterExtension
  static WireMockExtension proxy = WireMockExtension.newInstance()
      .options(options().dynamicPort())
      .build();

  @Test
  public void testInitWithInvalidProxySetting() {
    Map<String, String> initParameters = new HashMap<>() {{
      put("proxy", "123:invalid-url");
    }};

    ParameterException ex = assertThrows(ParameterException.class, () -> new HttpClientAction().init(initParameters));
    assertEquals("proxy", ex.getParameter());
  }

  @Test
  public void testTriggerWithoutCallingInitFirst() {
    assertThrows(IllegalStateException.class, () -> new HttpClientAction().trigger(null));
  }

  @Test
  public void testTriggerWithInvalidMethod() {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("method", "invalid");
    }};

    ParameterException ex = assertThrows(ParameterException.class, () -> triggerAction(null, triggerParameters));
    assertEquals("method", ex.getParameter());
  }

  @Test
  public void testTriggerWithoutUrlSetting() {
    ParameterException ex = assertThrows(ParameterException.class, () -> triggerAction(null, null));
    assertEquals("url", ex.getParameter());
  }

  @Test
  public void testTriggerWithInvalidUrlSetting() {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", "123:invalid-url");
    }};

    ParameterException ex = assertThrows(ParameterException.class, () -> triggerAction(null, triggerParameters));
    assertEquals("url", ex.getParameter());
  }

  @Test
  public void testTriggerWithInvalidUrlProtocol() {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", "ftp://example.org");
    }};

    ParameterException ex = assertThrows(ParameterException.class, () -> triggerAction(null, triggerParameters));
    assertEquals("url", ex.getParameter());
  }

  @Test
  public void testActionWithDefaultOptions() throws Exception {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/do", server.getPort()));
    }};

    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    server.verify(getRequestedFor(urlEqualTo("/do")));
  }

  @Test
  public void testActionWithTextBody() throws Exception {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/do", server.getPort()));
      put("method", "POST");
      put("body", "Hello World!");
    }};

    server.stubFor(post("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    server.verify(postRequestedFor(urlEqualTo("/do"))
        .withHeader("Content-Type", equalTo(ContentType.DEFAULT_TEXT.toString()))
        .withRequestBody(equalTo("Hello World!"))
    );
  }

  @Test
  public void testActionWithJsonBody() throws Exception {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/do", server.getPort()));
      put("method", "POST");
      put("body", "{ \"a\" : \"b\" }");
      put("contentType", ContentType.APPLICATION_JSON.toString());
    }};

    server.stubFor(post("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    server.verify(postRequestedFor(urlEqualTo("/do"))
        .withHeader("Content-Type", equalTo(ContentType.APPLICATION_JSON.toString()))
        .withRequestBody(equalToJson("{ \"a\" : \"b\" }"))
    );
  }

  @Test
  public void testActionWithAdditionalHeaders() throws Exception {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/do", server.getPort()));
      put("header@X-Custom-Header", "42");
    }};

    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    server.verify(getRequestedFor(urlEqualTo("/do"))
        .withHeader("X-Custom-Header", equalTo("42"))
    );
  }

  @Test
  public void testActionWithFailedResponse() {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/do", server.getPort()));
    }};

    server.stubFor(get("/do").willReturn(unauthorized()));
    assertThrows(TriggerExecutionException.class, () -> triggerAction(null, triggerParameters));
    server.verify(getRequestedFor(urlEqualTo("/do")));
  }

  @Test
  public void testActionWithProxySettings() throws Exception {
    Map<String, String> initParameters = new HashMap<>() {{
      put("proxy", String.format("http://localhost:%d", proxy.getPort()));
    }};
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/do", server.getPort()));
    }};

    proxy.stubFor(get(anyUrl()).willReturn(aResponse()
        .proxiedFrom(String.format("http://localhost:%d", server.getPort()))
        .withAdditionalRequestHeader("X-Proxy", "42")
    ));
    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(initParameters, triggerParameters);
    proxy.verify(getRequestedFor(urlEqualTo("/do")));
    server.verify(getRequestedFor(urlEqualTo("/do"))
        .withHeader("X-Proxy", equalTo("42"))
    );
  }

  @Test
  public void testActionWithHttpRedirect() throws Exception {
    Map<String, String> triggerParameters = new HashMap<>() {{
      put("url", String.format("http://localhost:%d/moved", proxy.getPort()));
    }};

    proxy.stubFor(get("/moved").willReturn(permanentRedirect(String.format("http://localhost:%d/do", server.getPort()))));
    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    proxy.verify(getRequestedFor(urlEqualTo("/moved")));
    server.verify(getRequestedFor(urlEqualTo("/do")));
  }

  private void triggerAction(Map<String, String> initParameters, Map<String, String> triggerParameters) throws Exception {
    try (TriggerAction action = HttpClientAction.class.getDeclaredConstructor().newInstance()) {
      action.init(initParameters);
      action.trigger(triggerParameters);
    }
  }
}
