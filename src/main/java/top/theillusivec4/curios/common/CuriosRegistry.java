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

package top.theillusivec4.curios.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.common.item.AmuletItem;
import top.theillusivec4.curios.common.item.CrownItem;
import top.theillusivec4.curios.common.item.KnucklesItem;
import top.theillusivec4.curios.common.item.RingItem;

public class CuriosRegistry {

  public static final Item AMULET = new AmuletItem();
  public static final Item CROWN = new CrownItem();
  public static final Item RING = new RingItem();
  public static final Item KNUCKLES = new KnucklesItem();

  public static final ScreenHandlerType<CuriosScreenHandler> CURIOS_SCREENHANDLER = ScreenHandlerRegistry
      .registerSimple(new Identifier(CuriosApi.MODID, "curios_screen"), CuriosScreenHandler::new);

  public static void registerItems() {
    Registry.register(Registry.ITEM, new Identifier(CuriosApi.MODID, "amulet"), AMULET);
    Registry.register(Registry.ITEM, new Identifier(CuriosApi.MODID, "crown"), CROWN);
    Registry.register(Registry.ITEM, new Identifier(CuriosApi.MODID, "ring"), RING);
    Registry.register(Registry.ITEM, new Identifier(CuriosApi.MODID, "knuckles"), KNUCKLES);
  }

  public static void registerComponents() {
    ItemComponentCallbackV2.event(AMULET).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {

              @Override
              public void curioTick(String identifier, int index, LivingEntity livingEntity) {

                if (!livingEntity.getEntityWorld().isClient() && livingEntity.age % 40 == 0) {
                  livingEntity.addStatusEffect(
                      new StatusEffectInstance(StatusEffects.REGENERATION, 80, 0, true, true));
                }
              }
            })));

    ItemComponentCallbackV2.event(CROWN).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {

              @Override
              public void curioTick(String identifier, int index, LivingEntity livingEntity) {

                if (!livingEntity.getEntityWorld().isClient() && livingEntity.age % 20 == 0) {
                  livingEntity.addStatusEffect(
                      new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, -1, true, true));
                  itemStack.damage(1, livingEntity, damager -> CuriosApi.getCuriosHelper()
                      .onBrokenCurio(identifier, index, damager));
                }
              }
            })));

    ItemComponentCallbackV2.event(RING).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {
              @Override
              public void curioTick(String identifier, int index, LivingEntity livingEntity) {

                if (!livingEntity.getEntityWorld().isClient() && livingEntity.age % 19 == 0) {
                  livingEntity.addStatusEffect(
                      new StatusEffectInstance(StatusEffects.HASTE, 20, 0, true, true));
                }
              }

              @Override
              public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(
                  String identifier) {
                Multimap<EntityAttribute, EntityAttributeModifier> attributes = HashMultimap
                    .create();

                if (CuriosApi.getCuriosHelper().getCurioTags(itemStack.getItem())
                    .contains(identifier)) {
                  attributes.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                      new EntityAttributeModifier(RingItem.SPEED_UUID, "Speed bonus", 0.1,
                          Operation.MULTIPLY_TOTAL));
                  attributes.put(EntityAttributes.GENERIC_ARMOR,
                      new EntityAttributeModifier(RingItem.ARMOR_UUID, "Armor bonus", 2,
                          Operation.ADDITION));
                }
                return attributes;
              }

              @Override
              public void playRightClickEquipSound(LivingEntity livingEntity) {
                livingEntity.world
                    .playSound(null, livingEntity.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
                        SoundCategory.NEUTRAL, 1.0F, 1.0F);
              }

              @Override
              public boolean canRightClickEquip() {
                return true;
              }

              @Override
              public DropRule getDropRule(LivingEntity livingEntity) {
                return DropRule.ALWAYS_KEEP;
              }
            })));

    ItemComponentCallbackV2.event(KNUCKLES).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {

              @Override
              public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(
                  String identifier) {
                Multimap<EntityAttribute, EntityAttributeModifier> atts = HashMultimap.create();

                if (CuriosApi.getCuriosHelper().getCurioTags(itemStack.getItem())
                    .contains(identifier)) {
                  atts.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                      new EntityAttributeModifier(KnucklesItem.ATTACK_DAMAGE_UUID,
                          "Attack damage bonus", 4, Operation.ADDITION));
                }
                return atts;
              }
            })));
  }
}
