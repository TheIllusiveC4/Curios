package top.theillusivec4.curios.client;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import java.util.Set;
import java.util.UUID;

import static net.minecraft.item.ItemStack.DECIMALFORMAT;

public class EventHandlerClient {

    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

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
                Set<String> slots = curio.getCurioTypes(stack);

                for (String s : slots) {
                    tooltip.add(new TextComponentString(" -").appendSibling(new TextComponentTranslation("curios.identifier." + s)));
                }

                for (String identifier : CuriosAPI.getTypeRegistry().keySet()) {
                    Multimap<String, AttributeModifier> multimap = curio.getAttributeModifiers(identifier, stack);

                    if (!multimap.isEmpty()) {
                        EntityPlayer player = evt.getEntityPlayer();
                        tooltip.add(new TextComponentString(""));
                        tooltip.add(new TextComponentTranslation("curios.modifiers", identifier));

                        for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                            AttributeModifier attributemodifier = entry.getValue();
                            double amount = attributemodifier.getAmount();
                            boolean flag = false;

                            if (player != null) {

                                if (attributemodifier.getID() == ATTACK_DAMAGE_MODIFIER) {
                                    amount = amount + player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                                    amount = amount + (double) EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
                                    flag = true;
                                } else if (attributemodifier.getID() == ATTACK_SPEED_MODIFIER) {
                                    amount += player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                                    flag = true;
                                }

                                double d1;

                                if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                                    d1 = amount;
                                } else {
                                    d1 = amount * 100.0D;
                                }

                                if (flag) {
                                    tooltip.add((new TextComponentString(" ")).appendSibling(new TextComponentTranslation("attribute.modifier.equals." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), new TextComponentTranslation("attribute.name." + entry.getKey()))).applyTextStyle(TextFormatting.DARK_GREEN));
                                } else if (amount > 0.0D) {
                                    tooltip.add((new TextComponentTranslation("attribute.modifier.plus." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), new TextComponentTranslation("attribute.name." + entry.getKey()))).applyTextStyle(TextFormatting.BLUE));
                                } else if (amount < 0.0D) {
                                    d1 = d1 * -1.0D;
                                    tooltip.add((new TextComponentTranslation("attribute.modifier.take." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), new TextComponentTranslation("attribute.name." + entry.getKey()))).applyTextStyle(TextFormatting.RED));
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
