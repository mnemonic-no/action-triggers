package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.ListUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionSearchRequest;
import no.mnemonic.services.triggers.service.dao.TriggerActionDefinitionEntity;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class TriggerActionDefinitionSearchDelegate {

  private final Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier;
  private final Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter;

  private TriggerActionDefinitionSearchDelegate(Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier,
                                                Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter) {
    this.entitiesSupplier = ObjectUtils.notNull(entitiesSupplier, "Cannot instantiate TriggerActionDefinitionSearchDelegate without 'entitiesSupplier'.");
    this.entityConverter = ObjectUtils.notNull(entityConverter, "Cannot instantiate TriggerActionDefinitionSearchDelegate without 'entityConverter'.");
  }

  public Iterable<TriggerActionDefinition> handle(TriggerActionDefinitionSearchRequest request) throws InvalidArgumentException {
    if (request == null) throw new InvalidArgumentException("Request object is required.");

    // No filter options have been defined yet, just return everything.
    return ListUtils.list(entitiesSupplier.get(), entityConverter);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier;
    private Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter;

    private Builder() {
    }

    public TriggerActionDefinitionSearchDelegate build() {
      return new TriggerActionDefinitionSearchDelegate(entitiesSupplier, entityConverter);
    }

    public Builder setEntitiesSupplier(Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier) {
      this.entitiesSupplier = entitiesSupplier;
      return this;
    }

    public Builder setEntityConverter(Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter) {
      this.entityConverter = entityConverter;
      return this;
    }
  }
}
