package top.theillusivec4.curios.common.slottype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.CuriosConfig.CuriosSettings.CuriosSetting;
import top.theillusivec4.curios.common.slottype.SlotType.Builder;

public class SlotTypeManager {

  private static Map<String, Builder> imcBuilders = new HashMap<>();
  private static Map<String, Builder> configBuilders = new HashMap<>();

  public static void buildImcSlotTypes(Stream<InterModComms.IMCMessage> register,
      Stream<IMCMessage> modify) {
    imcBuilders.clear();
    processImc(register, true);
    processImc(modify, false);
  }

  public static void buildConfigSlotTypes() {
    configBuilders.clear();
    List<CuriosSetting> settings = CuriosConfig.curios;

    if (settings == null) {
      return;
    }

    settings.forEach(setting -> {
      String id = setting.identifier;

      if (id == null || id.isEmpty()) {
        Curios.LOGGER.error("Missing identifier in curios config, skipping...");
        return;
      }
      Builder builder = imcBuilders.get(id);
      boolean force = setting.override != null ? setting.override : false;

      if (builder == null) {
        builder = new Builder(id);
        SlotTypeMessage.Builder preset = SlotTypePreset.findPreset(id)
            .map(SlotTypePreset::getMessageBuilder).orElse(null);

        if (preset != null) {
          SlotTypeMessage msg = preset.build();
          builder.icon(msg.getIcon()).priority(msg.getPriority()).size(msg.getSize())
              .locked(msg.isLocked()).visible(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
        }
      } else {
        builder = new Builder(id).copyFrom(builder);
      }
      configBuilders.putIfAbsent(id, builder);

      if (setting.priority != null) {
        builder.priority(setting.priority, force);
      }

      if (setting.icon != null && !setting.icon.isEmpty()) {
        builder.icon(new ResourceLocation(setting.icon));
      }

      if (setting.size != null) {
        builder.size(setting.size, force);
      }

      if (setting.locked != null) {
        builder.locked(setting.locked, force);
      }

      if (setting.visible != null) {
        builder.visible(setting.visible, force);
      }

      if (setting.hasCosmetic != null) {
        builder.hasCosmetic(setting.hasCosmetic, force);
      }
    });
    imcBuilders.forEach((key, builder) -> configBuilders.putIfAbsent(key, builder));
  }

  public static void buildSlotTypes() {
    Map<String, Builder> builders = !configBuilders.isEmpty() ? configBuilders : imcBuilders;
    builders.values().forEach(builder -> CuriosApi.getSlotHelper().addSlotType(builder.build()));
  }

  private static void processImc(Stream<InterModComms.IMCMessage> messages, boolean create) {
    TreeMap<String, List<SlotTypeMessage>> messageMap = new TreeMap<>();
    List<IMCMessage> list = messages.collect(Collectors.toList());

    list.forEach(msg -> {
      Object obj = msg.getMessageSupplier().get();

      if (obj instanceof SlotTypeMessage) {
        messageMap.computeIfAbsent(msg.getSenderModId(), k -> new ArrayList<>())
            .add((SlotTypeMessage) obj);
      }
    });

    messageMap.values().forEach(msgList -> msgList.forEach(msg -> {
      String id = msg.getIdentifier();
      Builder builder = imcBuilders.get(id);

      if (builder == null && create) {
        builder = new Builder(id);
        imcBuilders.put(id, builder);
      }

      if (builder != null) {
        builder.icon(msg.getIcon()).priority(msg.getPriority()).size(msg.getSize())
            .locked(msg.isLocked()).visible(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
      }
    }));
  }
}
