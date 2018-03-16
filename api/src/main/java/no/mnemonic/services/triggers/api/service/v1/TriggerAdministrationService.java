package no.mnemonic.services.triggers.api.service.v1;

import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.api.request.v1.*;

/**
 * Minimal definition of the TriggerAdministrationService required by worker. It will be changed / extended in the future!
 */
public interface TriggerAdministrationService {

  /**
   * Fetch a single TriggerActionDefinition by name.
   *
   * @param request Request identifying TriggerActionDefinition.
   * @return TriggerActionDefinition identified by name.
   * @throws InvalidArgumentException If the request contains invalid parameters.
   * @throws ObjectNotFoundException  If the requested TriggerActionDefinition could not be found.
   */
  default TriggerActionDefinition getTriggerActionDefinition(TriggerActionDefinitionGetByNameRequest request)
      throws InvalidArgumentException, ObjectNotFoundException {
    throw new UnsupportedOperationException();
  }

  /**
   * Fetch multiple TriggerActionDefinitions by a search request.
   *
   * @param request Request limiting the returned TriggerActionDefinitions.
   * @return All TriggerActionDefinitions fulfilling the search parameters.
   * @throws InvalidArgumentException If the request contains invalid parameters.
   */
  default Iterable<TriggerActionDefinition> searchTriggerActionDefinitions(TriggerActionDefinitionSearchRequest request)
      throws InvalidArgumentException {
    throw new UnsupportedOperationException();
  }

  /**
   * Fetch a single TriggerEventDefinition by service name and event name.
   *
   * @param request Request identifying TriggerEventDefinition.
   * @return TriggerEventDefinition identified by service name and event name.
   * @throws InvalidArgumentException If the request contains invalid parameters.
   * @throws ObjectNotFoundException  If the requested TriggerEventDefinition could not be found.
   */
  default TriggerEventDefinition getTriggerEventDefinition(TriggerEventDefinitionGetByServiceEventRequest request)
      throws InvalidArgumentException, ObjectNotFoundException {
    throw new UnsupportedOperationException();
  }

  /**
   * Fetch multiple TriggerEventDefinitions by a search request.
   *
   * @param request Request limiting the returned TriggerEventDefinitions.
   * @return All TriggerEventDefinitions fulfilling the search parameters.
   * @throws InvalidArgumentException If the request contains invalid parameters.
   */
  default Iterable<TriggerEventDefinition> searchTriggerEventDefinitions(TriggerEventDefinitionSearchRequest request)
      throws InvalidArgumentException {
    throw new UnsupportedOperationException();
  }

  /**
   * Fetch multiple TriggerRules by a search request.
   *
   * @param request Request limiting the returned TriggerRules.
   * @return All TriggerRules fulfilling the search parameters.
   * @throws InvalidArgumentException If the request contains invalid parameters.
   */
  default Iterable<TriggerRule> searchTriggerRules(TriggerRuleSearchRequest request)
      throws InvalidArgumentException {
    throw new UnsupportedOperationException();
  }

}
