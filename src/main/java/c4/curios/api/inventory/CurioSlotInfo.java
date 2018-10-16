package c4.curios.api.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurioSlotInfo {

    private final String identifier;
    private final ResourceLocation slotOverlay;
    private final List<Tuple<ItemStack, Integer>> validStacks;
    private final int maxSlots;

    public CurioSlotInfo(String identifier, @Nullable ResourceLocation slotOverlay, int maxSlots) {
        this.identifier = identifier;
        this.validStacks = new ArrayList<>();
        this.slotOverlay = slotOverlay;
        this.maxSlots = maxSlots;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Tuple<ItemStack, Integer>> getValidStacks() {
        return ImmutableList.copyOf(validStacks);
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public int getStackLimit(ItemStack stack) {
        for (Tuple<ItemStack, Integer> tuple : validStacks) {
            ItemStack present = tuple.getFirst();
            if (ItemStack.areItemStacksEqual(present, stack)) {
                return tuple.getSecond();
            }
        }
        return 64;
    }

    public boolean addValidStack(ItemStack stack, int stackSize) {

        for (Tuple<ItemStack, Integer> tuple : validStacks) {
            ItemStack present = tuple.getFirst();
            if (ItemStack.areItemStacksEqual(present, stack)) {
                return false;
            }
        }

        validStacks.add(new Tuple<>(stack, stackSize));
        return true;
    }

    public String getTranslationKey() {
        return "curios.identifier." + identifier;
    }

    public String getFormattedName() {
        return I18n.format(getTranslationKey());
    }

    @Nullable
    public ResourceLocation getSlotOverlay() {
        return slotOverlay;
    }
}
