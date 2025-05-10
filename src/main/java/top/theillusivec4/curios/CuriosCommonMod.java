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

package top.theillusivec4.curios;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.resource.VanillaServerListeners;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.extensions.RegisterCuriosExtensionsEvent;
import top.theillusivec4.curios.api.internal.CuriosServices;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.common.CuriosCommonEvents;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemHandler;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;
import top.theillusivec4.curios.common.data.CuriosSlotResources;
import top.theillusivec4.curios.common.integration.CuriosIntegrations;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.config.CuriosClientConfig;
import top.theillusivec4.curios.config.CuriosConfig;
import top.theillusivec4.curios.impl.CuriosRegistry;
import top.theillusivec4.curios.server.command.CurioArgumentType;
import top.theillusivec4.curios.server.command.CuriosCommand;
import top.theillusivec4.curios.server.command.CuriosSelectorOptions;

@Mod(CuriosConstants.MOD_ID)
public class CuriosCommonMod {

  public CuriosCommonMod(IEventBus eventBus, ModContainer modContainer) {
    CuriosRegistry.init(eventBus);
    CuriosIntegrations.setup(eventBus);
    eventBus.addListener(this::setup);
    eventBus.addListener(this::registerCaps);
    eventBus.addListener(this::registerPayloadHandler);
    NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
    NeoForge.EVENT_BUS.addListener(this::registerCommands);
    NeoForge.EVENT_BUS.addListener(this::reload);
    modContainer.registerConfig(ModConfig.Type.CLIENT, CuriosClientConfig.CLIENT_SPEC);
    modContainer.registerConfig(ModConfig.Type.COMMON, CuriosConfig.COMMON_SPEC);
    modContainer.registerConfig(ModConfig.Type.SERVER, CuriosConfig.SERVER_SPEC);
  }

  private void registerPayloadHandler(final RegisterPayloadHandlersEvent evt) {
    NetworkHandler.register(evt.registrar("1.0"));
  }

  private void setup(FMLCommonSetupEvent evt) {
    NeoForge.EVENT_BUS.register(new CuriosCommonEvents());
    ModLoader.postEventWrapContainerInModOrder(new RegisterCuriosExtensionsEvent());
    evt.enqueueWork(CuriosSelectorOptions::register);
  }

  private void registerCaps(RegisterCapabilitiesEvent evt) {

    for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {

      evt.registerEntity(CuriosCapability.ITEM_HANDLER, entityType,
                         (entity, ctx) -> {

                           if (entity instanceof LivingEntity livingEntity) {

                             if (!CuriosSlotTypes.getDefaultEntitySlotTypes(livingEntity)
                                 .isEmpty()) {
                               return new CurioItemHandler(livingEntity);
                             }
                           }
                           return null;
                         });

      evt.registerEntity(CuriosCapability.INVENTORY, entityType,
                         (entity, ctx) -> {

                           if (entity instanceof LivingEntity livingEntity) {

                             if (!CuriosSlotTypes.getDefaultEntitySlotTypes(livingEntity)
                                 .isEmpty()) {
                               return new CurioInventoryCapability(livingEntity);
                             }
                           }
                           return null;
                         });
    }

    for (Item item : BuiltInRegistries.ITEM) {
      evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> {
        Item it = stack.getItem();
        ICurioItem curioItem = CuriosServices.EXTENSIONS.getCurioItem(item);

        if (curioItem == null && it instanceof ICurioItem itemCurio) {
          curioItem = itemCurio;
        }

        if (curioItem != null && curioItem.hasCurioCapability(stack)) {
          return new ItemizedCurioCapability(curioItem, stack);
        }
        return null;
      }, item);
    }
  }

  private void serverAboutToStart(ServerAboutToStartEvent evt) {
    Set<String> slotIds = new HashSet<>();

    for (ISlotType value : CuriosSlotResources.SERVER.getSlots().values()) {
      slotIds.add(value.getId());
    }
    CurioArgumentType.slotIds = slotIds;
  }

  private void registerCommands(RegisterCommandsEvent evt) {
    CuriosCommand.register(evt.getDispatcher(), evt.getBuildContext());
  }

  private void reload(final AddServerReloadListenersEvent evt) {
    CuriosSlotResources.SERVER = new CuriosSlotResources(evt.getRegistryAccess());
    evt.addListener(CuriosSlotResources.ID, CuriosSlotResources.SERVER);
    evt.addDependency(VanillaServerListeners.LAST, CuriosSlotResources.ID);
  }

  public static String itemCacheKey(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString() +
        (!stack.getComponents().isEmpty() ?
         stack.getComponents().stream().map(TypedDataComponent::toString)
             .reduce((s, s2) -> s + s2) : "");
  }
}
