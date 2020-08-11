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

package top.theillusivec4.curios.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Map;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosCommand {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

    LiteralArgumentBuilder<ServerCommandSource> curiosCommand = CommandManager.literal("curios")
        .requires(player -> player.hasPermissionLevel(2));

    curiosCommand.then(CommandManager.literal("set").then(
        CommandManager.argument("slot", CurioArgumentType.slot()).then(
            CommandManager.argument("player", EntityArgumentType.player()).executes(
                context -> setSlotsForPlayer(context.getSource(),
                    EntityArgumentType.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"), 1)).then(
                CommandManager.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> setSlotsForPlayer(context.getSource(),
                        EntityArgumentType.getPlayer(context, "player"),
                        CurioArgumentType.getSlot(context, "slot"),
                        IntegerArgumentType.getInteger(context, "amount")))))));

    curiosCommand.then(CommandManager.literal("add").then(
        CommandManager.argument("slot", CurioArgumentType.slot()).then(
            CommandManager.argument("player", EntityArgumentType.player()).executes(
                context -> growSlotForPlayer(context.getSource(),
                    EntityArgumentType.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"), 1)).then(
                CommandManager.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> growSlotForPlayer(context.getSource(),
                        EntityArgumentType.getPlayer(context, "player"),
                        CurioArgumentType.getSlot(context, "slot"),
                        IntegerArgumentType.getInteger(context, "amount")))))));

    curiosCommand.then(CommandManager.literal("remove").then(
        CommandManager.argument("slot", CurioArgumentType.slot()).then(
            CommandManager.argument("player", EntityArgumentType.player()).executes(
                context -> shrinkSlotForPlayer(context.getSource(),
                    EntityArgumentType.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"), 1)).then(
                CommandManager.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> shrinkSlotForPlayer(context.getSource(),
                        EntityArgumentType.getPlayer(context, "player"),
                        CurioArgumentType.getSlot(context, "slot"),
                        IntegerArgumentType.getInteger(context, "amount")))))));

    curiosCommand.then(CommandManager.literal("unlock").then(
        CommandManager.argument("slot", CurioArgumentType.slot()).then(
            CommandManager.argument("player", EntityArgumentType.player()).executes(
                context -> unlockSlotForPlayer(context.getSource(),
                    EntityArgumentType.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(CommandManager.literal("lock").then(
        CommandManager.argument("slot", CurioArgumentType.slot()).then(
            CommandManager.argument("player", EntityArgumentType.player()).executes(
                context -> lockSlotForPlayer(context.getSource(),
                    EntityArgumentType.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(CommandManager.literal("clear").then(
        CommandManager.argument("player", EntityArgumentType.player()).executes(
            context -> clearSlotsForPlayer(context.getSource(),
                EntityArgumentType.getPlayer(context, "player"), "")).then(
            CommandManager.argument("slot", CurioArgumentType.slot()).executes(
                context -> clearSlotsForPlayer(context.getSource(),
                    EntityArgumentType.getPlayer(context, "player"),
                    CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(CommandManager.literal("reset").then(
        CommandManager.argument("player", EntityArgumentType.player()).executes(
            context -> resetSlotsForPlayer(context.getSource(),
                EntityArgumentType.getPlayer(context, "player")))));

    dispatcher.register(curiosCommand);
  }

  private static int setSlotsForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP,
      String slot, int amount) {
    CuriosApi.getSlotHelper().setSlotsForType(slot, playerMP, amount);
    source.sendFeedback(new TranslatableText("commands.curios.set.success", slot,
            CuriosApi.getSlotHelper().getSlotsForType(playerMP, slot), playerMP.getDisplayName()),
        true);
    return Command.SINGLE_SUCCESS;
  }

  private static int growSlotForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP,
      String slot, int amount) {
    CuriosApi.getSlotHelper().growSlotType(slot, amount, playerMP);
    source.sendFeedback(new TranslatableText("commands.curios.add.success", amount, slot,
        playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int shrinkSlotForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP,
      String slot, int amount) {
    CuriosApi.getSlotHelper().shrinkSlotType(slot, amount, playerMP);
    source.sendFeedback(new TranslatableText("commands.curios.remove.success", amount, slot,
        playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int unlockSlotForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP,
      String slot) {
    CuriosApi.getSlotHelper().unlockSlotType(slot, playerMP);
    source.sendFeedback(
        new TranslatableText("commands.curios.unlock.success", slot, playerMP.getDisplayName()),
        true);
    return Command.SINGLE_SUCCESS;
  }

  private static int lockSlotForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP,
      String slot) {
    CuriosApi.getSlotHelper().lockSlotType(slot, playerMP);
    source.sendFeedback(
        new TranslatableText("commands.curios.lock.success", slot, playerMP.getDisplayName()),
        true);
    return Command.SINGLE_SUCCESS;
  }

  private static int clearSlotsForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP,
      String slot) {

    CuriosApi.getCuriosHelper().getCuriosHandler(playerMP).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      if (!slot.isEmpty() && curios.get(slot) != null) {
        clear(curios.get(slot));
      } else {

        for (String id : curios.keySet()) {
          clear(curios.get(id));
        }
      }
    });

    if (slot.isEmpty()) {
      source.sendFeedback(
          new TranslatableText("commands.curios.clearAll.success", playerMP.getDisplayName()),
          true);
    } else {
      source.sendFeedback(
          new TranslatableText("commands.curios.clear.success", slot, playerMP.getDisplayName()),
          true);
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int resetSlotsForPlayer(ServerCommandSource source, ServerPlayerEntity playerMP) {
    CuriosApi.getCuriosHelper().getCuriosHandler(playerMP).ifPresent(handler -> {
      handler.reset();
      handler.sync();
    });
    source.sendFeedback(
        new TranslatableText("commands.curios.reset.success", playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static void clear(ICurioStacksHandler stacksHandler) {

    for (int i = 0; i < stacksHandler.getSlots(); i++) {
      stacksHandler.getStacks().setStack(i, ItemStack.EMPTY);
      stacksHandler.getCosmeticStacks().setStack(i, ItemStack.EMPTY);
    }
  }
}
