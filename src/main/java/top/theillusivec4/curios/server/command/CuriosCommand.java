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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.slottype.LegacySlotManager;

public class CuriosCommand {

  private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER =
      new Dynamic3CommandExceptionType((p_180347_, p_180348_, p_180349_) -> Component.translatable(
          "commands.item.source.not_a_container", p_180347_, p_180348_,
          p_180349_));
  private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT =
      new DynamicCommandExceptionType(
          (p_180353_) -> Component.translatable("commands.item.source.no_such_slot", p_180353_));

  private static final SuggestionProvider<CommandSourceStack>
      SUGGEST_MODIFIER = (p_278910_, p_278911_) -> {
    LootDataManager lootdatamanager = p_278910_.getSource().getServer().getLootData();
    return SharedSuggestionProvider.suggestResource(lootdatamanager.getKeys(LootDataType.MODIFIER),
                                                    p_278911_);
  };

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              CommandBuildContext buildContext) {

    LiteralArgumentBuilder<CommandSourceStack> curiosCommand = Commands.literal("curios")
        .requires(player -> player.hasPermission(2));

    curiosCommand.then(Commands.literal("list").executes(context -> {
      Map<String, Set<String>> map = new HashMap<>(LegacySlotManager.getIdsToMods());

      for (Map.Entry<String, Set<String>> entry : CuriosSlotManager.SERVER.getModsFromSlots()
          .entrySet()) {
        map.computeIfAbsent(entry.getKey(), (k) -> new HashSet<>()).addAll(entry.getValue());
      }

      for (Map.Entry<String, Set<String>> entry : CuriosEntityManager.SERVER.getModsFromSlots()
          .entrySet()) {
        map.computeIfAbsent(entry.getKey(), (k) -> new HashSet<>()).addAll(entry.getValue());
      }

      for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
        context.getSource().sendSuccess(
            () -> Component.literal(entry.getKey() + " - " + String.join(", ", entry.getValue())),
            false);
      }
      return Command.SINGLE_SUCCESS;
    }));

    curiosCommand.then(Commands.literal("replace").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("index", IntegerArgumentType.integer()).then(
                Commands.argument("player", EntityArgument.player()).then(
                        Commands.literal("with").then(
                            Commands.argument("item", ItemArgument.item(buildContext)).executes(
                                    context -> replaceItemForPlayer(context.getSource(),
                                                                    EntityArgument.getPlayer(context,
                                                                                             "player"),
                                                                    CurioArgumentType.getSlot(context,
                                                                                              "slot"),
                                                                    IntegerArgumentType.getInteger(context,
                                                                                                   "index"),
                                                                    ItemArgument.getItem(context, "item")))
                                .then(
                                    Commands.argument("count", IntegerArgumentType.integer()).executes(
                                        context -> replaceItemForPlayer(context.getSource(),
                                                                        EntityArgument.getPlayer(
                                                                            context, "player"),
                                                                        CurioArgumentType.getSlot(
                                                                            context, "slot"),
                                                                        IntegerArgumentType.getInteger(
                                                                            context, "index"),
                                                                        ItemArgument.getItem(context,
                                                                                             "item"),
                                                                        IntegerArgumentType.getInteger(
                                                                            context, "count"))))))
                    .then(
                        Commands.literal("from").then(
                            Commands.literal("block").then(
                                Commands.argument("source", BlockPosArgument.blockPos()).then(
                                    Commands.argument("sourceSlot", SlotArgument.slot()).executes(
                                        context -> blockToSlot(
                                            context.getSource(),
                                            BlockPosArgument.getLoadedBlockPos(context, "source"),
                                            SlotArgument.getSlot(context, "sourceSlot"),
                                            EntityArgument.getPlayer(context, "player"),
                                            CurioArgumentType.getSlot(context, "slot"),
                                            IntegerArgumentType.getInteger(context, "index")
                                        )).then(
                                        Commands.argument("modifier", ResourceLocationArgument.id())
                                            .suggests(SUGGEST_MODIFIER)
                                            .executes(
                                                context -> blockToSlot(
                                                    context.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(context,
                                                                                       "source"),
                                                    SlotArgument.getSlot(context, "sourceSlot"),
                                                    ResourceLocationArgument.getItemModifier(
                                                        context, "modifier"),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    CurioArgumentType.getSlot(context, "slot"),
                                                    IntegerArgumentType.getInteger(context, "index")
                                                )
                                            )
                                    )
                                )
                            )
                        ).then(
                            Commands.literal("entity-vanilla").then(
                                Commands.argument("source", EntityArgument.entity()).then(
                                    Commands.argument("sourceSlot", SlotArgument.slot()).executes(
                                        context -> entityToSlot(
                                            context.getSource(),
                                            EntityArgument.getEntity(context,
                                                                     "source"),
                                            SlotArgument.getSlot(context,
                                                                 "sourceSlot"),
                                            EntityArgument.getPlayer(context, "player"),
                                            CurioArgumentType.getSlot(context, "slot"),
                                            IntegerArgumentType.getInteger(context,
                                                                           "index")
                                        )
                                    ).then(
                                        Commands.argument("modifier", ResourceLocationArgument.id())
                                            .suggests(SUGGEST_MODIFIER)
                                            .executes(
                                                context -> entityToSlot(
                                                    context.getSource(),
                                                    EntityArgument.getEntity(context, "source"),
                                                    SlotArgument.getSlot(context, "sourceSlot"),
                                                    ResourceLocationArgument.getItemModifier(
                                                        context, "modifier"),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    CurioArgumentType.getSlot(context, "slot"),
                                                    IntegerArgumentType.getInteger(context, "index")
                                                )
                                            )
                                    )
                                )
                            )
                        ).then(
                            Commands.literal("entity-curios").then(
                                Commands.argument("sourceSlot", CurioArgumentType.slot()).then(
                                    Commands.argument("sourceIndex", IntegerArgumentType.integer())
                                        .then(
                                            Commands.argument("source", EntityArgument.player())
                                                .executes(
                                                    context -> entityToSlot(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "source"),
                                                        CurioArgumentType.getSlot(context,
                                                                                  "sourceSlot"),
                                                        IntegerArgumentType.getInteger(context,
                                                                                       "sourceIndex"),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        CurioArgumentType.getSlot(context, "slot"),
                                                        IntegerArgumentType.getInteger(context,
                                                                                       "index")
                                                    )
                                                ).then(
                                                    Commands.argument("modifier",
                                                                      ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_MODIFIER)
                                                        .executes(
                                                            context -> entityToSlot(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context,
                                                                                         "source"),
                                                                CurioArgumentType.getSlot(context,
                                                                                          "sourceSlot"),
                                                                IntegerArgumentType.getInteger(context,
                                                                                               "sourceIndex"),
                                                                ResourceLocationArgument.getItemModifier(
                                                                    context, "modifier"),
                                                                EntityArgument.getPlayer(context,
                                                                                         "player"),
                                                                CurioArgumentType.getSlot(context,
                                                                                          "slot"),
                                                                IntegerArgumentType.getInteger(context,
                                                                                               "index")
                                                            )
                                                        )
                                                )
                                        )
                                )
                            )
                        )
                    )))));

    curiosCommand.then(Commands.literal("set").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> setSlotsForPlayer(context.getSource(),
                                             EntityArgument.getPlayer(context, "player"),
                                             CurioArgumentType.getSlot(context, "slot"), 1)).then(
                Commands.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> setSlotsForPlayer(context.getSource(),
                                                 EntityArgument.getPlayer(context, "player"),
                                                 CurioArgumentType.getSlot(context, "slot"),
                                                 IntegerArgumentType.getInteger(context,
                                                                                "amount")))))));

    curiosCommand.then(Commands.literal("add").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> growSlotForPlayer(context.getSource(),
                                             EntityArgument.getPlayer(context, "player"),
                                             CurioArgumentType.getSlot(context, "slot"), 1)).then(
                Commands.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> growSlotForPlayer(context.getSource(),
                                                 EntityArgument.getPlayer(context, "player"),
                                                 CurioArgumentType.getSlot(context, "slot"),
                                                 IntegerArgumentType.getInteger(context,
                                                                                "amount")))))));

    curiosCommand.then(Commands.literal("remove").then(
        Commands.argument("slot", CurioArgumentType.slot()).then(
            Commands.argument("player", EntityArgument.player()).executes(
                context -> shrinkSlotForPlayer(context.getSource(),
                                               EntityArgument.getPlayer(context, "player"),
                                               CurioArgumentType.getSlot(context, "slot"), 1)).then(
                Commands.argument("amount", IntegerArgumentType.integer()).executes(
                    context -> shrinkSlotForPlayer(context.getSource(),
                                                   EntityArgument.getPlayer(context, "player"),
                                                   CurioArgumentType.getSlot(context, "slot"),
                                                   IntegerArgumentType.getInteger(context,
                                                                                  "amount")))))));

    curiosCommand.then(Commands.literal("clear").then(
        Commands.argument("player", EntityArgument.player()).executes(
            context -> clearSlotsForPlayer(context.getSource(),
                                           EntityArgument.getPlayer(context, "player"), "")).then(
            Commands.argument("slot", CurioArgumentType.slot()).executes(
                context -> clearSlotsForPlayer(context.getSource(),
                                               EntityArgument.getPlayer(context, "player"),
                                               CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(Commands.literal("drop").then(
        Commands.argument("player", EntityArgument.player()).executes(
            context -> dropSlotsForPlayer(context.getSource(),
                                          EntityArgument.getPlayer(context, "player"), "")).then(
            Commands.argument("slot", CurioArgumentType.slot()).executes(
                context -> dropSlotsForPlayer(context.getSource(),
                                              EntityArgument.getPlayer(context, "player"),
                                              CurioArgumentType.getSlot(context, "slot"))))));

    curiosCommand.then(Commands.literal("reset").then(
        Commands.argument("player", EntityArgument.player()).executes(
            context -> resetSlotsForPlayer(context.getSource(),
                                           EntityArgument.getPlayer(context, "player")))));

    dispatcher.register(curiosCommand);
  }

  private static int entityToSlot(CommandSourceStack source, ServerPlayer sourcePlayer,
                                  String sourceSlot, int sourceIndex,
                                  LootItemFunction lootFunction, ServerPlayer player,
                                  String slot, int index) {
    return replaceItemForPlayer(source, player, slot, index,
                                applyModifier(source, lootFunction,
                                              getEntityItem(sourcePlayer, sourceSlot,
                                                            sourceIndex)));
  }

  private static int entityToSlot(CommandSourceStack source, ServerPlayer sourcePlayer,
                                  String sourceSlot, int sourceIndex, ServerPlayer player,
                                  String slot, int index) {
    return replaceItemForPlayer(source, player, slot, index,
                                getEntityItem(sourcePlayer, sourceSlot, sourceIndex));
  }

  private static int entityToSlot(CommandSourceStack source, Entity entity, int sourceSlot,
                                  LootItemFunction lootFunction, ServerPlayer player,
                                  String slot, int index) throws CommandSyntaxException {
    return replaceItemForPlayer(source, player, slot, index,
                                applyModifier(source, lootFunction,
                                              getEntityItem(entity, sourceSlot)));
  }

  private static int entityToSlot(CommandSourceStack source, Entity entity, int sourceSlot,
                                  ServerPlayer player, String slot, int index)
      throws CommandSyntaxException {
    return replaceItemForPlayer(source, player, slot, index, getEntityItem(entity, sourceSlot));
  }

  private static int blockToSlot(CommandSourceStack source, BlockPos pos, int sourceSlot,
                                 LootItemFunction lootFunction, ServerPlayer player,
                                 String slot, int index)
      throws CommandSyntaxException {
    return replaceItemForPlayer(source, player, slot, index,
                                applyModifier(source, lootFunction,
                                              getBlockItem(source, pos, sourceSlot)));
  }

  private static int blockToSlot(CommandSourceStack source, BlockPos pos, int sourceSlot,
                                 ServerPlayer player, String slot, int index)
      throws CommandSyntaxException {
    return replaceItemForPlayer(source, player, slot, index, getBlockItem(source, pos, sourceSlot));
  }

  private static ItemStack getBlockItem(CommandSourceStack source, BlockPos pos, int slot)
      throws CommandSyntaxException {
    Container container = getContainer(source, pos);

    if (slot >= 0 && slot < container.getContainerSize()) {
      return container.getItem(slot).copy();
    } else {
      throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(slot);
    }
  }

  private static ItemStack getEntityItem(ServerPlayer player, String slot, int index) {
    ItemStack[] stack = new ItemStack[] {ItemStack.EMPTY};
    CuriosApi.getCuriosInventory(player).resolve().flatMap(inv -> inv.findCurio(slot, index))
        .ifPresent(slotResult -> stack[0] = slotResult.stack().copy());
    return stack[0];
  }

  private static ItemStack getEntityItem(Entity entity, int slot) throws CommandSyntaxException {
    SlotAccess slotaccess = entity.getSlot(slot);

    if (slotaccess == SlotAccess.NULL) {
      throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(slot);
    } else {
      return slotaccess.get().copy();
    }
  }

  static Container getContainer(CommandSourceStack source, BlockPos pos)
      throws CommandSyntaxException {
    BlockEntity blockentity = source.getLevel().getBlockEntity(pos);

    if (blockentity instanceof Container container) {
      return container;
    }
    throw ERROR_SOURCE_NOT_A_CONTAINER.create(pos.getX(), pos.getY(), pos.getZ());
  }

  private static ItemStack applyModifier(CommandSourceStack source,
                                         LootItemFunction lootFunction, ItemStack stack) {
    ServerLevel serverlevel = source.getLevel();
    LootParams lootparams =
        (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN,
                                                            source.getPosition())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity())
            .create(LootContextParamSets.COMMAND);
    LootContext lootcontext = (new LootContext.Builder(lootparams)).create(null);
    lootcontext.pushVisitedElement(LootContext.createVisitedEntry(lootFunction));
    return lootFunction.apply(stack, lootcontext);
  }

  private static int replaceItemForPlayer(CommandSourceStack source, ServerPlayer player,
                                          String slot, int index, ItemStack stack) {
    CuriosApi.getCuriosInventory(player).ifPresent(inv -> inv.setEquippedCurio(slot, index, stack));
    source.sendSuccess(() -> Component.translatable("commands.curios.replace.success", slot,
                                                    player.getDisplayName(),
                                                    stack.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int replaceItemForPlayer(CommandSourceStack source, ServerPlayer player,
                                          String slot, int index, ItemInput item)
      throws CommandSyntaxException {
    return replaceItemForPlayer(source, player, slot, index, item, 1);
  }

  private static int replaceItemForPlayer(CommandSourceStack source, ServerPlayer player,
                                          String slot, int index, ItemInput item, int count)
      throws CommandSyntaxException {
    ItemStack stack = item.createItemStack(count, false);
    CuriosApi.getCuriosHelper().setEquippedCurio(player, slot, index, stack);
    source.sendSuccess(() -> Component.translatable("commands.curios.replace.success", slot,
                                                    player.getDisplayName(),
                                                    stack.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int setSlotsForPlayer(CommandSourceStack source, ServerPlayer playerMP,
                                       String slot, int amount) {
    CuriosApi.getSlotHelper().setSlotsForType(slot, playerMP, amount);
    source.sendSuccess(() -> Component.translatable("commands.curios.set.success", slot,
                                                    CuriosApi.getSlotHelper()
                                                        .getSlotsForType(playerMP, slot),
                                                    playerMP.getDisplayName()),
                       true);
    return Command.SINGLE_SUCCESS;
  }

  private static int growSlotForPlayer(CommandSourceStack source, ServerPlayer playerMP,
                                       String slot, int amount) {
    CuriosApi.getSlotHelper().growSlotType(slot, amount, playerMP);
    source.sendSuccess(() -> Component.translatable("commands.curios.add.success", amount, slot,
                                                    playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int shrinkSlotForPlayer(CommandSourceStack source, ServerPlayer playerMP,
                                         String slot, int amount) {
    CuriosApi.getSlotHelper().shrinkSlotType(slot, amount, playerMP);
    source.sendSuccess(() -> Component.translatable("commands.curios.remove.success", amount, slot,
                                                    playerMP.getDisplayName()), true);
    return Command.SINGLE_SUCCESS;
  }

  private static int dropSlotsForPlayer(CommandSourceStack source, ServerPlayer playerMP,
                                        String slot) {

    CuriosApi.getCuriosHelper().getCuriosHandler(playerMP).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      if (!slot.isEmpty() && curios.get(slot) != null) {
        drop(curios.get(slot), playerMP);
      } else {

        for (String id : curios.keySet()) {
          drop(curios.get(id), playerMP);
        }
      }
    });

    if (slot.isEmpty()) {
      source.sendSuccess(() -> Component.translatable("commands.curios.dropAll.success",
                                                      playerMP.getDisplayName()), true);
    } else {
      source.sendSuccess(() -> Component.translatable("commands.curios.drop.success", slot,
                                                      playerMP.getDisplayName()), true);
    }
    return Command.SINGLE_SUCCESS;
  }

  private static void drop(ICurioStacksHandler stacksHandler, ServerPlayer serverPlayer) {

    for (int i = 0; i < stacksHandler.getSlots(); i++) {
      ItemStack stack1 = stacksHandler.getStacks().getStackInSlot(i);
      stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
      ItemStack stack2 = stacksHandler.getCosmeticStacks().getStackInSlot(i);
      stacksHandler.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);

      if (!stack1.isEmpty()) {
        serverPlayer.drop(stack1, true, false);
      }

      if (!stack2.isEmpty()) {
        serverPlayer.drop(stack2, true, false);
      }
    }
  }

  private static int clearSlotsForPlayer(CommandSourceStack source, ServerPlayer playerMP,
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
      source.sendSuccess(() -> Component.translatable("commands.curios.clearAll.success",
                                                      playerMP.getDisplayName()), true);
    } else {
      source.sendSuccess(() -> Component.translatable("commands.curios.clear.success", slot,
                                                      playerMP.getDisplayName()), true);
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int resetSlotsForPlayer(CommandSourceStack source, ServerPlayer playerMP) {
    CuriosApi.getCuriosHelper().getCuriosHandler(playerMP).ifPresent(handler -> {
      handler.reset();
      NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerMP),
                                   new SPacketSyncCurios(playerMP.getId(), handler.getCurios()));
    });
    source.sendSuccess(
        () -> Component.translatable("commands.curios.reset.success", playerMP.getDisplayName()),
        true);
    return Command.SINGLE_SUCCESS;
  }

  private static void clear(ICurioStacksHandler stacksHandler) {

    for (int i = 0; i < stacksHandler.getSlots(); i++) {
      stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
      stacksHandler.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
    }
  }
}
