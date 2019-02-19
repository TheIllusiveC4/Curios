package top.theillusivec4.curios.common;

import com.google.common.collect.ImmutableMap;
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
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosAPI;

public class CommandCurios {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {

        LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("curios")
                .requires(player -> player.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()));

        literalargumentbuilder.then(Commands.literal("add")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeRegistry().keySet(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> addSlotToPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> addSlotToPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"), IntegerArgumentType.getInteger(context, "amount")))))));

        literalargumentbuilder.then(Commands.literal("enable")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeRegistry().keySet(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> enableSlotForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("disable")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeRegistry().keySet(), builder))
                        .then(Commands.argument("player", EntityArgument.singlePlayer())
                                .executes(context -> disableSlotForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("clear")
                .then(Commands.argument("player", EntityArgument.singlePlayer())
                        .executes(context -> clearSlotsForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), ""))
                                .then(Commands.argument("slot", StringArgumentType.string())
                                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeRegistry().keySet(), builder))
                                        .executes(context -> clearSlotsForPlayer(context.getSource(), EntityArgument.getOnePlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        dispatcher.register(literalargumentbuilder);
    }

    private static int addSlotToPlayer(CommandSource source, EntityPlayerMP playerMP, String slot, int amount) {
        CuriosAPI.addTypeSlotsToEntity(slot, amount, playerMP);

        if (amount > 0) {
            source.sendFeedback(new TextComponentTranslation("commands.curios.add.success", amount, slot, playerMP.getDisplayName()), true);
        } else if (amount < 0) {
            source.sendFeedback(new TextComponentTranslation("commands.curios.remove.success", -amount, slot, playerMP.getDisplayName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int enableSlotForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosAPI.enableTypeForEntity(slot, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.enable.success", slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int disableSlotForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosAPI.disableTypeForEntity(slot, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.disable.success", slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearSlotsForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosAPI.getCuriosHandler(playerMP).ifPresent(handler -> {
            ImmutableMap<String, ItemStackHandler> map = handler.getCurioMap();

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

    private static void clear(ItemStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            stacks.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
