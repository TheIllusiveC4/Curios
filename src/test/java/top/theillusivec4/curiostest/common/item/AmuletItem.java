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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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

  private static final ResourceLocation AMULET_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID,
                                            "textures/entity/amulet.png");
  private Object model;

  public AmuletItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public void curioTick(SlotContext slotContext, ItemStack stack) {
    LivingEntity living = slotContext.entity();

    if (!living.level().isClientSide() && living.tickCount % 40 == 0) {
      living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, true));
    }
  }

  @Nonnull
  @Override
  public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
    return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD.value(), 1.0f, 1.0f);
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
  public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
      ItemStack stack, SlotContext slotContext, PoseStack poseStack,
      MultiBufferSource renderTypeBuffer, int packedLight, S renderState,
      RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context,
      float yRotation, float xRotation) {

    if (this.model == null) {
      this.model = new AmuletModel(
          Minecraft.getInstance().getEntityModels().bakeLayer(CuriosLayerDefinitions.AMULET));
    }

    if (this.model instanceof AmuletModel amuletModel) {

      if (renderState instanceof HumanoidRenderState humanoidRenderState) {
        amuletModel.setupAnim(humanoidRenderState);
      }
      VertexConsumer vertexconsumer = ItemRenderer
          .getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(AMULET_TEXTURE),
                              stack.hasFoil());
      (amuletModel)
          .renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
    }
  }
}
