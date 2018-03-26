package no.mnemonic.services.triggers.service.converters;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;
import no.mnemonic.services.triggers.api.model.v1.AccessMode;
import no.mnemonic.services.triggers.api.model.v1.OrganizationInfo;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.service.dao.TriggerRuleEntity;

import java.util.UUID;
import java.util.function.Function;

public class TriggerRuleConverter implements Function<TriggerRuleEntity, TriggerRule> {

  private final Function<UUID, OrganizationInfo> organizationResolver;
  private final Function<String, TriggerActionDefinition> triggerActionResolver;

  private TriggerRuleConverter(Function<UUID, OrganizationInfo> organizationResolver,
                               Function<String, TriggerActionDefinition> triggerActionResolver) {
    this.organizationResolver = ObjectUtils.notNull(organizationResolver, "Cannot instantiate TriggerRuleConverter without 'organizationResolver'.");
    this.triggerActionResolver = ObjectUtils.notNull(triggerActionResolver, "Cannot instantiate TriggerRuleConverter without 'triggerActionResolver'.");
  }

  @Override
  public TriggerRule apply(TriggerRuleEntity entity) {
    if (entity == null) return null;
    return TriggerRule.builder()
        .setId(entity.getId())
        .setService(entity.getService())
        .setExpression(entity.getExpression())
        .setAccessMode(ObjectUtils.ifNotNull(entity.getAccessMode(), mode -> AccessMode.valueOf(mode.name())))
        .setTriggerAction(ObjectUtils.ifNotNull(triggerActionResolver.apply(entity.getTriggerAction()), TriggerActionDefinition::toInfo))
        .setEvents(SetUtils.set(entity.getEvents()))
        .setOrganizations(SetUtils.set(entity.getOrganizations(), organizationResolver))
        .setScopes(SetUtils.set(entity.getScopes()))
        .setTriggerParameters(MapUtils.map(entity.getTriggerParameters()))
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Function<UUID, OrganizationInfo> organizationResolver;
    private Function<String, TriggerActionDefinition> triggerActionResolver;

    private Builder() {
    }

    public TriggerRuleConverter build() {
      return new TriggerRuleConverter(organizationResolver, triggerActionResolver);
    }

    public Builder setOrganizationResolver(Function<UUID, OrganizationInfo> organizationResolver) {
      this.organizationResolver = organizationResolver;
      return this;
    }

    public Builder setTriggerActionResolver(Function<String, TriggerActionDefinition> triggerActionResolver) {
      this.triggerActionResolver = triggerActionResolver;
      return this;
    }
  }
}
