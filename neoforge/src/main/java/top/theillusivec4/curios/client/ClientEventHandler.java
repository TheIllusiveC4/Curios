/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.client;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

public class ClientEventHandler {

  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Post evt) {

    if (KeyRegistry.openCurios.consumeClick() && Minecraft.getInstance().isWindowActive()) {
      PacketDistributor.sendToServer(new CPacketOpenCurios(ItemStack.EMPTY));
    }
  }

  @SubscribeEvent
  public void onAttributeTooltip(final AddAttributeTooltipsEvent evt) {
    AttributeTooltipContext context = evt.getContext();
    ItemStack stack = evt.getStack();
    GatherSkippedAttributeTooltipsEvent skipped =
        NeoForge.EVENT_BUS.post(new GatherSkippedAttributeTooltipsEvent(stack, context));

    if (skipped.isSkippingAll()) {
      return;
    }
    List<Component> attributesTooltip = new ArrayList<>();
    Player player = context.player();
    List<String> slots = getItemStackSlots(stack, player);

    for (String identifier : slots) {
      SlotContext slotContext = new SlotContext(identifier, player, 0, false, true);
      Multimap<Holder<Attribute>, AttributeModifier> attributes =
          CuriosApi.getAttributeModifiers(slotContext, CuriosApi.getSlotId(slotContext), stack);
      attributes.values().removeIf(modifier -> skipped.isSkipped(modifier.id()));

      if (attributes.isEmpty()) {
        continue;
      }
      attributesTooltip.add(Component.empty());
      attributesTooltip.add(
          Component.translatable("curios.modifiers." + identifier).withStyle(ChatFormatting.GOLD));

      if (player != null) {
        AttributeUtil.applyTextFor(
            stack,
            attributesTooltip::add,
            attributes,
            AttributeTooltipContext.of(player, context, context.flag()));
      }
    }
    evt.addTooltipLines(
        CuriosApi.getCurio(stack)
            .map(curio -> curio.getAttributesTooltip(attributesTooltip, context))
            .orElse(attributesTooltip)
            .toArray(new Component[0]));
  }

  @SubscribeEvent
  public void onTooltip(final ItemTooltipEvent evt) {
    ItemStack stack = evt.getItemStack();
    Player player = evt.getEntity();

    if (stack.isEmpty()) {
      return;
    }
    List<String> slots = getItemStackSlots(stack, player);

    if (slots.isEmpty()) {
      return;
    }
    MutableComponent slotsTooltip =
        Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD);

    for (int j = 0; j < slots.size(); j++) {
      String id = slots.get(j);
      String key = "curios.identifier." + id;
      MutableComponent type =
          Component.translatableWithFallback(
              key, Character.toUpperCase(id.charAt(0)) + id.substring(1).toLowerCase());

      if (j < slots.size() - 1) {
        type = type.append(", ");
      }
      type = type.withStyle(ChatFormatting.YELLOW);
      slotsTooltip.append(type);
    }
    Item.TooltipContext context = evt.getContext();
    List<Component> toAdd = List.of(slotsTooltip);
    evt.getToolTip()
        .addAll(
            1,
            CuriosApi.getCurio(stack)
                .map(curio -> curio.getSlotsTooltip(toAdd, context))
                .orElse(toAdd));
  }

  private static List<String> getItemStackSlots(ItemStack stack, Player player) {
    Set<String> slots =
        Set.copyOf(
            (player != null
                    ? CuriosApi.getItemStackSlots(stack, player)
                    : CuriosApi.getItemStackSlots(stack, FMLLoader.getDist() == Dist.CLIENT))
                .keySet());

    if (slots.contains("curio")) {
      slots = Set.of("curio");
    }
    return new ArrayList<>(slots);
  }
}
