package top.theillusivec4.curios.client;

import com.google.common.collect.Multimap;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventHandlerClient {

    protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    @SubscribeEvent
    public void onKeyInput(TickEvent.ClientTickEvent evt) {

        if (evt.phase != TickEvent.Phase.END) return;

        if (KeyRegistry.openCurios.isPressed()) {
            NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios());
        }
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent evt) {
        ItemStack stack = evt.getItemStack();

        if (!stack.isEmpty()) {
            CuriosAPI.getCurio(evt.getItemStack()).ifPresent(curio -> {
                List<ITextComponent> tooltip = evt.getToolTip();
                tooltip.add(new TextComponentTranslation("curios.name").applyTextStyle(TextFormatting.AQUA));
                List<String> slots = curio.getCurioSlots(stack);

                if (slots.isEmpty()) {
                    tooltip.add(" -" + I18n.format("curios.identifier.generic"));
                } else {
                    for (String s : curio.getCurioSlots(stack)) {
                        tooltip.add(" -" + I18n.format("curios.identifier." + s));
                    }
                }

                for (String identifier : CuriosAPI.getRegistry().keySet()) {
                    Multimap<String, AttributeModifier> multimap = curio.getAttributeModifiers(identifier, stack);

                    if (!multimap.isEmpty()) {
                        EntityPlayer player = evt.getEntityPlayer();
                        tooltip.add("");
                        tooltip.add(I18n.format("curios.modifiers", identifier));

                        for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                            AttributeModifier attributemodifier = entry.getValue();
                            double amount = attributemodifier.getAmount();
                            boolean flag = false;

                            if (player != null) {

                                if (attributemodifier.getID() == ATTACK_DAMAGE_MODIFIER) {
                                    amount = amount + player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                                    amount = amount + (double) EnchantmentHelper.getModifierForCreature(stack,
                                            EnumCreatureAttribute.UNDEFINED);
                                    flag = true;
                                } else if (attributemodifier.getID() == ATTACK_SPEED_MODIFIER) {
                                    amount += player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                                    flag = true;
                                }

                                double d1;

                                if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                                    d1 = amount;
                                } else {
                                    d1 = amount * 100.0D;
                                }

                                if (flag) {
                                    tooltip.add(" " + I18n.format("attribute.modifier.equals." + attributemodifier.getOperation(),
                                            ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry.getKey())));
                                } else if (amount > 0.0D) {
                                    tooltip.add(TextFormatting.BLUE + " " + I18n.format("attribute.modifier.plus." + attributemodifier.getOperation(),
                                            ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry.getKey())));
                                } else if (amount < 0.0D) {
                                    d1 = d1 * -1.0D;
                                    tooltip.add(TextFormatting.RED + " " + I18n.format("attribute.modifier.take." + attributemodifier.getOperation(),
                                            ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry.getKey())));
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
