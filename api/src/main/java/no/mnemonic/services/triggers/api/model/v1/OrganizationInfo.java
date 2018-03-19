package no.mnemonic.services.triggers.api.model.v1;

import java.util.UUID;

public class OrganizationInfo {

  private final UUID id;
  private final String shortName;
  private final String name;

  private OrganizationInfo(UUID id, String shortName, String name) {
    this.id = id;
    this.shortName = shortName;
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public String getShortName() {
    return shortName;
  }

  public String getName() {
    return name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID id;
    private String shortName;
    private String name;

    private Builder() {
    }

    public OrganizationInfo build() {
      return new OrganizationInfo(id, shortName, name);
    }

    public Builder setId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder setShortName(String shortName) {
      this.shortName = shortName;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }
  }
}
