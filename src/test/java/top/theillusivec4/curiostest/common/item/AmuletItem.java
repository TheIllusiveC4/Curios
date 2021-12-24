/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curiostest.common.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.CuriosLayerDefinitions;
import top.theillusivec4.curiostest.client.model.AmuletModel;

public class AmuletItem extends Item implements ICurioItem, ICurioRenderer {
  private static final ResourceLocation AMULET_TEXTURE = new ResourceLocation(CuriosTest.MODID,
      "textures/entity/amulet.png");
  private Object model;

  public AmuletItem() {
    super(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).defaultDurability(0));
  }

  @Override
  public void curioTick(SlotContext slotContext, ItemStack stack) {
    LivingEntity living = slotContext.entity();

    if (!living.level.isClientSide() && living.tickCount % 40 == 0) {
      living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, true));
    }
  }

  @Nonnull
  @Override
  public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
    return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
  }

  @Override
  public boolean canEquipFromUse(SlotContext slot, ItemStack stack) {
    return true;
  }

  @Override
  public boolean isFoil(@Nonnull ItemStack stack) {
    return true;
  }

  @Override
  public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
    return 3;
  }

  @Override
  public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                        SlotContext slotContext,
                                                                        PoseStack matrixStack,
                                                                        RenderLayerParent<T, M> renderLayerParent,
                                                                        MultiBufferSource renderTypeBuffer,
                                                                        int light, float limbSwing,
                                                                        float limbSwingAmount,
                                                                        float partialTicks,
                                                                        float ageInTicks,
                                                                        float netHeadYaw,
                                                                        float headPitch) {

    if (this.model == null) {
      this.model = new AmuletModel<>(
          Minecraft.getInstance().getEntityModels().bakeLayer(CuriosLayerDefinitions.AMULET));
    }

    if (this.model instanceof AmuletModel) {
      ICurioRenderer.translateIfSneaking(matrixStack, slotContext.entity());
      ICurioRenderer.rotateIfSneaking(matrixStack, slotContext.entity());
      VertexConsumer vertexconsumer = ItemRenderer
          .getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(AMULET_TEXTURE), false,
              stack.hasFoil());
      ((AmuletModel<?>) this.model)
          .renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
              1.0F, 1.0F);
    }
  }
}
