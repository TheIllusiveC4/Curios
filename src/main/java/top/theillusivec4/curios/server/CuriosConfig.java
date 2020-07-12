package top.theillusivec4.curios.server;

import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.RecordSerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.RecordConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.CuriosCommon;

public class CuriosConfig {

  private static final PropertyMirror<Map<String, CurioSetting>> CURIO_SETTINGS;
  private static final RecordConfigType<CurioSetting> CONFIG_TYPE;
  private static final RecordSerializableType SERIALIZABLE_TYPE;
  private static final ConfigTree INSTANCE;

  static {
    Map<String, SerializableType<?>> fields = new HashMap<>();
    fields.put("icon", ConfigTypes.STRING.getSerializedType());
    fields.put("priority", ConfigTypes.INTEGER.getSerializedType());
    fields.put("size", ConfigTypes.INTEGER.getSerializedType());
    fields.put("locked", ConfigTypes.BOOLEAN.getSerializedType());
    fields.put("visible", ConfigTypes.BOOLEAN.getSerializedType());
    fields.put("hasCosmetic", ConfigTypes.BOOLEAN.getSerializedType());
    fields.put("override", ConfigTypes.BOOLEAN.getSerializedType());
    SERIALIZABLE_TYPE = new RecordSerializableType(fields);
    CONFIG_TYPE = new RecordConfigType<>(SERIALIZABLE_TYPE, CurioSetting.class,
        CurioSetting::deserialize, CurioSetting::serialize);
    CURIO_SETTINGS = PropertyMirror.create(ConfigTypes.makeMap(ConfigTypes.STRING, CONFIG_TYPE));

    ConfigTreeBuilder builder = ConfigTree.builder();
    builder.beginValue("curios", ConfigTypes.makeList(CONFIG_TYPE), new ArrayList<>())
        .withComment("List of curios").finishValue(CURIO_SETTINGS::mirror);
    INSTANCE = builder.build();
  }

  public static void init() {
    JanksonValueSerializer serializer = new JanksonValueSerializer(false);
    Path path = Paths.get("config", CuriosApi.MODID + ".json5");

    try (OutputStream stream = new BufferedOutputStream(
        Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
      FiberSerialization.serialize(INSTANCE, stream, serializer);
    } catch (IOException e) {
      CuriosCommon.LOGGER.error("Error serializing new config!");
      e.printStackTrace();
    }

    try (InputStream stream = new BufferedInputStream(
        Files.newInputStream(path, StandardOpenOption.READ, StandardOpenOption.CREATE))) {
      FiberSerialization.deserialize(INSTANCE, stream, serializer);
    } catch (IOException | ValueDeserializationException e) {
      CuriosCommon.LOGGER.error("Error deserializing config!");
      e.printStackTrace();
    }
    bake();
  }

  public static Map<String, CurioSetting> curios;

  public static void bake() {
    curios = CURIO_SETTINGS.getValue();
  }

  public static class CurioSetting {

    public String icon;
    public Integer priority;
    public Integer size;
    public Boolean locked;
    public Boolean visible;
    public Boolean hasCosmetic;
    public Boolean override;

    public static CurioSetting deserialize(Map<String, Object> fields) {
      CurioSetting setting = new CurioSetting();
      setting.icon = fields.get("icon") instanceof String ? (String) fields.get("icon") : null;
      setting.priority =
          fields.get("priority") instanceof Integer ? (Integer) fields.get("priority") : null;
      setting.size = fields.get("size") instanceof Integer ? (Integer) fields.get("size") : null;
      setting.locked =
          fields.get("locked") instanceof Boolean ? (Boolean) fields.get("locked") : null;
      setting.visible =
          fields.get("visible") instanceof Boolean ? (Boolean) fields.get("visible") : null;
      setting.hasCosmetic =
          fields.get("hasCosmetic") instanceof Boolean ? (Boolean) fields.get("hasCosmetic") : null;
      setting.override =
          fields.get("override") instanceof Boolean ? (Boolean) fields.get("override") : null;
      return setting;
    }

    public static Map<String, Object> serialize(CurioSetting setting) {
      Map<String, Object> serial = new HashMap<>();

      if (setting.icon != null) {
        serial.put("icon", setting.icon);
      }

      if (setting.priority != null) {
        serial.put("priority", setting.priority);
      }

      if (setting.size != null) {
        serial.put("size", setting.size);
      }

      if (setting.locked != null) {
        serial.put("locked", setting.locked);
      }

      if (setting.visible != null) {
        serial.put("visible", setting.visible);
      }

      if (setting.hasCosmetic != null) {
        serial.put("hasCosmetic", setting.hasCosmetic);
      }

      if (setting.override != null) {
        serial.put("override", setting.override);
      }
      return serial;
    }
  }
}
