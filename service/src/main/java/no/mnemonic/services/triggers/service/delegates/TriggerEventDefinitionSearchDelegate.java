package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.CollectionUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerEventDefinitionSearchRequest;
import no.mnemonic.services.triggers.service.dao.TriggerEventDefinitionEntity;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TriggerEventDefinitionSearchDelegate {

  private final Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier;
  private final Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter;

  private TriggerEventDefinitionSearchDelegate(Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier,
                                               Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter) {
    this.entitiesSupplier = ObjectUtils.notNull(entitiesSupplier, "Cannot instantiate TriggerEventDefinitionSearchDelegate without 'entitiesSupplier'.");
    this.entityConverter = ObjectUtils.notNull(entityConverter, "Cannot instantiate TriggerEventDefinitionSearchDelegate without 'entityConverter'.");
  }

  public Iterable<TriggerEventDefinition> handle(TriggerEventDefinitionSearchRequest request) throws InvalidArgumentException {
    if (request == null) throw new InvalidArgumentException("Request object is required.");

    return entitiesSupplier.get().stream()
        .filter(event -> CollectionUtils.isEmpty(request.getService()) || request.getService().contains(event.getService()))
        .filter(event -> CollectionUtils.isEmpty(request.getEvent()) || request.getEvent().contains(event.getName()))
        .map(entityConverter)
        .collect(Collectors.toList());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier;
    private Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter;

    private Builder() {
    }

    public TriggerEventDefinitionSearchDelegate build() {
      return new TriggerEventDefinitionSearchDelegate(entitiesSupplier, entityConverter);
    }

    public Builder setEntitiesSupplier(Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier) {
      this.entitiesSupplier = entitiesSupplier;
      return this;
    }

    public Builder setEntityConverter(Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter) {
      this.entityConverter = entityConverter;
      return this;
    }
  }
}
