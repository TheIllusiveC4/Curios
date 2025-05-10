package top.theillusivec4.curios.api.internal.services;

import java.util.Map;
import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;

@ApiStatus.Internal
public interface ICuriosSlots {

  Map<String, ISlotType> getSlotTypes(boolean isClient);

  Map<String, ISlotType> getSlotTypes(LivingEntity livingEntity);

  Map<String, ISlotType> getSlotTypes(EntityType<?> entityType, boolean isClient);

  Map<String, ISlotType> getSlotTypes(ItemStack stack, boolean isClient);

  Map<String, ISlotType> getSlotTypes(ItemStack stack, LivingEntity livingEntity);

  ISlotData getSlotData(String id);

  IEntitiesData getEntitiesData();

  void registerPredicate(ResourceLocation resourceLocation,
                         BiPredicate<SlotContext, ItemStack> slotContent);

  BiPredicate<SlotContext, ItemStack> getPredicate(ResourceLocation resourceLocation);

  Map<ResourceLocation, BiPredicate<SlotContext, ItemStack>> getPredicates();
}
