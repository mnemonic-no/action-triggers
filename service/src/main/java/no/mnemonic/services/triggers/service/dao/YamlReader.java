package no.mnemonic.services.triggers.service.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;
import no.mnemonic.commons.utilities.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YamlReader<T> {

  private static final Logger LOGGER = Logging.getLogger(YamlReader.class);
  private static final ObjectMapper MAPPER = YAMLMapper.builder()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  private final Path filePath;
  private final Class<T> entityClass;
  private final ObjectReader reader;

  public YamlReader(Path filePath, Class<T> entityClass) {
    this.filePath = ObjectUtils.notNull(filePath, "'filePath' is required!");
    this.entityClass = ObjectUtils.notNull(entityClass, "'entityClass' is required!");
    this.reader = MAPPER.readerFor(entityClass);

    // Fail early if file is not readable.
    if (!Files.isReadable(filePath)) throw new IllegalArgumentException("Cannot read file: " + filePath);
  }

  public Collection<T> readAll() {
    List<T> result = new ArrayList<>();

    try (InputStream data = new FileInputStream(filePath.toFile());
         MappingIterator<T> values = reader.readValues(data)) {
      while (values.hasNext()) {
        try {
          result.add(values.nextValue());
        } catch (IOException ex) {
          // Reading a single JSON entity failed, e.g. because a required field is missing. Skip it.
          LOGGER.warning(ex, "Cannot deserialize entity for class %s. Skip it.", entityClass.getSimpleName());
        }
      }
    } catch (IOException ex) {
      // Unexpected exception when setting up reader. Re-throw as runtime exception.
      throw new IllegalStateException("Cannot read entities from file: " + filePath, ex);
    }

    return result;
  }

}
