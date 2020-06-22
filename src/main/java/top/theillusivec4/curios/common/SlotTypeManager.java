package top.theillusivec4.curios.common;

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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.imc.CurioImcMessage;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.common.CuriosConfig.CuriosSettings.CuriosSetting;
import top.theillusivec4.curios.common.SlotType.Builder;

public class SlotTypeManager {

  private static Map<String, Builder> slotTypeBuilders = new HashMap<>();

  public static void buildImcSlotTypes(Stream<InterModComms.IMCMessage> register,
      Stream<IMCMessage> modify) {
    processImc(register, true);
    processImc(modify, false);
  }

  public static void buildConfigSlotTypes() {
    List<CuriosSetting> settings = CuriosConfig.curios;
    settings.forEach(setting -> {
      String id = setting.identifier;

      if (id != null && id.isEmpty()) {
        Curios.LOGGER.error("Missing identifier in curios config, skipping...");
        return;
      }
      Builder builder = slotTypeBuilders.get(id);
      boolean force = setting.override != null ? setting.override : false;

      if (builder == null) {
        builder = new Builder(id);
      }

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
  }

  public static void buildSlotTypes() {
    Map<String, ISlotType> slotTypes = new HashMap<>();
    Map<String, ResourceLocation> slotIcons = new HashMap<>();
    slotTypeBuilders.forEach((id, builder) -> {
      ISlotType type = builder.build();
      slotTypes.put(id, type);
      slotIcons.put(id, type.getIcon());
    });
    CuriosApi.idToType = slotTypes;
    CuriosApi.idToIcon = slotIcons;
  }

  private static void processImc(Stream<InterModComms.IMCMessage> messages, boolean create) {
    TreeMap<String, List<CurioImcMessage>> messageMap = new TreeMap<>();
    List<IMCMessage> list = messages.collect(Collectors.toList());

    list.forEach(msg -> {
      Object obj = msg.getMessageSupplier().get();

      if (obj instanceof CurioImcMessage) {
        messageMap.computeIfAbsent(msg.getSenderModId(), k -> new ArrayList<>())
            .add((CurioImcMessage) obj);
      }
    });

    messageMap.values().forEach(msgList -> msgList.forEach(msg -> {
      String id = msg.getIdentifier();
      Builder builder = slotTypeBuilders.get(id);

      if (builder == null && create) {
        builder = new Builder(id);
        slotTypeBuilders.put(id, builder);
      }

      if (builder != null) {
        builder.icon(msg.getIcon()).priority(msg.getPriority()).size(msg.getSize())
            .locked(msg.isLocked()).visible(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
      }
    }));
  }
}
