package no.mnemonic.services.triggers.api.request.v1;

import no.mnemonic.commons.utilities.collections.SetUtils;

import java.util.Set;

public class TriggerRuleSearchRequest {

  private Set<String> service;
  private Set<String> event;

  public Set<String> getService() {
    return service;
  }

  public TriggerRuleSearchRequest setService(Set<String> service) {
    this.service = service;
    return this;
  }

  public TriggerRuleSearchRequest addService(String service) {
    this.service = SetUtils.addToSet(this.service, service);
    return this;
  }

  public Set<String> getEvent() {
    return event;
  }

  public TriggerRuleSearchRequest setEvent(Set<String> event) {
    this.event = event;
    return this;
  }

  public TriggerRuleSearchRequest addEvent(String event) {
    this.event = SetUtils.addToSet(this.event, event);
    return this;
  }
}
