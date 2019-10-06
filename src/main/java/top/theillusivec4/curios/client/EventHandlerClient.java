/*
 * Copyright (C) 2018-2019  C4
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

package top.theillusivec4.curios.client;

import static net.minecraft.item.ItemStack.DECIMALFORMAT;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

public class EventHandlerClient {

  private static final UUID ATTACK_DAMAGE_MODIFIER = UUID
      .fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
  private static final UUID ATTACK_SPEED_MODIFIER = UUID
      .fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

  @SubscribeEvent
  public void onKeyInput(TickEvent.ClientTickEvent evt) {

    if (evt.phase != TickEvent.Phase.END) {
      return;
    }

    Minecraft mc = Minecraft.getInstance();

    if (KeyRegistry.openCurios.isPressed() && mc.isGameFocused()) {
      NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
          new CPacketOpenCurios((float) mc.mouseHelper.getMouseX(),
              (float) mc.mouseHelper.getMouseY()));
    }
  }

  @SubscribeEvent
  public void onTooltip(ItemTooltipEvent evt) {

    ItemStack stack = evt.getItemStack();

    if (!stack.isEmpty()) {
      List<ITextComponent> tooltip = evt.getToolTip();
      CompoundNBT tag = stack.getTag();
      int i = 0;

      if (tag != null && tag.contains("HideFlags", 99)) {
        i = tag.getInt("HideFlags");
      }

      Set<String> slots = CuriosAPI.getCurioTags(stack.getItem());

      if (!slots.isEmpty()) {
        List<ITextComponent> tagTooltips = new ArrayList<>();

        for (String s : slots) {
          String key = "curios.identifier." + s;
          tagTooltips.add(new TranslationTextComponent(key).applyTextStyle(TextFormatting.GOLD));
        }

        CuriosAPI.getCurio(stack)
            .ifPresent(curio -> tooltip.addAll(1, curio.getTagsTooltip(tagTooltips)));

        if (!CuriosAPI.getCurio(stack).isPresent()) {
          tooltip.addAll(1, tagTooltips);
        }

        final int hideFlags = i;
        CuriosAPI.getCurio(stack).ifPresent(curio -> {

          for (String identifier : slots) {
            Multimap<String, AttributeModifier> multimap = curio.getAttributeModifiers(identifier);

            if (!multimap.isEmpty() && (hideFlags & 2) == 0) {
              PlayerEntity player = evt.getEntityPlayer();
              tooltip.add(new StringTextComponent(""));
              tooltip.add(new TranslationTextComponent("curios.modifiers." + identifier)
                  .applyTextStyle(TextFormatting.GOLD));

              for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                AttributeModifier attributemodifier = entry.getValue();
                double amount = attributemodifier.getAmount();
                boolean flag = false;

                if (player != null) {

                  if (attributemodifier.getID() == ATTACK_DAMAGE_MODIFIER) {
                    amount = amount + player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                        .getBaseValue();
                    amount = amount + (double) EnchantmentHelper
                        .getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
                    flag = true;
                  } else if (attributemodifier.getID() == ATTACK_SPEED_MODIFIER) {
                    amount += player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED)
                        .getBaseValue();
                    flag = true;
                  }

                  double d1;

                  if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE
                      && attributemodifier.getOperation()
                      != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = amount;
                  } else {
                    d1 = amount * 100.0D;
                  }

                  if (flag) {
                    tooltip.add((new StringTextComponent(" ")).appendSibling(
                        new TranslationTextComponent(
                            "attribute.modifier.equals." + attributemodifier.getOperation().getId(),
                            DECIMALFORMAT.format(d1),
                            new TranslationTextComponent("attribute.name." + entry.getKey())))
                        .applyTextStyle(TextFormatting.DARK_GREEN));
                  } else if (amount > 0.0D) {
                    tooltip.add((new TranslationTextComponent(
                        "attribute.modifier.plus." + attributemodifier.getOperation().getId(),
                        DECIMALFORMAT.format(d1),
                        new TranslationTextComponent("attribute.name." + entry.getKey())))
                        .applyTextStyle(TextFormatting.BLUE));
                  } else if (amount < 0.0D) {
                    d1 = d1 * -1.0D;
                    tooltip.add((new TranslationTextComponent(
                        "attribute.modifier.take." + attributemodifier.getOperation().getId(),
                        DECIMALFORMAT.format(d1),
                        new TranslationTextComponent("attribute.name." + entry.getKey())))
                        .applyTextStyle(TextFormatting.RED));
                  }
                }
              }
            }
          }
        });
      }
    }
  }
}
