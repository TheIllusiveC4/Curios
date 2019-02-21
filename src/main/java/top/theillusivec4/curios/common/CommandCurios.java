package top.theillusivec4.curios.common;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.CuriosHelper;
import top.theillusivec4.curios.api.CuriosRegistry;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncMap;

import java.util.SortedMap;

public class CommandCurios {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {

        LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("curios")
                .requires(player -> player.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()));

        literalargumentbuilder.then(Commands.literal("add")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosRegistry.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> addSlotToPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> addSlotToPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"), IntegerArgumentType.getInteger(context, "amount")))))));

        literalargumentbuilder.then(Commands.literal("remove")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosRegistry.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> removeSlotFromPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> removeSlotFromPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"), IntegerArgumentType.getInteger(context, "amount")))))));

        literalargumentbuilder.then(Commands.literal("enable")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosRegistry.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> enableSlotForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("disable")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosRegistry.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> disableSlotForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("clear")
                .then(Commands.argument("player", EntityArgument.singlePlayer())
                        .executes(context -> clearSlotsForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), ""))
                                .then(Commands.argument("slot", StringArgumentType.string())
                                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosRegistry.getTypeIdentifiers(), builder))
                                        .executes(context -> clearSlotsForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.singlePlayer())
                        .executes(context -> resetSlotsForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player")))));

        dispatcher.register(literalargumentbuilder);
    }

    private static int addSlotToPlayer(CommandSource source, EntityPlayerMP playerMP, String slot, int amount) {
        CuriosHelper.addTypeSlotsToEntity(slot, amount, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.add.success", amount, slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeSlotFromPlayer(CommandSource source, EntityPlayerMP playerMP, String slot, int amount) {
        CuriosHelper.removeTypeSlotsFromEntity(slot, amount, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.remove.success", amount, slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int enableSlotForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosHelper.enableTypeForEntity(slot, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.enable.success", slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int disableSlotForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosHelper.disableTypeForEntity(slot, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.disable.success", slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearSlotsForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosHelper.getCuriosHandler(playerMP).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> map = handler.getCurioMap();

            if (!slot.isEmpty() && map.get(slot) != null) {
                clear(map.get(slot));
            } else {

                for (String id : map.keySet()) {
                    clear(map.get(id));
                }
            }
        });

        if (slot.isEmpty()) {
            source.sendFeedback(new TextComponentTranslation("commands.curios.clearAll.success", playerMP.getDisplayName()), true);
        } else {
            source.sendFeedback(new TextComponentTranslation("commands.curios.clear.success", slot, playerMP.getDisplayName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetSlotsForPlayer(CommandSource source, EntityPlayerMP playerMP) {
        CuriosHelper.getCuriosHandler(playerMP).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> slots = Maps.newTreeMap();

            for (String id : CuriosRegistry.getTypeIdentifiers()) {
                CurioType type = CuriosRegistry.getType(id);

                if (type != null && type.isEnabled()) {
                    slots.put(id, new CurioStackHandler(type.getSize()));
                }
            }
            handler.setCurioMap(slots);
            NetworkHandler.INSTANCE.sendTo(new SPacketSyncMap(playerMP.getEntityId(), handler.getCurioMap()),
                    playerMP.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        });
        source.sendFeedback(new TextComponentTranslation("commands.curios.reset.success", playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void clear(CurioStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            stacks.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
