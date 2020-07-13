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

package top.theillusivec4.curios.mixin;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.component.ICurio;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

  private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID
      .fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
  private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID
      .fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

  @Shadow
  public abstract Item getItem();

  @Shadow
  public abstract CompoundTag getTag();

  @Inject(at = @At("RETURN"), method = "getTooltip", cancellable = true)
  public void getTooltip(PlayerEntity playerEntity, TooltipContext tooltipContext,
      CallbackInfoReturnable<List<Text>> cb) {
    List<Text> tooltip = cb.getReturnValue();
    @SuppressWarnings("ConstantConditions") ItemStack stack = ((ItemStack) (Object) this);
    CompoundTag tag = this.getTag();
    int i = 0;

    if (tag != null && tag.contains("HideFlags", 99)) {
      i = tag.getInt("HideFlags");
    }

    Set<String> curioTags = CuriosApi.getCuriosHelper().getCurioTags(this.getItem());
    List<String> slots = new ArrayList<>(curioTags);

    if (!slots.isEmpty()) {
      List<Text> tagsTooltip = new ArrayList<>();
      MutableText slotsTooltip = new TranslatableText("curios.slot").append(": ")
          .formatted(Formatting.GOLD);

      for (int j = 0; j < slots.size(); j++) {
        String key = "curios.identifier." + slots.get(j);
        MutableText type = new TranslatableText(key);

        if (j < slots.size() - 1) {
          type = type.append(", ");
        }
        type = type.formatted(Formatting.YELLOW);
        slotsTooltip.append(type);
      }
      tagsTooltip.add(slotsTooltip);
      Optional<ICurio> optionalCurio = CuriosApi.getCuriosHelper().getCurio(stack);
      optionalCurio.ifPresent(curio -> {
        List<Text> curioTagsTooltip = curio.getTagsTooltip(tagsTooltip);

        if (!curioTagsTooltip.isEmpty()) {
          tooltip.addAll(1, curio.getTagsTooltip(tagsTooltip));
        }
      });

      if (!optionalCurio.isPresent()) {
        tooltip.addAll(1, tagsTooltip);
      }

      for (String identifier : slots) {
        Multimap<EntityAttribute, EntityAttributeModifier> multimap = CuriosApi.getCuriosHelper()
            .getAttributeModifiers(identifier, stack);

        if (!multimap.isEmpty() && (i & 2) == 0) {
          tooltip.add(LiteralText.EMPTY);
          tooltip.add(
              new TranslatableText("curios.modifiers." + identifier).formatted(Formatting.GOLD));

          for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
            EntityAttributeModifier attributeModifier = entry.getValue();
            double amount = attributeModifier.getValue();
            boolean flag = false;

            if (playerEntity != null) {

              if (attributeModifier.getId() == ATTACK_DAMAGE_MODIFIER_ID) {
                amount += playerEntity
                    .getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                amount += EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
                flag = true;
              } else if (attributeModifier.getId() == ATTACK_SPEED_MODIFIER_ID) {
                amount += playerEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
                flag = true;
              }

              double g;

              if (attributeModifier.getOperation()
                  != EntityAttributeModifier.Operation.MULTIPLY_BASE
                  && attributeModifier.getOperation()
                  != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {

                if ((entry.getKey()).equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                  g = amount * 10.0D;
                } else {
                  g = amount;
                }
              } else {
                g = amount * 100.0D;
              }

              if (flag) {
                tooltip.add((new LiteralText(" ")).append(new TranslatableText(
                    "attribute.modifier.equals." + attributeModifier.getOperation().getId(),
                    ItemStack.MODIFIER_FORMAT.format(g),
                    new TranslatableText((entry.getKey()).getTranslationKey())))
                    .formatted(Formatting.DARK_GREEN));
              } else if (amount > 0.0D) {
                tooltip.add((new TranslatableText(
                    "attribute.modifier.plus." + attributeModifier.getOperation().getId(),
                    ItemStack.MODIFIER_FORMAT.format(g),
                    new TranslatableText((entry.getKey()).getTranslationKey())))
                    .formatted(Formatting.BLUE));
              } else if (amount < 0.0D) {
                g *= -1.0D;
                tooltip.add((new TranslatableText(
                    "attribute.modifier.take." + attributeModifier.getOperation().getId(),
                    ItemStack.MODIFIER_FORMAT.format(g),
                    new TranslatableText((entry.getKey()).getTranslationKey())))
                    .formatted(Formatting.RED));
              }
            }
          }
        }
      }
    }

  }
}
