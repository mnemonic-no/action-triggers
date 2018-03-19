package no.mnemonic.services.triggers.api.request.v1;

public class TriggerEventDefinitionGetByServiceEventRequest {

  private String service;
  private String event;

  public String getService() {
    return service;
  }

  public TriggerEventDefinitionGetByServiceEventRequest setService(String service) {
    this.service = service;
    return this;
  }

  public String getEvent() {
    return event;
  }

  public TriggerEventDefinitionGetByServiceEventRequest setEvent(String event) {
    this.event = event;
    return this;
  }
}
