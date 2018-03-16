package no.mnemonic.services.triggers.api.model.v1;

public class ParameterDefinition {

  private final String description;
  private final boolean required;
  private final String defaultValue;

  private ParameterDefinition(String description, boolean required, String defaultValue) {
    this.description = description;
    this.required = required;
    this.defaultValue = defaultValue;
  }

  public String getDescription() {
    return description;
  }

  public boolean isRequired() {
    return required;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String description;
    private boolean required;
    private String defaultValue;

    private Builder() {
    }

    public ParameterDefinition build() {
      return new ParameterDefinition(description, required, defaultValue);
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setRequired(boolean required) {
      this.required = required;
      return this;
    }

    public Builder setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }
  }
}
