package no.mnemonic.services.triggers.service.converters;

import no.mnemonic.services.triggers.api.model.v1.FunctionInfo;
import no.mnemonic.services.triggers.api.model.v1.ParameterDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.service.dao.ParameterDefinitionEntity;
import no.mnemonic.services.triggers.service.dao.TriggerActionDefinitionEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerActionDefinitionConverterTest {

  private final Function<String, FunctionInfo> functionResolver = name -> FunctionInfo.builder()
      .setId(UUID.randomUUID())
      .setName(name)
      .build();
  private final TriggerActionDefinitionConverter converter = TriggerActionDefinitionConverter.builder()
      .setFunctionResolver(functionResolver)
      .build();

  @Test
  public void testInitializeWithoutFunctionResolver() {
    assertThrows(RuntimeException.class, () -> TriggerActionDefinitionConverter.builder().build());
  }

  @Test
  public void testConverterWithNullEntity() {
    assertNull(converter.apply(null));
  }

  @Test
  public void testConverterWithCompleteEntity() {
    TriggerActionDefinitionEntity entity = createEntity();
    assertModel(entity, converter.apply(entity));
  }

  private TriggerActionDefinitionEntity createEntity() {
    return TriggerActionDefinitionEntity.builder()
        .setId(UUID.randomUUID())
        .setName("name")
        .setDescription("description")
        .setTriggerActionClass("triggerActionClass")
        .setRequiredPermission("requiredPermission")
        .addInitParameter("name1", "value1")
        .addInitParameter("name2", "value2")
        .addInitParameter("name3", "value3")
        .addTriggerParameter("name1", ParameterDefinitionEntity.builder()
            .setDescription("description")
            .setRequired(true)
            .build())
        .addTriggerParameter("name2", ParameterDefinitionEntity.builder()
            .setDescription("description")
            .setRequired(false)
            .setDefaultValue("defaultValue")
            .build())
        .build();
  }

  private void assertModel(TriggerActionDefinitionEntity entity, TriggerActionDefinition model) {
    assertEquals(entity.getId(), model.getId());
    assertEquals(entity.getName(), model.getName());
    assertEquals(entity.getDescription(), model.getDescription());
    assertEquals(entity.getTriggerActionClass(), model.getTriggerActionClass());
    assertEquals(entity.getRequiredPermission(), model.getRequiredPermission().getName());
    assertEquals(entity.getInitParameters(), model.getInitParameters());
    for (String parameter : entity.getTriggerParameters().keySet()) {
      ParameterDefinitionEntity parameterEntity = entity.getTriggerParameters().get(parameter);
      ParameterDefinition parameterModel = model.getTriggerParameters().get(parameter);
      assertEquals(parameterEntity.getDescription(), parameterModel.getDescription());
      assertEquals(parameterEntity.isRequired(), parameterModel.isRequired());
      assertEquals(parameterEntity.getDefaultValue(), parameterModel.getDefaultValue());
    }
  }
}
