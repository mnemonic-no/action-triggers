package no.mnemonic.services.triggers.api.model.v1;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TriggerActionDefinition {

  private final UUID id;
  private final String name;
  private final String description;
  private final String triggerActionClass;
  private final FunctionInfo requiredPermission;
  private final Map<String, String> initParameters;
  private final Map<String, ParameterDefinition> triggerParameters;

  private TriggerActionDefinition(UUID id, String name, String description, String triggerActionClass,
                                  FunctionInfo requiredPermission, Map<String, String> initParameters,
                                  Map<String, ParameterDefinition> triggerParameters) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.triggerActionClass = triggerActionClass;
    this.requiredPermission = requiredPermission;
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

  public FunctionInfo getRequiredPermission() {
    return requiredPermission;
  }

  public Map<String, String> getInitParameters() {
    return initParameters;
  }

  public Map<String, ParameterDefinition> getTriggerParameters() {
    return triggerParameters;
  }

  public Info toInfo() {
    return new Info(id, name);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID id;
    private String name;
    private String description;
    private String triggerActionClass;
    private FunctionInfo requiredPermission;
    private Map<String, String> initParameters;
    private Map<String, ParameterDefinition> triggerParameters;

    private Builder() {
    }

    public TriggerActionDefinition build() {
      return new TriggerActionDefinition(id, name, description, triggerActionClass, requiredPermission, initParameters, triggerParameters);
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

    public Builder setRequiredPermission(FunctionInfo requiredPermission) {
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

    public Builder setTriggerParameters(Map<String, ParameterDefinition> triggerParameters) {
      this.triggerParameters = triggerParameters;
      return this;
    }

    public Builder addTriggerParameter(String parameterName, ParameterDefinition parameterDefinition) {
      this.triggerParameters = MapUtils.addToMap(this.triggerParameters, parameterName, parameterDefinition);
      return this;
    }
  }

  public class Info {
    private final UUID id;
    private final String name;

    private Info(UUID id, String name) {
      this.id = id;
      this.name = name;
    }

    public UUID getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }
}
