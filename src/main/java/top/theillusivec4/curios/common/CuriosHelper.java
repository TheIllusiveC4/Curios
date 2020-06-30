package top.theillusivec4.curios.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;

public class CuriosHelper implements ICuriosHelper {

  private static TriConsumer<String, Integer, LivingEntity> brokenCurioConsumer;

  @Override
  public LazyOptional<ICurio> getCurio(ItemStack stack) {
    return stack.getCapability(CuriosCapability.ITEM);
  }

  @Override
  public LazyOptional<ICuriosItemHandler> getCuriosHandler(
      @Nonnull final LivingEntity livingEntity) {
    return livingEntity.getCapability(CuriosCapability.INVENTORY);
  }

  @Override
  public Set<String> getCurioTags(Item item) {
    return item.getTags().stream().filter(tag -> tag.getNamespace().equals(CuriosApi.MODID))
        .map(ResourceLocation::getPath).collect(Collectors.toSet());
  }

  @Override
  public Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(Item item,
      @Nonnull final LivingEntity livingEntity) {
    return findEquippedCurio((stack) -> stack.getItem() == item, livingEntity);
  }

  @Nonnull
  @Override
  public Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(
      Predicate<ItemStack> filter, @Nonnull final LivingEntity livingEntity) {

    ImmutableTriple<String, Integer, ItemStack> result = getCuriosHandler(livingEntity)
        .map(handler -> {
          Map<String, ICurioStacksHandler> curios = handler.getCurios();

          for (String id : curios.keySet()) {
            ICurioStacksHandler stacksHandler = curios.get(id);
            IDynamicStackHandler stackHandler = stacksHandler.getStacks();

            for (int i = 0; i < stackHandler.getSlots(); i++) {
              ItemStack stack = stackHandler.getStackInSlot(i);

              if (!stack.isEmpty() && filter.test(stack)) {
                return new ImmutableTriple<>(id, i, stack);
              }
            }
          }
          return new ImmutableTriple<>("", 0, ItemStack.EMPTY);
        }).orElse(new ImmutableTriple<>("", 0, ItemStack.EMPTY));

    return result.getLeft().isEmpty() ? Optional.empty() : Optional.of(result);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier,
      ItemStack stack) {
    Multimap<Attribute, AttributeModifier> multimap;

    if (stack.getTag() != null && stack.getTag().contains("CurioAttributeModifiers", 9)) {
      multimap = HashMultimap.create();
      ListNBT listnbt = stack.getTag().getList("CurioAttributeModifiers", 10);

      for (int i = 0; i < listnbt.size(); ++i) {
        CompoundNBT compoundnbt = listnbt.getCompound(i);

        if (!compoundnbt.contains("Slot", 8) || compoundnbt.getString("Slot").equals(identifier)) {
          Attribute attribute = ForgeRegistries.ATTRIBUTES
              .getValue(ResourceLocation.tryCreate(compoundnbt.getString("AttributeName")));

          if (attribute != null) {
            AttributeModifier attributemodifier = AttributeModifier.func_233800_a_(compoundnbt);

            if (attributemodifier != null
                && attributemodifier.getID().getLeastSignificantBits() != 0L
                && attributemodifier.getID().getMostSignificantBits() != 0L) {
              multimap.put(attribute, attributemodifier);
            }
          }
        }
      }
      return multimap;
    }
    return getCurio(stack).map(curio -> curio.getAttributeModifiers(identifier))
        .orElse(HashMultimap.create());
  }

  @Override
  public void onBrokenCurio(String id, int index, LivingEntity damager) {
    brokenCurioConsumer.accept(id, index, damager);
  }

  @Override
  public void setBrokenCurioConsumer(TriConsumer<String, Integer, LivingEntity> consumer) {

    if (brokenCurioConsumer == null) {
      brokenCurioConsumer = consumer;
    }
  }
}
