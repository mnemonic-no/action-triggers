package no.mnemonic.services.triggers.service.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class YamlReaderTest {

  private Path tmpYamlFile;

  @BeforeEach
  public void setUp(@TempDir Path tempDir) {
    tmpYamlFile = tempDir.resolve(UUID.randomUUID() + ".yaml");
  }

  @Test
  public void testInitializeWithoutFilePath() {
    assertThrows(RuntimeException.class, () -> new YamlReader<>(null, TriggerEventDefinitionEntity.class));
  }

  @Test
  public void testInitializeWithoutEntityClass() {
    assertThrows(RuntimeException.class, () -> new YamlReader<>(Paths.get("/non/existing/file.yaml"), null));
  }

  @Test
  public void testInitializeWithNonExistingFile() {
    assertThrows(IllegalArgumentException.class, () -> new YamlReader<>(Paths.get("/non/existing/file.yaml"), TriggerEventDefinitionEntity.class));
  }

  @Test
  public void testReadAllWithEmptyFile() throws Exception {
    writeContent("");
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllIgnoresUnknownProperty() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        name: name
        unknown: something
        """);
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(1, reader.readAll().size());
  }

  @Test
  public void testReadAllSkipsInvalidEntity() throws Exception {
    writeContent("""
        ---
        id: 123e4567-e89b-12d3-a456-426655441111
        ---
        id: 123e4567-e89b-12d3-a456-426655442222
        service: service
        name: name
        """);
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(1, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerEventDefinitions() {
    String yaml = ClassLoader.getSystemResource("triggerEventDefinition.yaml").getPath();
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(Paths.get(yaml), TriggerEventDefinitionEntity.class);
    assertTriggerEventDefinitions(reader.readAll());
  }

  @Test
  public void testReadAllTriggerEventDefinitionsMinimal() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        name: name
        """);
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(1, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerEventDefinitionsMinimalMissingID() throws Exception {
    writeContent("service: service\nname: name");
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerEventDefinitionsMinimalMissingService() throws Exception {
    writeContent("id: 123e4567-e89b-12d3-a456-426655441111\nname: name");
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerEventDefinitionsMinimalMissingName() throws Exception {
    writeContent("id: 123e4567-e89b-12d3-a456-426655441111\nservice: service");
    YamlReader<TriggerEventDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerEventDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerActionDefinitions() {
    String yaml = ClassLoader.getSystemResource("triggerActionDefinition.yaml").getPath();
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(Paths.get(yaml), TriggerActionDefinitionEntity.class);
    assertTriggerActionDefinitions(reader.readAll());
  }

  @Test
  public void testReadAllTriggerActionDefinitionsMinimal() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        name: name
        description: description
        triggerActionClass: triggerActionClass
        requiredPermission: requiredPermission
        """);
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerActionDefinitionEntity.class);
    assertEquals(1, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerActionDefinitionsMinimalMissingID() throws Exception {
    writeContent("""
        name: name
        description: description
        triggerActionClass: triggerActionClass
        requiredPermission: requiredPermission
        """);
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerActionDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerActionDefinitionsMinimalMissingName() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        description: description
        triggerActionClass: triggerActionClass
        requiredPermission: requiredPermission
        """);
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerActionDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerActionDefinitionsMinimalMissingDescription() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        name: name
        triggerActionClass: triggerActionClass
        requiredPermission: requiredPermission
        """);
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerActionDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerActionDefinitionsMinimalMissingTriggerActionClass() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        name: name
        description: description
        requiredPermission: requiredPermission
        """);
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerActionDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerActionDefinitionsMinimalMissingRequiredPermission() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        name: name
        description: description
        triggerActionClass: triggerActionClass
        """);
    YamlReader<TriggerActionDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, TriggerActionDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllParameterDefinitionsMinimal() throws Exception {
    writeContent("description: description\nrequired: false");
    YamlReader<ParameterDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, ParameterDefinitionEntity.class);
    assertEquals(1, reader.readAll().size());
  }

  @Test
  public void testReadAllParameterDefinitionsMinimalMissingDescription() throws Exception {
    writeContent("required: false");
    YamlReader<ParameterDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, ParameterDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllParameterDefinitionsMinimalMissingRequired() throws Exception {
    writeContent("description: description");
    YamlReader<ParameterDefinitionEntity> reader = new YamlReader<>(tmpYamlFile, ParameterDefinitionEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRules() {
    String yaml = ClassLoader.getSystemResource("triggerRule.yaml").getPath();
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(Paths.get(yaml), TriggerRuleEntity.class);
    assertTriggerRules(reader.readAll());
  }

  @Test
  public void testReadAllTriggerRulesMinimal() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        events: [ event ]
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        accessMode: Public
        expression: expression
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(1, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingID() throws Exception {
    writeContent("""
        service: service
        events: [ event ]
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        accessMode: Public
        expression: expression
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingService() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        events: [ event ]
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        accessMode: Public
        expression: expression
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingEvents() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        accessMode: Public
        expression: expression
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingOrganizations() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        events: [ event ]
        accessMode: Public
        expression: expression
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingAccessMode() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        events: [ event ]
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        expression: expression
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingExpression() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        events: [ event ]
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        accessMode: Public
        triggerAction: triggerAction
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  @Test
  public void testReadAllTriggerRulesMinimalMissingTriggerAction() throws Exception {
    writeContent("""
        id: 123e4567-e89b-12d3-a456-426655441111
        service: service
        events: [ event ]
        organizations: [ 123e4567-e89b-12d3-a456-426655441111 ]
        accessMode: Public
        expression: expression
        """);
    YamlReader<TriggerRuleEntity> reader = new YamlReader<>(tmpYamlFile, TriggerRuleEntity.class);
    assertEquals(0, reader.readAll().size());
  }

  private void assertTriggerEventDefinitions(Collection<TriggerEventDefinitionEntity> entities) {
    assertEquals(3, entities.size());
    for (TriggerEventDefinitionEntity entity : entities) {
      assertNotNull(entity.getId());
      assertTrue(entity.getService().startsWith("service"));
      assertTrue(entity.getName().startsWith("event"));
      assertEquals("allowPublicAccess", entity.getPublicPermission());
      assertEquals("allowRoleBasedAccess", entity.getRoleBasedPermission());
      assertEquals("allowPrivateAccess", entity.getPrivatePermission());
      assertEquals(3, entity.getScopes().size());
    }
  }

  private void assertTriggerActionDefinitions(Collection<TriggerActionDefinitionEntity> entities) {
    assertEquals(3, entities.size());
    for (TriggerActionDefinitionEntity entity : entities) {
      assertNotNull(entity.getId());
      assertTrue(entity.getName().startsWith("action"));
      assertEquals("Longer description of an action definition", entity.getDescription());
      assertEquals("no.mnemonic.services.triggers.action.ExampleAction", entity.getTriggerActionClass());
      assertEquals("allowExampleAction", entity.getRequiredPermission());
      assertEquals(3, entity.getInitParameters().size());
      assertParameterDefinitions(entity.getTriggerParameters());
    }
  }

  private void assertParameterDefinitions(Map<String, ParameterDefinitionEntity> triggerParameters) {
    assertEquals(2, triggerParameters.size());
    for (ParameterDefinitionEntity entity : triggerParameters.values()) {
      assertNotNull(entity.getDescription());
      if (entity.isRequired()) {
        assertNull(entity.getDefaultValue());
      } else {
        assertNotNull(entity.getDefaultValue());
      }
    }
  }

  private void assertTriggerRules(Collection<TriggerRuleEntity> entities) {
    assertEquals(3, entities.size());
    for (TriggerRuleEntity entity : entities) {
      assertNotNull(entity.getId());
      assertTrue(entity.getService().startsWith("service"));
      assertEquals(3, entity.getEvents().size());
      assertEquals(2, entity.getOrganizations().size());
      assertEquals(3, entity.getScopes().size());
      assertNotNull(entity.getAccessMode());
      assertEquals("someExpressionToEvaluate", entity.getExpression());
      assertTrue(entity.getTriggerAction().startsWith("action"));
      assertEquals(3, entity.getTriggerParameters().size());
    }
  }

  private void writeContent(String content) throws Exception {
    try (FileWriter writer = new FileWriter(tmpYamlFile.toFile())) {
      writer.write(content);
    }
  }
}
