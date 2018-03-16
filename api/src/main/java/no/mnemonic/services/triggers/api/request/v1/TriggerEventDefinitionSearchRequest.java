package no.mnemonic.services.triggers.api.request.v1;

import no.mnemonic.commons.utilities.collections.SetUtils;

import java.util.Set;

public class TriggerEventDefinitionSearchRequest {

  private Set<String> service;
  private Set<String> event;

  public Set<String> getService() {
    return service;
  }

  public TriggerEventDefinitionSearchRequest setService(Set<String> service) {
    this.service = service;
    return this;
  }

  public TriggerEventDefinitionSearchRequest addService(String service) {
    this.service = SetUtils.addToSet(this.service, service);
    return this;
  }

  public Set<String> getEvent() {
    return event;
  }

  public TriggerEventDefinitionSearchRequest setEvent(Set<String> event) {
    this.event = event;
    return this;
  }

  public TriggerEventDefinitionSearchRequest addEvent(String event) {
    this.event = SetUtils.addToSet(this.event, event);
    return this;
  }
}
