package top.theillusivec4.curios.common.item;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.type.component.ICurio;

public class AmuletItem extends Item {

  private static final Identifier AMULET_TEXTURE = new Identifier(CuriosApi.MODID,
      "textures/entity/amulet.png");

  public AmuletItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamageIfAbsent(0));
    ItemComponentCallbackV2.event(this).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {

              @Override
              public void curioTick(String identifier, int index, LivingEntity livingEntity) {

                if (!livingEntity.getEntityWorld().isClient() && livingEntity.age % 40 == 0) {
                  livingEntity.addStatusEffect(
                      new StatusEffectInstance(StatusEffects.REGENERATION, 80, 0, true, true));
                }
              }

              @Override
              public ComponentType<ICurio> getComponentType() {
                return CuriosComponent.ITEM;
              }
            })));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
