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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.extensions.ICurioSlotExtension;
import top.theillusivec4.curios.api.extensions.RegisterCuriosExtensionsEvent;
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
    CuriosTestRegistry.init(eventBus);
    eventBus.addListener(this::clientSetup);
    eventBus.addListener(this::registerLayers);
    eventBus.addListener(this::creativeTab);
    eventBus.addListener(this::registerCaps);
    eventBus.addListener(this::gatherData);
    eventBus.addListener(this::registerSlotExtensions);
    NeoForge.EVENT_BUS.addListener(this::attributeModifier);
    CuriosSlotTypes.registerPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "test"),
                                     (ctx, stack) -> stack.getItem() == Items.OAK_BOAT);
  }

  private void registerSlotExtensions(final RegisterCuriosExtensionsEvent evt) {
    evt.registerSlotExtension(new ICurioSlotExtension() {
      @Override
      public ItemStack getDisplayStack(SlotContext slotContext, ItemStack defaultStack) {
        return Items.DIAMOND_AXE.getDefaultInstance();
      }
    }, "test");
  }

  private void gatherData(final GatherDataEvent.Client evt) {
    DataGenerator generator = evt.getGenerator();
    generator.addProvider(true, new AdvancementProvider(generator.getPackOutput(),
                                                        evt.getLookupProvider(),
                                                        List.of(new CuriosGenerator())));
    generator.addProvider(true, new CuriosTestProvider("curiostest", generator.getPackOutput(),
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

        if (livingEntity.level() instanceof ServerLevel serverLevel &&
            livingEntity.tickCount % 20 == 0) {
          livingEntity.addEffect(
              new MobEffectInstance(MobEffects.NIGHT_VISION, 300, -1, true, true));
          stack.hurtAndBreak(1, serverLevel, livingEntity,
                             item -> CuriosApi.broadcastCurioBreakEvent(slotContext));
        }
      }
    }, CuriosTestRegistry.CROWN.get());

    evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {

      @Override
      public void curioTick(SlotContext slotContext) {
        LivingEntity livingEntity = slotContext.entity();

        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 19 == 0) {
          livingEntity.addEffect(new MobEffectInstance(MobEffects.HASTE, 20, 0, true, true));
        }
      }

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
          SlotContext slotContext, ResourceLocation id) {
        Multimap<Holder<Attribute>, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.MOVEMENT_SPEED,
                 new AttributeModifier(
                     ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "speed_bonus"), 0.1,
                     AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        atts.put(Attributes.ARMOR,
                 new AttributeModifier(
                     ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "armor_bonus"), 2,
                     AttributeModifier.Operation.ADD_VALUE));
        atts.put(Attributes.KNOCKBACK_RESISTANCE,
                 new AttributeModifier(
                     ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "knockback_resist"),
                     0.2,
                     AttributeModifier.Operation.ADD_VALUE));
        CuriosApi.addSlotModifier(atts, "ring", id, 1, AttributeModifier.Operation.ADD_VALUE);
        CuriosApi.addSlotModifier(atts, "curio", id, -1, AttributeModifier.Operation.ADD_VALUE);
        return atts;
      }

      @Nonnull
      @Override
      public DropRule getDropRule(SlotContext slotContext, DamageSource source,
                                  boolean recentlyHit) {
        return DropRule.ALWAYS_KEEP;
      }

      @Nonnull
      @Override
      public SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD.value(), 1.0f, 1.0f);
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

      @Override
      public int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
        return 3;
      }
    }, CuriosTestRegistry.RING.get());

    evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public CurioAttributeModifiers getDefaultCurioAttributeModifiers() {
        return CurioAttributeModifiers.builder()
            .addModifier(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "knuckles"),
                    4,
                    AttributeModifier.Operation.ADD_VALUE)
            )
            .addSlotModifier(
                CuriosSlotTypes.Preset.RING.id(),
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "knuckles"),
                    2,
                    AttributeModifier.Operation.ADD_VALUE))
            .build();
      }

      @Override
      public int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
        return 10;
      }
    }, CuriosTestRegistry.KNUCKLES.get());
  }

  private void attributeModifier(final CurioAttributeModifierEvent evt) {

//    if (evt.getSlotContext().identifier().equals("curio")) {
//      evt.clearModifiers();
//      evt.addModifier(Attributes.MAX_HEALTH,
//          new AttributeModifier(ResourceLocation.withDefaultNamespace("test"), 10.0d,
//              AttributeModifier.Operation.ADD_VALUE));
//      evt.addModifier(SlotAttribute.getOrCreate("ring"),
//          new AttributeModifier(ResourceLocation.withDefaultNamespace("test"), 1.0d,
//              AttributeModifier.Operation.ADD_VALUE));
//    }
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

  private void clientSetup(final FMLClientSetupEvent evt) {
    ICurioRenderer.register(CuriosTestRegistry.AMULET.get(),
                            () -> (AmuletItem) CuriosTestRegistry.AMULET.get());
    ICurioRenderer.register(CuriosTestRegistry.CROWN.get(), CrownRenderer::new);
    ICurioRenderer.register(CuriosTestRegistry.KNUCKLES.get(), KnucklesRenderer::new);
  }

  private void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions evt) {
    evt.registerLayerDefinition(CuriosLayerDefinitions.CROWN, CrownModel::createLayer);
    evt.registerLayerDefinition(CuriosLayerDefinitions.AMULET, AmuletModel::createLayer);
    evt.registerLayerDefinition(CuriosLayerDefinitions.KNUCKLES, KnucklesModel::createLayer);
  }
}
