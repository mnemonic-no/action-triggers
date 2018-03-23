package no.mnemonic.services.triggers.service.converters;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.services.triggers.api.model.v1.FunctionInfo;
import no.mnemonic.services.triggers.api.model.v1.ParameterDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.service.dao.ParameterDefinitionEntity;
import no.mnemonic.services.triggers.service.dao.TriggerActionDefinitionEntity;

import java.util.Map;
import java.util.function.Function;

import static no.mnemonic.commons.utilities.collections.MapUtils.Pair.T;

public class TriggerActionDefinitionConverter implements Function<TriggerActionDefinitionEntity, TriggerActionDefinition> {

  private final Function<String, FunctionInfo> functionResolver;

  private TriggerActionDefinitionConverter(Function<String, FunctionInfo> functionResolver) {
    this.functionResolver = ObjectUtils.notNull(functionResolver, "Cannot instantiate TriggerActionDefinitionConverter without 'functionResolver'.");
  }

  @Override
  public TriggerActionDefinition apply(TriggerActionDefinitionEntity entity) {
    if (entity == null) return null;
    return TriggerActionDefinition.builder()
        .setId(entity.getId())
        .setName(entity.getName())
        .setDescription(entity.getDescription())
        .setTriggerActionClass(entity.getTriggerActionClass())
        .setRequiredPermission(functionResolver.apply(entity.getRequiredPermission()))
        .setInitParameters(MapUtils.map(entity.getInitParameters()))
        .setTriggerParameters(convertTriggerParameters(entity))
        .build();
  }

  private Map<String, ParameterDefinition> convertTriggerParameters(TriggerActionDefinitionEntity entity) {
    if (entity.getTriggerParameters() == null) return null;
    return MapUtils.map(entity.getTriggerParameters().entrySet(), e -> T(e.getKey(), convertParameterDefinition(e.getValue())));
  }

  private ParameterDefinition convertParameterDefinition(ParameterDefinitionEntity entity) {
    if (entity == null) return null;
    return ParameterDefinition.builder()
        .setDescription(entity.getDescription())
        .setRequired(entity.isRequired())
        .setDefaultValue(entity.getDefaultValue())
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Function<String, FunctionInfo> functionResolver;

    private Builder() {
    }

    public TriggerActionDefinitionConverter build() {
      return new TriggerActionDefinitionConverter(functionResolver);
    }

    public Builder setFunctionResolver(Function<String, FunctionInfo> functionResolver) {
      this.functionResolver = functionResolver;
      return this;
    }
  }
}
