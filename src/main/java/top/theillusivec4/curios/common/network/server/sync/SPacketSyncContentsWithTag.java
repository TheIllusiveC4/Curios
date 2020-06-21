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

package top.theillusivec4.curios.common.network.server.sync;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class SPacketSyncContentsWithTag {

  private int entityId;
  private int slotId;
  private String curioId;
  private ItemStack stack;
  private CompoundNBT compound;

  public SPacketSyncContentsWithTag(int entityId, String curioId, int slotId, ItemStack stack,
      CompoundNBT compound) {
    this.entityId = entityId;
    this.slotId = slotId;
    this.stack = stack.copy();
    this.curioId = curioId;
    this.compound = compound;
  }

  public static void encode(SPacketSyncContentsWithTag msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
    buf.writeItemStack(msg.stack);
    buf.writeCompoundTag(msg.compound);
  }

  public static SPacketSyncContentsWithTag decode(PacketBuffer buf) {
    return new SPacketSyncContentsWithTag(buf.readInt(), buf.readString(25), buf.readInt(),
        buf.readItemStack(), buf.readCompoundTag());
  }

  public static void handle(SPacketSyncContentsWithTag msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHandler((LivingEntity) entity).ifPresent(
              handler -> handler.getStacksHandler(msg.curioId).ifPresent(stacksHandler -> {
                ItemStack stack = msg.stack;
                CuriosApi.getCurio(stack).ifPresent(curio -> curio.readSyncData(msg.compound));
                stacksHandler.getStacks().setStackInSlot(msg.slotId, stack);
              }));
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
