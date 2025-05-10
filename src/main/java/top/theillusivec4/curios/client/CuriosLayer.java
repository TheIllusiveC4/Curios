/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CuriosLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
    extends RenderLayer<S, M> {

  private final RenderLayerParent<S, M> renderLayerParent;
  private final EntityRendererProvider.Context context;

  public CuriosLayer(RenderLayerParent<S, M> renderer,
                     EntityRendererProvider.Context context) {
    super(renderer);
    this.renderLayerParent = renderer;
    this.context = context;
  }

  @Override
  public void render(
      PoseStack poseStack,
      @Nonnull MultiBufferSource bufferSource,
      int packedLight,
      S renderState,
      float vertRot,
      float horizRot) {
    poseStack.pushPose();
    List<SlotResult> slots =
        renderState.getRenderDataOrDefault(CuriosClientMod.CUSTOM_RENDER, List.of());

    for (SlotResult slot : slots) {
      ICurioRenderer.get(slot.stack()).render(
          slot.stack(),
          slot.slotContext(),
          poseStack,
          bufferSource,
          packedLight,
          renderState,
          this.renderLayerParent,
          this.context,
          vertRot,
          horizRot
      );
    }
    poseStack.popPose();
  }
}
