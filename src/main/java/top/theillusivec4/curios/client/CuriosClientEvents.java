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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

public class CuriosClientEvents {

  @SubscribeEvent
  public void renderHand(final RenderArmEvent evt) {
    Minecraft mc = Minecraft.getInstance();

    if (mc.player != null) {
      PoseStack poseStack = evt.getPoseStack();
      poseStack.pushPose();
      AbstractClientPlayer clientPlayer = mc.player;
      EntityRenderer<? super AbstractClientPlayer, ?>
          entityRenderer = mc.getEntityRenderDispatcher().getRenderer(clientPlayer);
      EntityRenderState renderState = entityRenderer.createRenderState();

      if (renderState instanceof PlayerRenderState playerRenderState) {
        CuriosApi.getCuriosInventory(clientPlayer)
            .ifPresent(handler -> handler.getCurios().forEach((id, stacksHandler) -> {
              IDynamicStackHandler stackHandler = stacksHandler.getStacks();
              IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

              for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
                boolean cosmetic = true;
                NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                boolean renderable = renderStates.size() > i && renderStates.get(i);

                if (stack.isEmpty() && renderable) {
                  stack = stackHandler.getStackInSlot(i);
                  cosmetic = false;
                }

                if (!stack.isEmpty()) {
                  SlotContext
                      slotContext = new SlotContext(id, clientPlayer, i, cosmetic, renderable);
                  ICurioRenderer.get(stack).renderFirstPersonHand(
                      stack,
                      slotContext,
                      evt.getArm(),
                      poseStack,
                      evt.getMultiBufferSource(),
                      playerRenderState,
                      evt.getPlayer(),
                      evt.getPackedLight()
                  );
                }
              }
            }));
      }
      poseStack.popPose();
    }
  }

  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Post evt) {

    if (CuriosKeyMappings.OPEN_CURIOS_INVENTORY.consumeClick() && Minecraft.getInstance()
        .isWindowActive()) {
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
    Set<String> slots = getItemStackSlots(stack, player).keySet();

    for (String identifier : slots) {
      SlotContext slotContext = new SlotContext(identifier, player, 0, false, true);
      Multimap<Holder<Attribute>, AttributeModifier> attributes = LinkedHashMultimap.create();
      ICurioItem.forEachModifier(stack, slotContext, attributes::put);
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
            AttributeTooltipContext.of(player, context, context.tooltipDisplay(), context.flag()));
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
    Map<String, ISlotType> slots = getItemStackSlots(stack, player);

    if (slots.isEmpty()) {
      return;
    }
    List<String> slotIds = slots.keySet().stream().toList();
    MutableComponent slotsTooltip =
        Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD);

    for (int j = 0; j < slotIds.size(); j++) {
      String id = slotIds.get(j);
      String key = "curios.identifier." + id;
      MutableComponent type =
          Component.translatableWithFallback(
              key, Character.toUpperCase(id.charAt(0)) + id.substring(1).toLowerCase());

      if (j < slotIds.size() - 1) {
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

  private static Map<String, ISlotType> getItemStackSlots(ItemStack stack, Player player) {
    Map<String, ISlotType> result = new LinkedHashMap<>();
    Map<String, ISlotType> map =
        player != null
        ? CuriosSlotTypes.getItemSlotTypes(stack, player)
        : CuriosSlotTypes.getItemSlotTypes(stack, FMLLoader.getDist().isClient());

    for (Map.Entry<String, ISlotType> entry : map.entrySet()) {
      ISlotType slotType = entry.getValue();

      // Avoid getting slots with "all" validators to solve tooltip bloat
      if (!slotType.getValidators().contains(CuriosResources.resource("all"))) {
        result.put(entry.getKey(), slotType);
      }
    }
    String curio = CuriosSlotTypes.Preset.CURIO.id();

    if (result.containsKey(curio)) {

      if (stack.is(CuriosTags.CURIO)) {
        return Map.of(curio, result.get(curio));
      } else {
        result.remove(curio);
      }
    }
    return result;
  }
}
