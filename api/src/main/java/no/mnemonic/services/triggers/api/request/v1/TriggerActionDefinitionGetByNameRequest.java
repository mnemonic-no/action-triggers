package no.mnemonic.services.triggers.api.request.v1;

public class TriggerActionDefinitionGetByNameRequest {

  private String name;

  public String getName() {
    return name;
  }

  public TriggerActionDefinitionGetByNameRequest setName(String name) {
    this.name = name;
    return this;
  }
}
