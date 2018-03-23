package no.mnemonic.services.triggers.service.dao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import no.mnemonic.commons.utilities.ObjectUtils;

@JsonDeserialize(builder = ParameterDefinitionEntity.Builder.class)
public class ParameterDefinitionEntity {

  private final String description;
  private final Boolean required;
  private final String defaultValue;

  private ParameterDefinitionEntity(String description, Boolean required, String defaultValue) {
    this.description = ObjectUtils.notNull(description, "'description' is required!");
    this.required = ObjectUtils.notNull(required, "'required' is required!");
    this.defaultValue = defaultValue;
  }

  public String getDescription() {
    return description;
  }

  public Boolean isRequired() {
    return required;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {
    private String description;
    private Boolean required;
    private String defaultValue;

    private Builder() {
    }

    public ParameterDefinitionEntity build() {
      return new ParameterDefinitionEntity(description, required, defaultValue);
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setRequired(Boolean required) {
      this.required = required;
      return this;
    }

    public Builder setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }
  }
}
