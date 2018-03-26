package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.StringUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionGetByNameRequest;
import no.mnemonic.services.triggers.service.dao.TriggerActionDefinitionEntity;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class TriggerActionDefinitionGetByNameDelegate {

  private final Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier;
  private final Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter;

  private TriggerActionDefinitionGetByNameDelegate(Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier,
                                                   Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter) {
    this.entitiesSupplier = ObjectUtils.notNull(entitiesSupplier, "Cannot instantiate TriggerActionDefinitionGetByNameDelegate without 'entitiesSupplier'.");
    this.entityConverter = ObjectUtils.notNull(entityConverter, "Cannot instantiate TriggerActionDefinitionGetByNameDelegate without 'entityConverter'.");
  }

  public TriggerActionDefinition handle(TriggerActionDefinitionGetByNameRequest request)
      throws InvalidArgumentException, ObjectNotFoundException {
    if (request == null) throw new InvalidArgumentException("Request object is required.");
    if (StringUtils.isBlank(request.getName())) throw new InvalidArgumentException("'name' parameter is required in request.");

    return entitiesSupplier.get().stream()
        .filter(action -> Objects.equals(action.getName(), request.getName()))
        .map(entityConverter)
        .findFirst()
        .orElseThrow(() -> new ObjectNotFoundException(String.format("TriggerActionDefinition with name = %s does not exist.", request.getName())));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Collection<TriggerActionDefinitionEntity>> entitiesSupplier;
    private Function<TriggerActionDefinitionEntity, TriggerActionDefinition> entityConverter;

    private Builder() {
    }

    public TriggerActionDefinitionGetByNameDelegate build() {
      return new TriggerActionDefinitionGetByNameDelegate(entitiesSupplier, entityConverter);
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
