package no.mnemonic.services.triggers.api.model.v1;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TriggerEventDefinition {

  private final UUID id;
  private final String service;
  private final String name;
  private final FunctionInfo publicPermission;
  private final FunctionInfo roleBasedPermission;
  private final FunctionInfo privatePermission;
  private final Map<String, FunctionInfo> scopes;
  // TODO: Omitted contextParameters for now.

  private TriggerEventDefinition(UUID id, String service, String name, FunctionInfo publicPermission,
                                 FunctionInfo roleBasedPermission, FunctionInfo privatePermission,
                                 Map<String, FunctionInfo> scopes) {
    this.id = id;
    this.service = service;
    this.name = name;
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

  public FunctionInfo getPublicPermission() {
    return publicPermission;
  }

  public FunctionInfo getRoleBasedPermission() {
    return roleBasedPermission;
  }

  public FunctionInfo getPrivatePermission() {
    return privatePermission;
  }

  public Map<String, FunctionInfo> getScopes() {
    return scopes;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID id;
    private String service;
    private String name;
    private FunctionInfo publicPermission;
    private FunctionInfo roleBasedPermission;
    private FunctionInfo privatePermission;
    private Map<String, FunctionInfo> scopes;

    private Builder() {
    }

    public TriggerEventDefinition build() {
      return new TriggerEventDefinition(id, service, name, publicPermission, roleBasedPermission, privatePermission, scopes);
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

    public Builder setPublicPermission(FunctionInfo publicPermission) {
      this.publicPermission = publicPermission;
      return this;
    }

    public Builder setRoleBasedPermission(FunctionInfo roleBasedPermission) {
      this.roleBasedPermission = roleBasedPermission;
      return this;
    }

    public Builder setPrivatePermission(FunctionInfo privatePermission) {
      this.privatePermission = privatePermission;
      return this;
    }

    public Builder setScopes(Map<String, FunctionInfo> scopes) {
      this.scopes = scopes;
      return this;
    }

    public Builder addScope(String scopeName, FunctionInfo scopePermission) {
      this.scopes = MapUtils.addToMap(this.scopes, scopeName, scopePermission);
      return this;
    }
  }
}
