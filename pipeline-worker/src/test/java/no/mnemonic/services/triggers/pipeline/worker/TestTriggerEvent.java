package no.mnemonic.services.triggers.pipeline.worker;

import no.mnemonic.services.triggers.pipeline.api.AccessMode;
import no.mnemonic.services.triggers.pipeline.api.TriggerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class TestTriggerEvent implements TriggerEvent {
  private UUID id = UUID.randomUUID();
  private long timestamp = 123456789;
  private String service = "TestService";
  private String event = "TestEvent";
  private UUID organization = UUID.randomUUID();
  private AccessMode accessMode = AccessMode.Public;
  private String scope = "TestScope";
  private Map<String, Object> contextParameters = new HashMap<>();

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String getService() {
    return service;
  }

  @Override
  public String getEvent() {
    return event;
  }

  @Override
  public UUID getOrganization() {
    return organization;
  }

  @Override
  public AccessMode getAccessMode() {
    return accessMode;
  }

  @Override
  public String getScope() {
    return scope;
  }

  @Override
  public Map<String, ?> getContextParameters() {
    return contextParameters;
  }

  TestTriggerEvent setId(UUID id) {
    this.id = id;
    return this;
  }

  TestTriggerEvent setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  TestTriggerEvent setService(String service) {
    this.service = service;
    return this;
  }

  TestTriggerEvent setEvent(String event) {
    this.event = event;
    return this;
  }

  TestTriggerEvent setOrganization(UUID organization) {
    this.organization = organization;
    return this;
  }

  TestTriggerEvent setAccessMode(AccessMode accessMode) {
    this.accessMode = accessMode;
    return this;
  }

  TestTriggerEvent setScope(String scope) {
    this.scope = scope;
    return this;
  }

  TestTriggerEvent addContextParameter(String parameter, Object value) {
    this.contextParameters.put(parameter, value);
    return this;
  }
}
