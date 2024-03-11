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

package top.theillusivec4.curiostest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curiostest.client.CuriosLayerDefinitions;
import top.theillusivec4.curiostest.client.model.AmuletModel;
import top.theillusivec4.curiostest.client.model.CrownModel;
import top.theillusivec4.curiostest.client.model.KnucklesModel;
import top.theillusivec4.curiostest.client.renderer.CrownRenderer;
import top.theillusivec4.curiostest.client.renderer.KnucklesRenderer;
import top.theillusivec4.curiostest.common.CuriosTestRegistry;
import top.theillusivec4.curiostest.common.item.AmuletItem;
import top.theillusivec4.curiostest.data.CuriosGenerator;
import top.theillusivec4.curiostest.data.CuriosTestProvider;

@Mod(CuriosTest.MODID)
public class CuriosTest {

  public static final String MODID = "curiostest";
  public static final Logger LOGGER = LogManager.getLogger();

  public CuriosTest(IEventBus eventBus) {
    CuriosTestRegistry.init();
    eventBus.addListener(this::enqueue);
    eventBus.addListener(this::clientSetup);
    eventBus.addListener(this::registerLayers);
    eventBus.addListener(this::creativeTab);
    eventBus.addListener(this::registerCaps);
    eventBus.addListener(this::gatherData);
    NeoForge.EVENT_BUS.addListener(this::attributeModifier);
  }

  private void gatherData(final GatherDataEvent evt) {
    DataGenerator generator = evt.getGenerator();

    generator.addProvider(evt.includeServer(),
        new AdvancementProvider(generator.getPackOutput(), evt.getLookupProvider(),
            evt.getExistingFileHelper(), List.of(new CuriosGenerator())));

    generator.addProvider(evt.includeServer(),
        new CuriosTestProvider("curiostest", generator.getPackOutput(), evt.getExistingFileHelper(),
            evt.getLookupProvider()));
  }

  private void registerCaps(final RegisterCapabilitiesEvent evt) {
    evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public void curioTick(SlotContext slotContext) {
        LivingEntity livingEntity = slotContext.entity();

        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 20 == 0) {
          livingEntity
              .addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, -1, true, true));
          stack.hurtAndBreak(1, livingEntity,
              damager -> CuriosApi.broadcastCurioBreakEvent(slotContext));
        }
      }
    }, CuriosTestRegistry.CROWN.get());

    evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {

      @Override
      public void curioTick(SlotContext slotContext) {
        LivingEntity livingEntity = slotContext.entity();

        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 19 == 0) {
          livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 0, true, true));
        }
      }

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                          UUID uuid) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, CuriosTest.MODID + ":speed_bonus", 0.1,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        atts.put(Attributes.ARMOR,
            new AttributeModifier(uuid, CuriosTest.MODID + ":armor_bonus", 2,
                AttributeModifier.Operation.ADDITION));
        atts.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, CuriosTest.MODID + ":knockback_resist", 0.2,
                AttributeModifier.Operation.ADDITION));
        CuriosApi.addSlotModifier(atts, "ring", uuid, 1, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(atts, "curio", uuid, -1, AttributeModifier.Operation.ADDITION);
        return atts;
      }

      @Nonnull
      @Override
      public DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel,
                                  boolean recentlyHit) {
        return DropRule.ALWAYS_KEEP;
      }

      @Nonnull
      @Override
      public SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
      }

      @Override
      public boolean canEquipFromUse(SlotContext slot) {
        return true;
      }

      @Override
      public boolean makesPiglinsNeutral(SlotContext slotContext) {
        return true;
      }

      @Override
      public boolean isEnderMask(SlotContext slotContext, EnderMan enderMan) {
        return true;
      }

      @Override
      public int getFortuneLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
        return 3;
      }
    }, CuriosTestRegistry.RING.get());

    evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                          UUID uuid) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, CuriosTest.MODID + ":attack_damage_bonus", 4,
                AttributeModifier.Operation.ADDITION));
        CuriosApi.addSlotModifier(atts, "necklace", uuid, 2, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(atts, "ring", uuid, -1, AttributeModifier.Operation.ADDITION);
        return atts;
      }
    }, CuriosTestRegistry.KNUCKLES.get());
  }

  private void attributeModifier(final CurioAttributeModifierEvent evt) {

    if (evt.getSlotContext().identifier().equals("curio")) {
      evt.clearModifiers();
      evt.addModifier(Attributes.MAX_HEALTH, new AttributeModifier(evt.getUuid(), "test", 10.0d,
          AttributeModifier.Operation.ADDITION));
      evt.addModifier(SlotAttribute.getOrCreate("ring"),
          new AttributeModifier(evt.getUuid(), "test", 1.0d, AttributeModifier.Operation.ADDITION));
    }
  }

  private void creativeTab(final BuildCreativeModeTabContentsEvent evt) {

    if (evt.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
      Collection<ItemLike> items =
          List.of(CuriosTestRegistry.AMULET.get(), CuriosTestRegistry.CROWN.get(),
              CuriosTestRegistry.KNUCKLES.get(), CuriosTestRegistry.RING.get());

      for (ItemLike item : items) {
        evt.accept(item);
      }
    }
  }

  private void enqueue(final InterModEnqueueEvent evt) {
    InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
        () -> new SlotTypeMessage.Builder("legacy").build());
  }

  private void clientSetup(final FMLClientSetupEvent evt) {
    CuriosRendererRegistry.register(CuriosTestRegistry.AMULET.get(),
        () -> (AmuletItem) CuriosTestRegistry.AMULET.get());
    CuriosRendererRegistry.register(CuriosTestRegistry.CROWN.get(), CrownRenderer::new);
    CuriosRendererRegistry.register(CuriosTestRegistry.KNUCKLES.get(), KnucklesRenderer::new);
  }

  private void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions evt) {
    evt.registerLayerDefinition(CuriosLayerDefinitions.CROWN, CrownModel::createLayer);
    evt.registerLayerDefinition(CuriosLayerDefinitions.AMULET, AmuletModel::createLayer);
    evt.registerLayerDefinition(CuriosLayerDefinitions.KNUCKLES, KnucklesModel::createLayer);
  }
}
