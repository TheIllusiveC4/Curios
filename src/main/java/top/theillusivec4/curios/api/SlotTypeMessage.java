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

package top.theillusivec4.curios.api;

import net.minecraft.resources.ResourceLocation;

public final class SlotTypeMessage {

  public static final String REGISTER_TYPE = "register_type";
  public static final String MODIFY_TYPE = "modify_type";

  private final String identifier;
  private final Integer priority;
  private final int size;
  private final boolean visible;
  private final boolean cosmetic;
  private final ResourceLocation icon;

  private SlotTypeMessage(Builder builder) {
    this.identifier = builder.identifier;
    this.priority = builder.priority;
    this.size = builder.size;
    this.visible = builder.visible;
    this.cosmetic = builder.cosmetic;
    this.icon = builder.icon;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public ResourceLocation getIcon() {
    return this.icon;
  }

  public Integer getPriority() {
    return this.priority;
  }

  public int getSize() {
    return this.size;
  }

  /**
   * @deprecated Check if {@link SlotTypeMessage#getSize()} returns 0 instead
   */
  @Deprecated
  public boolean isLocked() {
    return getSize() == 0;
  }

  public boolean isVisible() {
    return this.visible;
  }

  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  public static class Builder {

    private final String identifier;
    private Integer priority;
    private int size = 1;
    private boolean visible = true;
    private boolean cosmetic = false;
    private ResourceLocation icon = null;

    public Builder(String identifier) {
      this.identifier = identifier;
    }

    public Builder icon(ResourceLocation icon) {
      this.icon = icon;
      return this;
    }

    public Builder priority(int priority) {
      this.priority = priority;
      return this;
    }

    public Builder size(int size) {
      this.size = size;
      return this;
    }

    /**
     * @deprecated Lock slots by setting size to 0 using {@link SlotTypeMessage.Builder#size(int)}
     */
    @Deprecated
    public Builder lock() {
      this.size = 0;
      return this;
    }

    public Builder hide() {
      this.visible = false;
      return this;
    }

    public Builder cosmetic() {
      this.cosmetic = true;
      return this;
    }

    public SlotTypeMessage build() {
      return new SlotTypeMessage(this);
    }
  }
}
