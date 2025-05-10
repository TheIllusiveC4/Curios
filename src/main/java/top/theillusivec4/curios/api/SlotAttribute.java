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

package top.theillusivec4.curios.api;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * A wrapper class for representing slot types as attributes for use in attribute modifiers
 */
public class SlotAttribute extends Attribute {

  private static final Map<String, Holder<? extends Attribute>> SLOT_ATTRIBUTES =
      new Object2ObjectArrayMap<>();

  private final String id;

  @SuppressWarnings("unchecked")
  public static Holder<Attribute> getOrCreate(String id) {
    return (Holder<Attribute>) SLOT_ATTRIBUTES.computeIfAbsent(id,
                                                               (k) -> new Holder.Direct<>(
                                                                   new SlotAttribute(id)));
  }

  protected SlotAttribute(String id) {
    super("curios.identifier." + id, 0);
    this.id = id;
  }

  public String id() {
    return this.id;
  }

  public ResourceLocation resourceLocation() {
    return CuriosResources.resource(this.getIdentifier());
  }

  @Nonnull
  @Override
  public MutableComponent toComponent(@Nonnull AttributeModifier modif, @Nonnull TooltipFlag flag) {
    double value = modif.amount();
    String key = value > 0 ? "curios.modifiers.slots.plus" : "curios.modifiers.slots.take";

    if (value > 1) {
      key = key + ".multiple";
    }
    ChatFormatting color = this.getStyle(value > 0);
    Component attrDesc = Component.translatable(this.getDescriptionId());
    Component valueComp = this.toValueComponent(modif.operation(), value, flag);
    MutableComponent comp = Component.translatable(key, valueComp, attrDesc).withStyle(color);
    return comp.append(this.getDebugInfo(modif, flag));
  }

  @Deprecated()
  public String getIdentifier() {
    return this.id;
  }

  public static final Codec<Holder<Attribute>> CODEC = CuriosServices.CODECS.slotAttributeCodec();
  public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Attribute>> STREAM_CODEC =
      CuriosServices.CODECS.slotAttributeStreamCodec();
}
