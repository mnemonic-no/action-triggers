package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.CollectionUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.api.request.v1.TriggerRuleSearchRequest;
import no.mnemonic.services.triggers.service.dao.TriggerRuleEntity;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TriggerRuleSearchDelegate {

  private final Supplier<Collection<TriggerRuleEntity>> entitiesSupplier;
  private final Function<TriggerRuleEntity, TriggerRule> entityConverter;

  private TriggerRuleSearchDelegate(Supplier<Collection<TriggerRuleEntity>> entitiesSupplier,
                                    Function<TriggerRuleEntity, TriggerRule> entityConverter) {
    this.entitiesSupplier = ObjectUtils.notNull(entitiesSupplier, "Cannot instantiate TriggerRuleSearchDelegate without 'entitiesSupplier'.");
    this.entityConverter = ObjectUtils.notNull(entityConverter, "Cannot instantiate TriggerRuleSearchDelegate without 'entityConverter'.");
  }

  public Iterable<TriggerRule> handle(TriggerRuleSearchRequest request) throws InvalidArgumentException {
    if (request == null) throw new InvalidArgumentException("Request object is required.");

    return entitiesSupplier.get().stream()
        .filter(rule -> CollectionUtils.isEmpty(request.getService()) || request.getService().contains(rule.getService()))
        .filter(rule -> CollectionUtils.isEmpty(request.getEvent()) || SetUtils.intersects(request.getEvent(), rule.getEvents()))
        .map(entityConverter)
        .collect(Collectors.toList());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Collection<TriggerRuleEntity>> entitiesSupplier;
    private Function<TriggerRuleEntity, TriggerRule> entityConverter;

    private Builder() {
    }

    public TriggerRuleSearchDelegate build() {
      return new TriggerRuleSearchDelegate(entitiesSupplier, entityConverter);
    }

    public Builder setEntitiesSupplier(Supplier<Collection<TriggerRuleEntity>> entitiesSupplier) {
      this.entitiesSupplier = entitiesSupplier;
      return this;
    }

    public Builder setEntityConverter(Function<TriggerRuleEntity, TriggerRule> entityConverter) {
      this.entityConverter = entityConverter;
      return this;
    }
  }
}
