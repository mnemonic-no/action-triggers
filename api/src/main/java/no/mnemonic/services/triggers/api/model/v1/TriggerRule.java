package no.mnemonic.services.triggers.api.model.v1;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TriggerRule {

  private final UUID id;
  private final String service;
  private final Set<String> events;
  private final Set<OrganizationInfo> organizations;
  private final Set<String> scopes;
  private final AccessMode accessMode;
  private final String expression;
  private final TriggerActionDefinition.Info triggerAction;
  private final Map<String, String> triggerParameters;

  private TriggerRule(UUID id, String service, Set<String> events, Set<OrganizationInfo> organizations, Set<String> scopes,
                      AccessMode accessMode, String expression, TriggerActionDefinition.Info triggerAction,
                      Map<String, String> triggerParameters) {
    this.id = id;
    this.service = service;
    this.events = ObjectUtils.ifNotNull(events, Collections::unmodifiableSet);
    this.organizations = ObjectUtils.ifNotNull(organizations, Collections::unmodifiableSet);
    this.scopes = ObjectUtils.ifNotNull(scopes, Collections::unmodifiableSet);
    this.accessMode = accessMode;
    this.expression = expression;
    this.triggerAction = triggerAction;
    this.triggerParameters = ObjectUtils.ifNotNull(triggerParameters, Collections::unmodifiableMap);
  }

  public UUID getId() {
    return id;
  }

  public String getService() {
    return service;
  }

  public Set<String> getEvents() {
    return events;
  }

  public Set<OrganizationInfo> getOrganizations() {
    return organizations;
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public AccessMode getAccessMode() {
    return accessMode;
  }

  public String getExpression() {
    return expression;
  }

  public TriggerActionDefinition.Info getTriggerAction() {
    return triggerAction;
  }

  public Map<String, String> getTriggerParameters() {
    return triggerParameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID id;
    private String service;
    private Set<String> events;
    private Set<OrganizationInfo> organizations;
    private Set<String> scopes;
    private AccessMode accessMode;
    private String expression;
    private TriggerActionDefinition.Info triggerAction;
    private Map<String, String> triggerParameters;

    private Builder() {
    }

    public TriggerRule build() {
      return new TriggerRule(id, service, events, organizations, scopes, accessMode, expression, triggerAction, triggerParameters);
    }

    public Builder setId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder setService(String service) {
      this.service = service;
      return this;
    }

    public Builder setEvents(Set<String> events) {
      this.events = events;
      return this;
    }

    public Builder addEvent(String event) {
      this.events = SetUtils.addToSet(this.events, event);
      return this;
    }

    public Builder setOrganizations(Set<OrganizationInfo> organizations) {
      this.organizations = organizations;
      return this;
    }

    public Builder addOrganization(OrganizationInfo organization) {
      this.organizations = SetUtils.addToSet(this.organizations, organization);
      return this;
    }

    public Builder setScopes(Set<String> scopes) {
      this.scopes = scopes;
      return this;
    }

    public Builder addScope(String scope) {
      this.scopes = SetUtils.addToSet(this.scopes, scope);
      return this;
    }

    public Builder setAccessMode(AccessMode accessMode) {
      this.accessMode = accessMode;
      return this;
    }

    public Builder setExpression(String expression) {
      this.expression = expression;
      return this;
    }

    public Builder setTriggerAction(TriggerActionDefinition.Info triggerAction) {
      this.triggerAction = triggerAction;
      return this;
    }

    public Builder setTriggerParameters(Map<String, String> triggerParameters) {
      this.triggerParameters = triggerParameters;
      return this;
    }

    public Builder addTriggerParameter(String parameterName, String parameterValue) {
      this.triggerParameters = MapUtils.addToMap(this.triggerParameters, parameterName, parameterValue);
      return this;
    }
  }
}
