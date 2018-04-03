package no.mnemonic.services.triggers.action;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.mnemonic.services.triggers.action.exceptions.ParameterException;
import no.mnemonic.services.triggers.action.exceptions.TriggerExecutionException;
import org.apache.http.entity.ContentType;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HttpClientActionTest {

  @Rule
  public WireMockRule server = new WireMockRule(options().dynamicPort());
  @Rule
  public WireMockRule proxy = new WireMockRule(options().dynamicPort());

  @Test
  public void testInitWithInvalidProxySetting() throws Exception {
    Map<String, String> initParameters = new HashMap<String, String>() {{
      put("proxy", "123:invalid-url");
    }};

    try {
      new HttpClientAction().init(initParameters);
      fail();
    } catch (ParameterException ex) {
      assertEquals("proxy", ex.getParameter());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testTriggerWithoutCallingInitFirst() throws Exception {
    new HttpClientAction().trigger(null);
  }

  @Test
  public void testTriggerWithoutUrlSetting() throws Exception {
    try {
      triggerAction(null, null);
      fail();
    } catch (ParameterException ex) {
      assertEquals("url", ex.getParameter());
    }
  }

  @Test
  public void testTriggerWithInvalidUrlSetting() throws Exception {
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", "123:invalid-url");
    }};

    try {
      triggerAction(null, triggerParameters);
      fail();
    } catch (ParameterException ex) {
      assertEquals("url", ex.getParameter());
    }
  }

  @Test
  public void testTriggerWithInvalidUrlProtocol() throws Exception {
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", "ftp://example.org");
    }};

    try {
      triggerAction(null, triggerParameters);
      fail();
    } catch (ParameterException ex) {
      assertEquals("url", ex.getParameter());
    }
  }

  @Test
  public void testActionWithDefaultOptions() throws Exception {
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/do", server.port()));
    }};

    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    server.verify(getRequestedFor(urlEqualTo("/do")));
  }

  @Test
  public void testActionWithTextBody() throws Exception {
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/do", server.port()));
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
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/do", server.port()));
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
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/do", server.port()));
      put("X-Custom-Header", "42");
    }};

    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    server.verify(getRequestedFor(urlEqualTo("/do"))
        .withHeader("X-Custom-Header", equalTo("42"))
    );
  }

  @Test
  public void testActionWithFailedResponse() throws Exception {
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/do", server.port()));
    }};

    server.stubFor(get("/do").willReturn(unauthorized()));
    try {
      triggerAction(null, triggerParameters);
      fail();
    } catch (TriggerExecutionException ignored) {
      server.verify(getRequestedFor(urlEqualTo("/do")));
    }
  }

  @Test
  public void testActionWithProxySettings() throws Exception {
    Map<String, String> initParameters = new HashMap<String, String>() {{
      put("proxy", String.format("http://localhost:%d", proxy.port()));
    }};
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/do", server.port()));
    }};

    proxy.stubFor(get(anyUrl()).willReturn(aResponse()
        .proxiedFrom(String.format("http://localhost:%d", server.port()))
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
    Map<String, String> triggerParameters = new HashMap<String, String>() {{
      put("url", String.format("http://localhost:%d/moved", proxy.port()));
    }};

    proxy.stubFor(get("/moved").willReturn(permanentRedirect(String.format("http://localhost:%d/do", server.port()))));
    server.stubFor(get("/do").willReturn(ok()));
    triggerAction(null, triggerParameters);
    proxy.verify(getRequestedFor(urlEqualTo("/moved")));
    server.verify(getRequestedFor(urlEqualTo("/do")));
  }

  private void triggerAction(Map<String, String> initParameters, Map<String, String> triggerParameters) throws Exception {
    try (TriggerAction action = HttpClientAction.class.newInstance()) {
      action.init(initParameters);
      action.trigger(triggerParameters);
    }
  }
}
