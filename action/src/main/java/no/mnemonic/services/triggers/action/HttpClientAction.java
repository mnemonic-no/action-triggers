package no.mnemonic.services.triggers.action;

import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;
import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;
import no.mnemonic.services.triggers.action.exceptions.ParameterException;
import no.mnemonic.services.triggers.action.exceptions.TriggerExecutionException;
import no.mnemonic.services.triggers.action.exceptions.TriggerInitializationException;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * {@link TriggerAction} implementation for calling HTTP(s) webhooks.
 * <p>
 * It has the following initialization parameters:
 * <ul>
 * <li><b>proxy</b>: URL of a server which will be used to proxy requests (optional).</li>
 * <li>All other parameters will be ignored.</li>
 * </ul>
 * <p>
 * It has the following trigger parameters:
 * <ul>
 * <li><b>url</b>: URL of the webhook to call (required).</li>
 * <li><b>method</b>: HTTP method to be used when making requests (optional, defaults to GET).</li>
 * <li><b>body</b>: Body send in request (optional).</li>
 * <li><b>contentType</b>: Media type of body data (optional, defaults to text/plain, ignored if body parameter is not specified).</li>
 * <li>All other parameters will be sent as additional request headers.</li>
 * </ul>
 */
public class HttpClientAction implements TriggerAction {

  private static final Logger LOGGER = Logging.getLogger(HttpClientAction.class);

  private static final String INIT_PARAMETER_PROXY = "proxy";
  private static final String TRIGGER_PARAMETER_METHOD = "method";
  private static final String TRIGGER_PARAMETER_URL = "url";
  private static final String TRIGGER_PARAMETER_BODY = "body";
  private static final String TRIGGER_PARAMETER_CONTENT_TYPE = "contentType";
  private static final String DEFAULT_HTTP_METHOD = "GET";
  private static final Set<String> SUPPORTED_PROTOCOLS = Collections.unmodifiableSet(SetUtils.set("http", "https"));

  private CloseableHttpClient client;

  @Override
  public void init(Map<String, String> initParameters) throws ParameterException, TriggerInitializationException {
    // Copy initialization parameters into an internal variable, such that it's safe to change them.
    Map<String, String> params = MapUtils.map(initParameters);

    try {
      // Create HTTP client by applying provided initialization parameters and system properties as a fallback.
      client = applyProxySettings(HttpClients.custom(), params)
          .useSystemProperties()
          .build();
    } catch (ParameterException ex) {
      // If applying initialization parameters throws a ParameterException just log and re-throw it.
      LOGGER.warning(ex, "Could not initialize HTTP client. Parameter '%s' is invalid", ex.getParameter());
      throw ex;
    } catch (Exception catchAll) {
      // All other exceptions are treated as action initialization failed.
      LOGGER.error(catchAll, "Could not initialize HTTP client.");
      throw new TriggerInitializationException("Could not initialize HTTP client.", catchAll);
    }
  }

  @Override
  public void trigger(Map<String, String> triggerParameters) throws ParameterException, TriggerExecutionException {
    if (client == null) {
      throw new IllegalStateException("Cannot execute action because HTTP client is not initialized. Forgot to call init()?");
    }

    // Copy trigger parameters into an internal variable, such that it's safe to change them.
    Map<String, String> params = MapUtils.map(triggerParameters);

    try (CloseableHttpResponse response = client.execute(createHttpRequest(params))) {
      // Everything which is not a 2xx status code is considered an error. Also ignore any response body.
      int code = response.getStatusLine().getStatusCode();
      if (!(code >= 200 && code < 300)) {
        throw new HttpResponseException(code, String.format("Failed to execute HTTP request. Received response with status: %s", response.getStatusLine()));
      }

      if (LOGGER.isInfo()) {
        LOGGER.info("Successfully executed HTTP request. Received response with status: %s", response.getStatusLine());
      }
    } catch (ParameterException ex) {
      // If executing the HTTP request throws a ParameterException just log and re-throw it.
      LOGGER.warning(ex, "Could not execute HTTP request. Parameter '%s' is invalid", ex.getParameter());
      throw ex;
    } catch (Exception catchAll) {
      // All other exceptions are treated as action execution failed. Especially this will contain IOExceptions and
      // ClientProtocolExceptions thrown by the HTTP client on execute().
      LOGGER.error(catchAll, "Could not execute HTTP request.");
      throw new TriggerExecutionException("Could not execute HTTP request.", catchAll);
    }
  }

  @Override
  public void close() {
    HttpClientUtils.closeQuietly(client);
  }

  private HttpClientBuilder applyProxySettings(HttpClientBuilder builder, Map<String, String> initParameters)
      throws ParameterException {
    if (initParameters.containsKey(INIT_PARAMETER_PROXY)) {
      String proxy = initParameters.get(INIT_PARAMETER_PROXY);
      try {
        HttpHost host = HttpHost.create(proxy);

        if (LOGGER.isDebug()) {
          LOGGER.debug("Configured HTTP client to use proxy: %s", host.toURI());
        }

        return builder.setProxy(host);
      } catch (Exception ex) {
        throw new ParameterException(String.format("Provided proxy '%s' is invalid.", proxy), ex, INIT_PARAMETER_PROXY);
      }
    }

    return builder;
  }

  private HttpUriRequest createHttpRequest(Map<String, String> triggerParameters) throws ParameterException {
    String method = triggerParameters.getOrDefault(TRIGGER_PARAMETER_METHOD, DEFAULT_HTTP_METHOD);
    URI uri = extractUri(triggerParameters);
    String body = triggerParameters.get(TRIGGER_PARAMETER_BODY);
    ContentType contentType = extractContentType(triggerParameters);

    HttpUriRequest request = RequestBuilder.create(method)
        .setUri(uri)
        .setEntity(ObjectUtils.ifNotNull(body, b -> new StringEntity(b, contentType)))
        .build();

    // All remaining parameters are considered headers.
    triggerParameters.remove(TRIGGER_PARAMETER_METHOD);
    triggerParameters.remove(TRIGGER_PARAMETER_URL);
    triggerParameters.remove(TRIGGER_PARAMETER_BODY);
    triggerParameters.remove(TRIGGER_PARAMETER_CONTENT_TYPE);
    triggerParameters.forEach(request::addHeader);

    if (LOGGER.isDebug()) {
      LOGGER.debug("Created %s request to URL: %s", method, uri);
    }

    return request;
  }

  private URI extractUri(Map<String, String> triggerParameters) throws ParameterException {
    if (triggerParameters.containsKey(TRIGGER_PARAMETER_URL)) {
      String url = triggerParameters.get(TRIGGER_PARAMETER_URL);
      try {
        // A URL is expected here as a parameter but HttpUriRequest takes a URI, thus, a new URL is constructed first
        // to have stricter parsing and is converted to a URI afterwards satisfying HttpUriRequest.
        URI result = new URL(url).toURI();
        if (!SUPPORTED_PROTOCOLS.contains(result.getScheme())) {
          throw new MalformedURLException(String.format("Protocol '%s' is not supported.", result.getScheme()));
        }

        return result;
      } catch (Exception ex) {
        throw new ParameterException(String.format("Provided URL '%s' is invalid.", url), ex, TRIGGER_PARAMETER_URL);
      }
    }

    throw new ParameterException("Required trigger parameter 'url' is missing.", TRIGGER_PARAMETER_URL);
  }

  private ContentType extractContentType(Map<String, String> triggerParameters) throws ParameterException {
    if (triggerParameters.containsKey(TRIGGER_PARAMETER_CONTENT_TYPE)) {
      String contentType = triggerParameters.get(TRIGGER_PARAMETER_CONTENT_TYPE);
      try {
        return ContentType.parse(contentType);
      } catch (Exception ex) {
        throw new ParameterException(String.format("Provided content type '%s' is invalid.", contentType), ex, TRIGGER_PARAMETER_CONTENT_TYPE);
      }
    }

    return ContentType.DEFAULT_TEXT;
  }
}
