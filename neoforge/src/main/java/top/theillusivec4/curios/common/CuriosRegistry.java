/*
 * Copyright (c) 2018-2023 C4
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

import java.util.function.Supplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.capability.CurioInventory;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;
import top.theillusivec4.curios.common.util.SetCurioAttributesFunction;
import top.theillusivec4.curios.server.command.CurioArgumentType;

public class CuriosRegistry {

  private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(
          NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CuriosApi.MODID);
  private static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS =
      DeferredRegister.create(Registries.TRIGGER_TYPE, CuriosApi.MODID);
  private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
      DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, CuriosApi.MODID);
  private static final DeferredRegister<MenuType<?>> MENU_TYPES =
      DeferredRegister.create(Registries.MENU, CuriosApi.MODID);
  private static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS =
      DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CuriosApi.MODID);

  public static final Supplier<ArgumentTypeInfo<?, ?>>
      CURIO_SLOT_ARGUMENT = ARGUMENT_TYPES.register("slot_type",
      () -> ArgumentTypeInfos.registerByClass(CurioArgumentType.class,
          SingletonArgumentInfo.contextFree(CurioArgumentType::slot)));
  public static final Supplier<MenuType<CuriosContainer>> CURIO_MENU =
      MENU_TYPES.register("curios_container",
          () -> IMenuTypeExtension.create(CuriosContainer::new));
  public static final Supplier<LootItemFunctionType> CURIO_ATTRIBUTES =
      LOOT_FUNCTIONS.register("set_curio_attributes",
          () -> new LootItemFunctionType(SetCurioAttributesFunction.CODEC));
  public static final Supplier<EquipCurioTrigger>
      EQUIP_TRIGGER = CRITERION_TRIGGERS.register("equip_curio", () -> EquipCurioTrigger.INSTANCE);

  public static final Supplier<AttachmentType<CurioInventory>> INVENTORY =
      ATTACHMENT_TYPES.register("inventory",
          () -> AttachmentType.serializable(CurioInventory::new).copyOnDeath().build());

  public static void init() {
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    ARGUMENT_TYPES.register(eventBus);
    MENU_TYPES.register(eventBus);
    LOOT_FUNCTIONS.register(eventBus);
    ATTACHMENT_TYPES.register(eventBus);
    CRITERION_TRIGGERS.register(eventBus);
  }
}
