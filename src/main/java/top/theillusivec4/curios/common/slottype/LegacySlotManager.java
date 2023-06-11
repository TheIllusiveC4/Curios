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
import java.util.stream.Stream;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.common.slottype.SlotType.Builder;

public class LegacySlotManager {

  private static final Map<String, Builder> IMC_BUILDERS = new HashMap<>();
  private static final Map<String, Set<String>> IDS_TO_MODS = new HashMap<>();

  public static Map<String, Set<String>> getIdsToMods() {
    return ImmutableMap.copyOf(IDS_TO_MODS);
  }

  public static Map<String, Builder> getImcBuilders() {
    return ImmutableMap.copyOf(IMC_BUILDERS);
  }

  public static void buildImcSlotTypes(Stream<InterModComms.IMCMessage> register,
                                       Stream<IMCMessage> modify) {
    IMC_BUILDERS.clear();
    processImc(register, true);
    processImc(modify, false);
  }

  private static void processImc(Stream<InterModComms.IMCMessage> messages, boolean create) {
    TreeMap<String, List<SlotTypeMessage>> messageMap = new TreeMap<>();
    List<IMCMessage> messageList = messages.toList();

    messageList.forEach(msg -> {
      Object messageObject = msg.messageSupplier().get();

      if (messageObject instanceof SlotTypeMessage) {
        messageMap.computeIfAbsent(msg.senderModId(), k -> new ArrayList<>())
            .add((SlotTypeMessage) messageObject);
      } else if (messageObject instanceof Iterable<?> iterable) {
        Iterator<?> iter = iterable.iterator();

        if (iter.hasNext()) {
          Object firstChild = iter.next();

          if (firstChild instanceof SlotTypeMessage) {
            messageMap.computeIfAbsent(msg.senderModId(), k -> new ArrayList<>())
                .add((SlotTypeMessage) firstChild);

            iter.forEachRemaining(
                (child) -> messageMap.computeIfAbsent(msg.senderModId(), k -> new ArrayList<>())
                    .add((SlotTypeMessage) child));
          }
        }
      }
    });

    for (Map.Entry<String, List<SlotTypeMessage>> entry : messageMap.entrySet()) {
      String modId = entry.getKey();

      for (SlotTypeMessage msg : entry.getValue()) {
        String id = msg.getIdentifier();
        Builder builder = IMC_BUILDERS.get(id);

        if (builder == null && create) {
          builder = new Builder(id);
          IMC_BUILDERS.put(id, builder);
          IDS_TO_MODS.computeIfAbsent(id, (k) -> new HashSet<>()).add(modId);
        }

        if (builder != null) {
          builder.size(msg.getSize()).useNativeGui(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
          SlotTypeMessage.Builder preset = SlotTypePreset.findPreset(id)
              .map(SlotTypePreset::getMessageBuilder).orElse(null);
          SlotTypeMessage presetMsg = preset != null ? preset.build() : null;

          if (msg.getIcon() == null && presetMsg != null && presetMsg.getIcon() != null) {
            builder.icon(presetMsg.getIcon());
          } else if (msg.getIcon() != null) {
            builder.icon(msg.getIcon());
          }

          if (msg.getPriority() == null && presetMsg != null && presetMsg.getPriority() != null) {
            builder.order(presetMsg.getPriority());
          } else if (msg.getPriority() != null) {
            builder.order(msg.getPriority());
          }
        }
      }
    }
  }
}
