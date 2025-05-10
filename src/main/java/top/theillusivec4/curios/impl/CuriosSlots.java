package top.theillusivec4.curios.impl;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.internal.services.ICuriosSlots;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;
import top.theillusivec4.curios.common.data.CuriosSlotResources;
import top.theillusivec4.curios.common.data.EntitiesData;
import top.theillusivec4.curios.common.data.SlotData;

public class CuriosSlots implements ICuriosSlots {

  private static final Map<ResourceLocation, BiPredicate<SlotContext, ItemStack>> PREDICATES =
      Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());

  private static CuriosSlotResources getSidedSlots(boolean isClient) {
    return isClient ? CuriosSlotResources.CLIENT : CuriosSlotResources.SERVER;
  }

  @Override
  public Map<String, ISlotType> getSlotTypes(boolean isClient) {
    return getSidedSlots(isClient).getSlots();
  }

  @Override
  public Map<String, ISlotType> getSlotTypes(LivingEntity livingEntity) {
    return getSidedSlots(livingEntity.level().isClientSide()).getEntitySlots(
        livingEntity.getType());
  }

  @Override
  public Map<String, ISlotType> getSlotTypes(EntityType<?> entityType, boolean isClient) {
    return getSidedSlots(isClient).getEntitySlots(entityType);
  }

  @Override
  public Map<String, ISlotType> getSlotTypes(ItemStack stack, boolean isClient) {
    Map<String, ISlotType> results = new TreeMap<>();

    for (ISlotType value : getSidedSlots(isClient).getSlots().values()) {
      String key = value.getId();

      if (value.isItemValid(new SlotContext(key, null, 0, false, true), stack)) {
        results.put(key, value);
      }
    }

    if (!stack.is(CuriosTags.GENERIC_EXCLUSIONS) && !results.isEmpty()) {
      String key = CuriosSlotTypes.Preset.CURIO.id();
      results.put(key, ISlotType.get(key));
    }
    return results;
  }

  @Override
  public Map<String, ISlotType> getSlotTypes(ItemStack stack, LivingEntity livingEntity) {
    Map<String, ISlotType> results = new TreeMap<>();
    Map<String, ISlotType> slots = getSlotTypes(livingEntity);

    for (Map.Entry<String, ISlotType> entry : slots.entrySet()) {
      ISlotType value = entry.getValue();

      if (value.isItemValid(new SlotContext(entry.getKey(), livingEntity, 0, false, true), stack)) {
        results.put(entry.getKey(), value);
      }
    }

    if (!stack.is(CuriosTags.GENERIC_EXCLUSIONS) && !results.isEmpty()) {
      String key = CuriosSlotTypes.Preset.CURIO.id();
      results.put(key, ISlotType.get(key));
    }
    return results;
  }

  @Override
  public ISlotData getSlotData(String id) {
    return new SlotData(id, false);
  }

  @Override
  public IEntitiesData getEntitiesData() {
    return new EntitiesData();
  }

  @Override
  public void registerPredicate(ResourceLocation resourceLocation,
                                BiPredicate<SlotContext, ItemStack> predicate) {
    PREDICATES.put(resourceLocation, predicate);
  }

  @Override
  public BiPredicate<SlotContext, ItemStack> getPredicate(ResourceLocation resourceLocation) {
    return PREDICATES.get(resourceLocation);
  }

  @Override
  public Map<ResourceLocation, BiPredicate<SlotContext, ItemStack>> getPredicates() {
    return ImmutableMap.copyOf(PREDICATES);
  }

  static {
    PREDICATES.put(CuriosResources.resource("all"), (ctx, stack) -> true);
    PREDICATES.put(CuriosResources.resource("none"), (ctx, stack) -> false);
    PREDICATES.put(CuriosResources.resource("tag"),
                   (ctx, stack) -> {
                     String id = ctx.identifier();
                     TagKey<Item> tag1 =
                         ItemTags.create(CuriosResources.resource(id));
                     return stack.is(tag1) || stack.is(CuriosTags.CURIO);
                   });
  }
}
