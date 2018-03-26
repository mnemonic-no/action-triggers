package no.mnemonic.services.triggers.service.dao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.CollectionUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JsonDeserialize(builder = TriggerRuleEntity.Builder.class)
public class TriggerRuleEntity {

  private final UUID id;
  private final String service;
  private final AccessMode accessMode;
  private final String expression;
  private final String triggerAction;
  private final Set<String> events;
  private final Set<UUID> organizations;
  private final Set<String> scopes;
  private final Map<String, String> triggerParameters;

  private TriggerRuleEntity(UUID id, String service, AccessMode accessMode, String expression, String triggerAction,
                            Set<String> events, Set<UUID> organizations, Set<String> scopes, Map<String, String> triggerParameters) {
    this.id = ObjectUtils.notNull(id, "'id' is required!");
    this.service = ObjectUtils.notNull(service, "'service' is required!");
    this.accessMode = ObjectUtils.notNull(accessMode, "'accessMode' is required!");
    this.expression = ObjectUtils.notNull(expression, "'expression' is required!");
    this.triggerAction = ObjectUtils.notNull(triggerAction, "'triggerAction' is required!");
    this.events = ObjectUtils.ifNotNull(events, Collections::unmodifiableSet);
    this.organizations = ObjectUtils.ifNotNull(organizations, Collections::unmodifiableSet);
    this.scopes = ObjectUtils.ifNotNull(scopes, Collections::unmodifiableSet);
    this.triggerParameters = ObjectUtils.ifNotNull(triggerParameters, Collections::unmodifiableMap);

    if (CollectionUtils.isEmpty(this.events)) throw new IllegalArgumentException("'events' is required!");
    if (CollectionUtils.isEmpty(this.organizations)) throw new IllegalArgumentException("'organizations' is required!");
  }

  public UUID getId() {
    return id;
  }

  public String getService() {
    return service;
  }

  public AccessMode getAccessMode() {
    return accessMode;
  }

  public String getExpression() {
    return expression;
  }

  public String getTriggerAction() {
    return triggerAction;
  }

  public Set<String> getEvents() {
    return events;
  }

  public Set<UUID> getOrganizations() {
    return organizations;
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public Map<String, String> getTriggerParameters() {
    return triggerParameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {
    private UUID id;
    private String service;
    private AccessMode accessMode;
    private String expression;
    private String triggerAction;
    private Set<String> events;
    private Set<UUID> organizations;
    private Set<String> scopes;
    private Map<String, String> triggerParameters;

    private Builder() {
    }

    public TriggerRuleEntity build() {
      return new TriggerRuleEntity(id, service, accessMode, expression, triggerAction, events, organizations, scopes, triggerParameters);
    }

    public Builder setId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder setService(String service) {
      this.service = service;
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

    public Builder setTriggerAction(String triggerAction) {
      this.triggerAction = triggerAction;
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

    public Builder setOrganizations(Set<UUID> organizations) {
      this.organizations = organizations;
      return this;
    }

    public Builder addOrganization(UUID organization) {
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
