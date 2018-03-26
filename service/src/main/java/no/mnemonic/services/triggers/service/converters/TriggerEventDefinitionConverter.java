package no.mnemonic.services.triggers.service.converters;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.services.triggers.api.model.v1.FunctionInfo;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.service.dao.TriggerEventDefinitionEntity;

import java.util.Map;
import java.util.function.Function;

import static no.mnemonic.commons.utilities.collections.MapUtils.Pair.T;

public class TriggerEventDefinitionConverter implements Function<TriggerEventDefinitionEntity, TriggerEventDefinition> {

  private final Function<String, FunctionInfo> functionResolver;

  private TriggerEventDefinitionConverter(Function<String, FunctionInfo> functionResolver) {
    this.functionResolver = ObjectUtils.notNull(functionResolver, "Cannot instantiate TriggerEventDefinitionConverter without 'functionResolver'.");
  }

  @Override
  public TriggerEventDefinition apply(TriggerEventDefinitionEntity entity) {
    if (entity == null) return null;
    return TriggerEventDefinition.builder()
        .setId(entity.getId())
        .setService(entity.getService())
        .setName(entity.getName())
        .setPublicPermission(functionResolver.apply(entity.getPublicPermission()))
        .setRoleBasedPermission(functionResolver.apply(entity.getRoleBasedPermission()))
        .setPrivatePermission(functionResolver.apply(entity.getPrivatePermission()))
        .setScopes(convertScopes(entity))
        .build();
  }

  private Map<String, FunctionInfo> convertScopes(TriggerEventDefinitionEntity entity) {
    if (entity.getScopes() == null) return null;
    return MapUtils.map(entity.getScopes().entrySet(), e -> T(e.getKey(), functionResolver.apply(e.getValue())));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Function<String, FunctionInfo> functionResolver;

    private Builder() {
    }

    public TriggerEventDefinitionConverter build() {
      return new TriggerEventDefinitionConverter(functionResolver);
    }

    public Builder setFunctionResolver(Function<String, FunctionInfo> functionResolver) {
      this.functionResolver = functionResolver;
      return this;
    }
  }
}
