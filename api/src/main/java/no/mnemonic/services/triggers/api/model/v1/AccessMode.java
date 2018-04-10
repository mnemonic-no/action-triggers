package no.mnemonic.services.triggers.api.model.v1;

public enum AccessMode {
  Public(0), RoleBased(1), Private(2);

  private final int order;

  AccessMode(int order) {
    this.order = order;
  }

  public boolean isLessRestricted(String mode) {
    return this.order < valueOf(mode).order;
  }
}
