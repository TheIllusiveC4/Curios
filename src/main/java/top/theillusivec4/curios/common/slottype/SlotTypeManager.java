/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.common.slottype;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.CuriosConfig.CuriosSettings.CuriosSetting;
import top.theillusivec4.curios.common.slottype.SlotType.Builder;
import top.theillusivec4.curios.server.command.CurioArgumentType;

public class SlotTypeManager {

  private static Map<String, Builder> imcBuilders = new HashMap<>();
  private static Map<String, Builder> configBuilders = new HashMap<>();
  private static Map<String, Set<String>> idsToMods = new HashMap<>();

  public static Map<String, Set<String>> getIdsToMods() {
    return ImmutableMap.copyOf(idsToMods);
  }

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
              .visible(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
        }
      } else {
        builder = new Builder(id).copyFrom(builder);
      }
      configBuilders.putIfAbsent(id, builder);
      idsToMods.computeIfAbsent(id, (k) -> new HashSet<>()).add("config");

      if (setting.priority != null) {
        builder.priority(setting.priority, force);
      }

      if (setting.icon != null && !setting.icon.isEmpty()) {
        builder.icon(new ResourceLocation(setting.icon));
      }

      if (setting.size != null) {
        builder.size(setting.size, force);
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
    CurioArgumentType.slotIds = CuriosApi.getSlotHelper().getSlotTypeIds();
  }

  private static void processImc(Stream<InterModComms.IMCMessage> messages, boolean create) {
    TreeMap<String, List<SlotTypeMessage>> messageMap = new TreeMap<>();
    List<IMCMessage> messageList = messages.collect(Collectors.toList());

    messageList.forEach(msg -> {
      Object messageObject = msg.getMessageSupplier().get();

      if (messageObject instanceof SlotTypeMessage) {
        messageMap.computeIfAbsent(msg.getSenderModId(), k -> new ArrayList<>())
            .add((SlotTypeMessage) messageObject);
      } else if (messageObject instanceof Iterable) {
        Iterable<?> iterable = (Iterable<?>) messageObject;
        Iterator<?> iter = iterable.iterator();

        if (iter.hasNext()) {
          Object firstChild = iter.next();

          if (firstChild instanceof SlotTypeMessage) {
            messageMap.computeIfAbsent(msg.getSenderModId(), k -> new ArrayList<>())
                .add((SlotTypeMessage) firstChild);

            iter.forEachRemaining(
                (child) -> messageMap.computeIfAbsent(msg.getSenderModId(), k -> new ArrayList<>())
                    .add((SlotTypeMessage) child));
          }
        }
      }
    });

    for (Map.Entry<String, List<SlotTypeMessage>> entry : messageMap.entrySet()) {
      String modId = entry.getKey();

      for (SlotTypeMessage msg : entry.getValue()) {
        String id = msg.getIdentifier();
        Builder builder = imcBuilders.get(id);

        if (builder == null && create) {
          builder = new Builder(id);
          imcBuilders.put(id, builder);
          idsToMods.computeIfAbsent(id, (k) -> new HashSet<>()).add(modId);
        }

        if (builder != null) {
          builder.size(msg.getSize()).visible(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
          SlotTypeMessage.Builder preset = SlotTypePreset.findPreset(id)
              .map(SlotTypePreset::getMessageBuilder).orElse(null);
          SlotTypeMessage presetMsg = preset != null ? preset.build() : null;

          if (msg.getIcon() == null && presetMsg != null) {
            builder.icon(presetMsg.getIcon());
          } else {
            builder.icon(msg.getIcon());
          }

          if (msg.getPriority() == null && presetMsg != null) {
            builder.priority(presetMsg.getPriority());
          } else {
            builder.priority(msg.getPriority());
          }
        }
      }
    }
  }
}
