package no.mnemonic.services.triggers.service.dao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@JsonDeserialize(builder = TriggerEventDefinitionEntity.Builder.class)
public class TriggerEventDefinitionEntity {

  private final UUID id;
  private final String service;
  private final String name;
  private final String publicPermission;
  private final String roleBasedPermission;
  private final String privatePermission;
  private final Map<String, String> scopes;
  // TODO: Omitted contextParameters for now.

  private TriggerEventDefinitionEntity(UUID id, String service, String name, String publicPermission,
                                       String roleBasedPermission, String privatePermission, Map<String, String> scopes) {
    this.id = ObjectUtils.notNull(id, "'id' is required!");
    this.service = ObjectUtils.notNull(service, "'service' is required!");
    this.name = ObjectUtils.notNull(name, "'name' is required!");
    this.publicPermission = publicPermission;
    this.roleBasedPermission = roleBasedPermission;
    this.privatePermission = privatePermission;
    this.scopes = ObjectUtils.ifNotNull(scopes, Collections::unmodifiableMap);
  }

  public UUID getId() {
    return id;
  }

  public String getService() {
    return service;
  }

  public String getName() {
    return name;
  }

  public String getPublicPermission() {
    return publicPermission;
  }

  public String getRoleBasedPermission() {
    return roleBasedPermission;
  }

  public String getPrivatePermission() {
    return privatePermission;
  }

  public Map<String, String> getScopes() {
    return scopes;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonPOJOBuilder(withPrefix = "set")
  public static class Builder {
    private UUID id;
    private String service;
    private String name;
    private String publicPermission;
    private String roleBasedPermission;
    private String privatePermission;
    private Map<String, String> scopes;

    private Builder() {
    }

    public TriggerEventDefinitionEntity build() {
      return new TriggerEventDefinitionEntity(id, service, name, publicPermission, roleBasedPermission, privatePermission, scopes);
    }

    public Builder setId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder setService(String service) {
      this.service = service;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setPublicPermission(String publicPermission) {
      this.publicPermission = publicPermission;
      return this;
    }

    public Builder setRoleBasedPermission(String roleBasedPermission) {
      this.roleBasedPermission = roleBasedPermission;
      return this;
    }

    public Builder setPrivatePermission(String privatePermission) {
      this.privatePermission = privatePermission;
      return this;
    }

    public Builder setScopes(Map<String, String> scopes) {
      this.scopes = scopes;
      return this;
    }

    public Builder addScope(String scopeName, String scopePermission) {
      this.scopes = MapUtils.addToMap(this.scopes, scopeName, scopePermission);
      return this;
    }
  }
}
