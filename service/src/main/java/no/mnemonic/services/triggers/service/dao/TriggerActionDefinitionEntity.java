package no.mnemonic.services.triggers.service.dao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@JsonDeserialize(builder = TriggerActionDefinitionEntity.Builder.class)
public class TriggerActionDefinitionEntity {

  private final UUID id;
  private final String name;
  private final String description;
  private final String triggerActionClass;
  private final String requiredPermission;
  private final Map<String, String> initParameters;
  private final Map<String, ParameterDefinitionEntity> triggerParameters;

  private TriggerActionDefinitionEntity(UUID id, String name, String description, String triggerActionClass, String requiredPermission,
                                        Map<String, String> initParameters, Map<String, ParameterDefinitionEntity> triggerParameters) {
    this.id = ObjectUtils.notNull(id, "'id' is required!");
    this.name = ObjectUtils.notNull(name, "'name' is required!");
    this.description = ObjectUtils.notNull(description, "'description' is required!");
    this.triggerActionClass = ObjectUtils.notNull(triggerActionClass, "'triggerActionClass' is required!");
    this.requiredPermission = ObjectUtils.notNull(requiredPermission, "'requiredPermission' is required!");
    this.initParameters = ObjectUtils.ifNotNull(initParameters, Collections::unmodifiableMap);
    this.triggerParameters = ObjectUtils.ifNotNull(triggerParameters, Collections::unmodifiableMap);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getTriggerActionClass() {
    return triggerActionClass;
  }

  public String getRequiredPermission() {
    return requiredPermission;
  }

  public Map<String, String> getInitParameters() {
    return initParameters;
  }

  public Map<String, ParameterDefinitionEntity> getTriggerParameters() {
    return triggerParameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {
    private UUID id;
    private String name;
    private String description;
    private String triggerActionClass;
    private String requiredPermission;
    private Map<String, String> initParameters;
    private Map<String, ParameterDefinitionEntity> triggerParameters;

    private Builder() {
    }

    public TriggerActionDefinitionEntity build() {
      return new TriggerActionDefinitionEntity(id, name, description, triggerActionClass, requiredPermission, initParameters, triggerParameters);
    }

    public Builder setId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setTriggerActionClass(String triggerActionClass) {
      this.triggerActionClass = triggerActionClass;
      return this;
    }

    public Builder setRequiredPermission(String requiredPermission) {
      this.requiredPermission = requiredPermission;
      return this;
    }

    public Builder setInitParameters(Map<String, String> initParameters) {
      this.initParameters = initParameters;
      return this;
    }

    public Builder addInitParameter(String parameterName, String parameterValue) {
      this.initParameters = MapUtils.addToMap(this.initParameters, parameterName, parameterValue);
      return this;
    }

    public Builder setTriggerParameters(Map<String, ParameterDefinitionEntity> triggerParameters) {
      this.triggerParameters = triggerParameters;
      return this;
    }

    public Builder addTriggerParameter(String parameterName, ParameterDefinitionEntity parameterDefinition) {
      this.triggerParameters = MapUtils.addToMap(this.triggerParameters, parameterName, parameterDefinition);
      return this;
    }
  }
}
