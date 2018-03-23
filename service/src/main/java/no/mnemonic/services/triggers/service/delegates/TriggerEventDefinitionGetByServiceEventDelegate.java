package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.StringUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerEventDefinitionGetByServiceEventRequest;
import no.mnemonic.services.triggers.service.dao.TriggerEventDefinitionEntity;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class TriggerEventDefinitionGetByServiceEventDelegate {

  private final Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier;
  private final Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter;

  private TriggerEventDefinitionGetByServiceEventDelegate(Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier,
                                                          Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter) {
    this.entitiesSupplier = ObjectUtils.notNull(entitiesSupplier, "Cannot instantiate TriggerEventDefinitionGetByServiceEventDelegate without 'entitiesSupplier'.");
    this.entityConverter = ObjectUtils.notNull(entityConverter, "Cannot instantiate TriggerEventDefinitionGetByServiceEventDelegate without 'entityConverter'.");
  }

  public TriggerEventDefinition handle(TriggerEventDefinitionGetByServiceEventRequest request)
      throws InvalidArgumentException, ObjectNotFoundException {
    if (request == null) throw new InvalidArgumentException("Request object is required.");
    if (StringUtils.isBlank(request.getService())) throw new InvalidArgumentException("'service' parameter is required in request.");
    if (StringUtils.isBlank(request.getEvent())) throw new InvalidArgumentException("'event' parameter is required in request.");

    return entitiesSupplier.get().stream()
        .filter(event -> Objects.equals(event.getService(), request.getService()))
        .filter(event -> Objects.equals(event.getName(), request.getEvent()))
        .map(entityConverter)
        .findFirst()
        .orElseThrow(() -> new ObjectNotFoundException(String.format("TriggerEventDefinition with service = %s and name = %s does not exist.", request.getService(), request.getEvent())));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Collection<TriggerEventDefinitionEntity>> entitiesSupplier;
    private Function<TriggerEventDefinitionEntity, TriggerEventDefinition> entityConverter;

    private Builder() {
    }

    public TriggerEventDefinitionGetByServiceEventDelegate build() {
      return new TriggerEventDefinitionGetByServiceEventDelegate(entitiesSupplier, entityConverter);
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
