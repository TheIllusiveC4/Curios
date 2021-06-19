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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.model.AmuletModel;

public class AmuletItem extends Item implements ICurioItem, ICurioRenderer {
  private static final ResourceLocation AMULET_TEXTURE = new ResourceLocation(CuriosTest.MODID,
      "textures/entity/amulet.png");
  private Object model;

  public AmuletItem() {
    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
    this.setRegistryName(CuriosTest.MODID, "amulet");
  }

  @Override
  public void curioTick(SlotContext slotContext, ItemStack stack) {
    LivingEntity living = slotContext.getWearer();

    if (!living.getEntityWorld().isRemote && living.ticksExisted % 40 == 0) {
      living.addPotionEffect(new EffectInstance(Effects.REGENERATION, 80, 0, true, true));
    }
  }

  @Nonnull
  @Override
  public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
    return new ICurio.SoundInfo(SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
  }

  @Override
  public boolean canEquipFromUse(SlotContext slot, ItemStack stack) {
    return true;
  }

  @Override
  public boolean hasEffect(@Nonnull ItemStack stack) {
    return true;
  }

  @Override
  public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                        SlotContext slotContext,
                                                                        IEntityRenderer<T, M> entityRenderer,
                                                                        MatrixStack matrixStack,
                                                                        IRenderTypeBuffer renderTypeBuffer,
                                                                        int light, float limbSwing,
                                                                        float limbSwingAmount,
                                                                        float partialTicks,
                                                                        float ageInTicks,
                                                                        float netHeadYaw,
                                                                        float headPitch) {
    ICurioRenderer.translateIfSneaking(matrixStack, slotContext.getWearer());
    ICurioRenderer.rotateIfSneaking(matrixStack, slotContext.getWearer());

    if (!(this.model instanceof AmuletModel)) {
      this.model = new AmuletModel<>();
    }
    AmuletModel<?> amuletModel = (AmuletModel<?>) this.model;
    IVertexBuilder vertexBuilder = ItemRenderer
        .getBuffer(renderTypeBuffer, amuletModel.getRenderType(AMULET_TEXTURE), false,
            stack.hasEffect());
    amuletModel
        .render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
            1.0F);
  }
}
