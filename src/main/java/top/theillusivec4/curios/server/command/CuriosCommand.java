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

package top.theillusivec4.curios.server.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.SortedMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncMap;

public class CuriosCommand {

  public static void register(CommandDispatcher<CommandSource> dispatcher) {

    final int opPermissionLevel = ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel();

    LiteralArgumentBuilder<CommandSource> curiosCommand = Commands.literal("curios")
        .requires(player -> player.hasPermissionLevel(opPermissionLevel));

    curiosCommand.then(Commands.literal("set").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> setSlotsOfPlayer(context.getSource(),
                    EntityArgument.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"), 1)).then(
                Commands.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> setSlotsOfPlayer(context.getSource(),
                        EntityArgument.getPlayer(context, "player"),
                        CurioArgumentType.getSlot(context, "slot"),
                        IntegerArgumentType.getInteger(context, "amount")))))));

    curiosCommand.then(Commands.literal("add").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> addSlotToPlayer(context.getSource(),
                    EntityArgument.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"), 1)).then(
                Commands.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> addSlotToPlayer(context.getSource(),
                        EntityArgument.getPlayer(context, "player"),
                        CurioArgumentType.getSlot(context, "slot"),
                        IntegerArgumentType.getInteger(context, "amount")))))));

    curiosCommand.then(Commands.literal("remove").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> removeSlotFromPlayer(context.getSource(),
                    EntityArgument.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"), 1)).then(
                Commands.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> removeSlotFromPlayer(context.getSource(),
                        EntityArgument.getPlayer(context, "player"),
                        CurioArgumentType.getSlot(context, "slot"),
                        IntegerArgumentType.getInteger(context, "amount")))))));

    curiosCommand.then(Commands.literal("enable").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> enableSlotForPlayer(context.getSource(),
                    EntityArgument.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(Commands.literal("disable").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> disableSlotForPlayer(context.getSource(),
                    EntityArgument.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(Commands.literal("clear").then(
        Commands.argument("player", EntityArgument.player()).executes(
            context -> clearSlotsForPlayer(context.getSource(),
                EntityArgument.getPlayer(context, "player"), "")).then(
            Commands.argument("slot", CurioArgumentType.slot()).executes(
                context -> clearSlotsForPlayer(context.getSource(),
                    EntityArgument.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(Commands.literal("reset").then(
        Commands.argument("player", EntityArgument.player()).executes(
            context -> resetSlotsForPlayer(context.getSource(),
                EntityArgument.getPlayer(context, "player")))));

    dispatcher.register(curiosCommand);
  }

  private static int setSlotsOfPlayer(CommandSource source, ServerPlayerEntity playerMP,
      String slot, int amount) {
    CuriosApi.setSlotsForType(slot, playerMP, amount);
    source.sendFeedback(new TranslationTextComponent("commands.curios.set.success", slot,
        CuriosApi.getSlotsForType(playerMP, slot), playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int addSlotToPlayer(CommandSource source, ServerPlayerEntity playerMP, String slot,
      int amount) {

    CuriosApi.growSlotType(slot, amount, playerMP);
    source.sendFeedback(new TranslationTextComponent("commands.curios.add.success", amount, slot,
        playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int removeSlotFromPlayer(CommandSource source, ServerPlayerEntity playerMP,
      String slot, int amount) {

    CuriosApi.shrinkSlotType(slot, amount, playerMP);
    source.sendFeedback(new TranslationTextComponent("commands.curios.remove.success", amount, slot,
        playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int enableSlotForPlayer(CommandSource source, ServerPlayerEntity playerMP,
      String slot) {

    CuriosApi.unlockSlotType(slot, playerMP);
    source.sendFeedback(new TranslationTextComponent("commands.curios.enable.success", slot,
        playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int disableSlotForPlayer(CommandSource source, ServerPlayerEntity playerMP,
      String slot) {

    CuriosApi.lockSlotType(slot, playerMP);
    source.sendFeedback(new TranslationTextComponent("commands.curios.disable.success", slot,
        playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int clearSlotsForPlayer(CommandSource source, ServerPlayerEntity playerMP,
      String slot) {

    CuriosApi.getCuriosHandler(playerMP).ifPresent(handler -> {
      SortedMap<String, CurioSlotStackHandler> map = handler.getCurios();

      if (!slot.isEmpty() && map.get(slot) != null) {
        clear(map.get(slot));
      } else {

        for (String id : map.keySet()) {
          clear(map.get(id));
        }
      }
    });

    if (slot.isEmpty()) {
      source.sendFeedback(new TranslationTextComponent("commands.curios.clearAll.success",
          playerMP.getDisplayName()), true);
    } else {
      source.sendFeedback(new TranslationTextComponent("commands.curios.clear.success", slot,
          playerMP.getDisplayName()), true);
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int resetSlotsForPlayer(CommandSource source, ServerPlayerEntity playerMP) {

    CuriosApi.getCuriosHandler(playerMP).ifPresent(handler -> {
      SortedMap<String, CurioSlotStackHandler> slots = Maps.newTreeMap();
      CuriosApi.getTypeIdentifiers().forEach(id -> CuriosApi.getType(id)
          .ifPresent(type -> slots.put(id, new CurioSlotStackHandler(type.getSize()))));
      handler.setCurios(slots);
      NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerMP),
          new SPacketSyncMap(playerMP.getEntityId(), handler.getCurios()));
    });
    source.sendFeedback(
        new TranslationTextComponent("commands.curios.reset.success", playerMP.getDisplayName()),
        true);
    return Command.SINGLE_SUCCESS;
  }

  private static void clear(CurioSlotStackHandler stacks) {

    for (int i = 0; i < stacks.getSlots(); i++) {
      stacks.setStackInSlot(i, ItemStack.EMPTY);
    }
  }
}
