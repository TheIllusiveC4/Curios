package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.capability.CapCurioInventory;
import top.theillusivec4.curios.api.capability.CapCurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CuriosAPI {

    private static Map<String, CurioType> idToType = Maps.newHashMap();
    private static Map<String, Tag<Item>> idToTag = Maps.newHashMap();
    private static Map<Item, Set<String>> itemToTypes = Maps.newHashMap();

    public static CurioType registerType(@Nonnull String identifier) {
        CurioType entry = new CurioType(identifier);
        idToType.put(identifier, entry);
        return entry;
    }

    @Nullable
    public static CurioType getType(String identifier) {
        return idToType.get(identifier);
    }

    public static ImmutableMap<String, CurioType> getTypeRegistry() {
        return ImmutableMap.copyOf(idToType);
    }

    @SuppressWarnings("ConstantConditions")
    public static LazyOptional<ICurio> getCurio(ItemStack stack) {
        return stack.getCapability(CapCurioItem.CURIO_CAP);
    }

    @SuppressWarnings("ConstantConditions")
    public static LazyOptional<ICurioItemHandler> getCuriosHandler(@Nonnull final EntityLivingBase entityLivingBase) {
        return entityLivingBase.getCapability(CapCurioInventory.CURIO_INV_CAP);
    }

    public static void setTypeEnabled(String id, boolean enabled) {
        CurioType entry = idToType.get(id);

        if (entry != null) {
            entry.setEnabled(enabled);
        }
    }

    public static void addTypeSlotToEntity(String id, final EntityLivingBase entityLivingBase) {
        addTypeSlotsToEntity(id, 1, entityLivingBase);
    }

    public static void addTypeSlotsToEntity(String id, int amount, final EntityLivingBase entityLivingBase) {

        getCuriosHandler(entityLivingBase).ifPresent(handler -> {
            ItemStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {

                if (amount < 0) {
                    NonNullList<ItemStack> drops = NonNullList.create();

                    for (int i = stackHandler.getSlots() - 1; i >= stackHandler.getSlots() + amount; i--) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        drops.add(stackHandler.getStackInSlot(i));
                        getCurio(stack).ifPresent(curio -> {
                            if (!stack.isEmpty()) {
                                curio.onUnequipped(stack, id, entityLivingBase);
                                entityLivingBase.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(id, stack));
                            }
                        });
                        stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }

                    if (entityLivingBase instanceof EntityPlayer) {

                        for (ItemStack drop : drops) {
                            ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entityLivingBase, drop);
                        }
                    } else {

                        for (ItemStack drop : drops) {
                            entityLivingBase.entityDropItem(drop, 0.0f);
                        }
                    }
                }
                NonNullList<ItemStack> copy = NonNullList.create();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    copy.add(stackHandler.getStackInSlot(i).copy());
                }
                stackHandler.setSize(stackHandler.getSlots() + amount);

                for (int i = 0; i < copy.size(); i++) {
                    stackHandler.setStackInSlot(i, copy.get(i));
                }

                ItemStackHandler prevStackHandler = handler.getPreviousCurioMap().get(id);

                if (prevStackHandler != null) {
                    NonNullList<ItemStack> copyPrevious = NonNullList.create();

                    for (int i = 0; i < prevStackHandler.getSlots(); i++) {
                        copyPrevious.add(prevStackHandler.getStackInSlot(i).copy());
                    }
                    prevStackHandler.setSize(stackHandler.getSlots() + amount);

                    for (int i = 0; i < copy.size(); i++) {
                        prevStackHandler.setStackInSlot(i, copyPrevious.get(i));
                    }
                }
            }
        });
    }

    public static void enableTypeForEntity(String id, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.addCurioSlot(id));
    }

    public static void disableTypeForEntity(String id, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> {
            ItemStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {

                NonNullList<ItemStack> drops = NonNullList.create();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    drops.add(stack.copy());
                    getCurio(stack).ifPresent(curio -> {
                        if (!stack.isEmpty()) {
                            curio.onUnequipped(stack, id, entityLivingBase);
                            entityLivingBase.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(id, stack));
                        }
                    });
                }

                if (entityLivingBase instanceof EntityPlayer) {

                    for (ItemStack drop : drops) {
                        ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entityLivingBase, drop);
                    }
                } else {

                    for (ItemStack drop : drops) {
                        entityLivingBase.entityDropItem(drop, 0.0f);
                    }
                }
                handler.removeCurioSlot(id);
            }
        });
    }

    public static Set<String> getCurioTags(Item item) {

        if (idToTag.isEmpty()) {
            refreshTags();
        }

        if (itemToTypes.containsKey(item)) {
            return itemToTypes.get(item);
        } else {
            Set<String> tags = Sets.newHashSet();

            for (String identifier : idToTag.keySet()) {

                if (idToTag.get(identifier).contains(item)) {
                    tags.add(identifier);
                }
            }
            itemToTypes.put(item, tags);
            return tags;
        }
    }

    public static void refreshTags() {
        Map<ResourceLocation, Tag<Item>> tags = ItemTags.getCollection().getTagMap();
        idToTag = ItemTags.getCollection().getTagMap().entrySet()
                .stream()
                .filter(map -> map.getKey().getNamespace().equals(Curios.MODID))
                .collect(Collectors.toMap(entry -> entry.getKey().getPath(), Map.Entry::getValue));
        itemToTypes.clear();
    }
}
