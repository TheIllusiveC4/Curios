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

package top.theillusivec4.curios.common.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

public class SPacketBreakCurio {

  private int entityId;
  private int slotId;
  private String curioId;

  public SPacketBreakCurio(int entityId, String curioId, int slotId) {

    this.entityId = entityId;
    this.slotId = slotId;
    this.curioId = curioId;
  }

  public static void encode(SPacketBreakCurio msg, PacketBuffer buf) {

    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
  }

  public static SPacketBreakCurio decode(PacketBuffer buf) {

    return new SPacketBreakCurio(buf.readInt(), buf.readString(25), buf.readInt());
  }

  public static void handle(SPacketBreakCurio msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

      if (entity instanceof LivingEntity) {
        LivingEntity livingEntity = (LivingEntity) entity;
        CuriosAPI.getCuriosHandler(livingEntity).ifPresent(handler -> {
          ItemStack stack = handler.getStackInSlot(msg.curioId, msg.slotId);
          CuriosAPI.getCurio(stack).ifPresent(curio -> curio.onCurioBreak(stack, livingEntity));

          if (!CuriosAPI.getCurio(stack).isPresent()) {

            if (!stack.isEmpty()) {

              if (!livingEntity.isSilent()) {
                livingEntity.world.playSound(livingEntity.getPosX(), livingEntity.getPosY(),
                    livingEntity.getPosZ(), SoundEvents.ENTITY_ITEM_BREAK,
                    livingEntity.getSoundCategory(), 0.8F,
                    0.8F + livingEntity.world.rand.nextFloat() * 0.4F, false);
              }

              for (int i = 0; i < 5; ++i) {
                Vec3d vec3d = new Vec3d(((double) livingEntity.getRNG().nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D, 0.0D);
                vec3d = vec3d.rotatePitch(-livingEntity.rotationPitch * ((float) Math.PI / 180F));
                vec3d = vec3d.rotateYaw(-livingEntity.rotationYaw * ((float) Math.PI / 180F));
                double d0 = (double) (-livingEntity.getRNG().nextFloat()) * 0.6D - 0.3D;
                Vec3d vec3d1 = new Vec3d(((double) livingEntity.getRNG().nextFloat() - 0.5D) * 0.3D,
                    d0, 0.6D);
                vec3d1 = vec3d1.rotatePitch(-livingEntity.rotationPitch * ((float) Math.PI / 180F));
                vec3d1 = vec3d1.rotateYaw(-livingEntity.rotationYaw * ((float) Math.PI / 180F));
                vec3d1 = vec3d1.add(livingEntity.getPosX(),
                    livingEntity.getPosY() + (double) livingEntity.getEyeHeight(),
                    livingEntity.getPosZ());

                livingEntity.world
                    .addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), vec3d1.x,
                        vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
              }
            }
          }
        });
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
