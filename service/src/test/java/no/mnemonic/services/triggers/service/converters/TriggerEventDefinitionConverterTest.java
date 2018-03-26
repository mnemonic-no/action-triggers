package no.mnemonic.services.triggers.service.converters;

import no.mnemonic.services.triggers.api.model.v1.FunctionInfo;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.service.dao.TriggerEventDefinitionEntity;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TriggerEventDefinitionConverterTest {

  private final Function<String, FunctionInfo> functionResolver = name -> FunctionInfo.builder()
      .setId(UUID.randomUUID())
      .setName(name)
      .build();
  private final TriggerEventDefinitionConverter converter = TriggerEventDefinitionConverter.builder()
      .setFunctionResolver(functionResolver)
      .build();

  @Test(expected = RuntimeException.class)
  public void testInitializeWithoutFunctionResolver() {
    TriggerEventDefinitionConverter.builder().build();
  }

  @Test
  public void testConverterWithNullEntity() {
    assertNull(converter.apply(null));
  }

  @Test
  public void testConverterWithCompleteEntity() {
    TriggerEventDefinitionEntity entity = createEntity();
    assertModel(entity, converter.apply(entity));
  }

  private TriggerEventDefinitionEntity createEntity() {
    return TriggerEventDefinitionEntity.builder()
        .setId(UUID.randomUUID())
        .setService("service")
        .setName("name")
        .setPublicPermission("publicPermission")
        .setRoleBasedPermission("roleBasedPermission")
        .setPrivatePermission("privatePermission")
        .addScope("name1", "permission1")
        .addScope("name2", "permission2")
        .addScope("name3", "permission3")
        .build();
  }

  private void assertModel(TriggerEventDefinitionEntity entity, TriggerEventDefinition model) {
    assertEquals(entity.getId(), model.getId());
    assertEquals(entity.getService(), model.getService());
    assertEquals(entity.getName(), model.getName());
    assertEquals(entity.getPublicPermission(), model.getPublicPermission().getName());
    assertEquals(entity.getRoleBasedPermission(), model.getRoleBasedPermission().getName());
    assertEquals(entity.getPrivatePermission(), model.getPrivatePermission().getName());
    for (String scope : entity.getScopes().keySet()) {
      assertEquals(entity.getScopes().get(scope), model.getScopes().get(scope).getName());
    }
  }
}
