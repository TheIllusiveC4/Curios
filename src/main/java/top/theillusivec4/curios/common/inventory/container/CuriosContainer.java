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

package top.theillusivec4.curios.common.inventory.container;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketScroll;
import top.theillusivec4.curios.common.network.server.SPacketScroll;

public class CuriosContainer extends InventoryMenu {

  private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
      InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
      InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
  private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] {
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
      EquipmentSlot.FEET};

  public final LazyOptional<ICuriosItemHandler> curiosHandler;
  public final Player player;

  private final boolean isLocalWorld;

  private CraftingContainer craftMatrix = new CraftingContainer(this, 2, 2);
  private ResultContainer craftResult = new ResultContainer();
  private int lastScrollIndex;
  private boolean cosmeticColumn;

  public CuriosContainer(int windowId, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
    this(windowId, playerInventory);
  }

  public CuriosContainer(int windowId, Inventory playerInventory) {
    super(playerInventory, playerInventory.player.level.isClientSide, playerInventory.player);
    this.menuType = CuriosRegistry.CONTAINER_TYPE;
    this.containerId = windowId;
    this.lastSlots.clear();
    this.slots.clear();
    this.player = playerInventory.player;
    this.isLocalWorld = this.player.level.isClientSide;
    this.curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(this.player);
    this.addSlot(
        new ResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154,
            28));

    for (int i = 0; i < 2; ++i) {

      for (int j = 0; j < 2; ++j) {
        this.addSlot(new Slot(this.craftMatrix, j + i * 2, 98 + j * 18, 18 + i * 18));
      }
    }

    for (int k = 0; k < 4; ++k) {
      final EquipmentSlot equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
      this.addSlot(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18) {

        @Override
        public int getMaxStackSize() {
          return 1;
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
          return stack.canEquip(equipmentslottype, CuriosContainer.this.player);
        }

        @Override
        public boolean mayPickup(@Nonnull Player playerIn) {
          ItemStack itemstack = this.getItem();
          return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper
              .hasBindingCurse(itemstack)) && super.mayPickup(playerIn);
        }


        @OnlyIn(Dist.CLIENT)
        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
          return Pair.of(InventoryMenu.BLOCK_ATLAS,
              ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
        }
      });
    }

    for (int l = 0; l < 3; ++l) {

      for (int j1 = 0; j1 < 9; ++j1) {
        this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
      }
    }

    for (int i1 = 0; i1 < 9; ++i1) {
      this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
    }
    this.addSlot(new Slot(playerInventory, 40, 77, 62) {
      @OnlyIn(Dist.CLIENT)
      @Override
      public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair
            .of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
      }
    });

    this.curiosHandler.ifPresent(curios -> {
      Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
      int slots = 0;
      int yOffset = 12;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
            this.addSlot(new CurioSlot(this.player, stackHandler, i, identifier, -18, yOffset,
                stacksHandler.getRenders()));
            yOffset += 18;
            slots++;
          }
        }
      }
      yOffset = 12;
      slots = 0;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {

            if (stacksHandler.hasCosmetic()) {
              IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
              this.cosmeticColumn = true;
              this.addSlot(
                  new CosmeticCurioSlot(this.player, cosmeticHandler, i, identifier, -37, yOffset));
            }
            yOffset += 18;
            slots++;
          }
        }
      }
    });
    this.scrollToIndex(0);
  }

  public boolean hasCosmeticColumn() {
    return this.cosmeticColumn;
  }

  public void resetSlots() {
    this.scrollToIndex(this.lastScrollIndex);
  }

  public void scrollToIndex(int indexIn) {

    this.curiosHandler.ifPresent(curios -> {
      Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
      int slots = 0;
      int yOffset = 12;
      int index = 0;
      int startingIndex = indexIn;
      this.slots.subList(46, this.slots.size()).clear();
      this.lastSlots.subList(46, this.lastSlots.size()).clear();
      this.remoteSlots.subList(46, this.remoteSlots.size()).clear();

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {

            if (index >= startingIndex) {
              slots++;
            }
            index++;
          }
        }
      }
      startingIndex = Math.min(startingIndex, Math.max(0, index - 8));
      index = 0;
      slots = 0;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {

            if (index >= startingIndex) {
              this.addSlot(new CurioSlot(this.player, stackHandler, i, identifier, -18, yOffset,
                  stacksHandler.getRenders()));
              yOffset += 18;
              slots++;
            }
            index++;
          }
        }
      }
      index = 0;
      slots = 0;
      yOffset = 12;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {

            if (index >= startingIndex) {

              if (stacksHandler.hasCosmetic()) {
                IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
                this.cosmeticColumn = true;
                this.addSlot(
                    new CosmeticCurioSlot(this.player, cosmeticHandler, i, identifier, -37,
                        yOffset));
              }
              yOffset += 18;
              slots++;
            }
            index++;
          }
        }
      }

      if (!this.isLocalWorld) {
        NetworkHandler.INSTANCE
            .send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player),
                new SPacketScroll(this.containerId, indexIn));
      }
      this.lastScrollIndex = indexIn;
    });
  }

  public void scrollTo(float pos) {

    this.curiosHandler.ifPresent(curios -> {
      int k = (curios.getVisibleSlots() - 8);
      int j = (int) (pos * k + 0.5D);

      if (j < 0) {
        j = 0;
      }

      if (j == this.lastScrollIndex) {
        return;
      }

      if (this.isLocalWorld) {
        NetworkHandler.INSTANCE
            .send(PacketDistributor.SERVER.noArg(), new CPacketScroll(this.containerId, j));
      }
    });
  }

  @Override
  public void slotsChanged(@Nonnull Container inventoryIn) {

    if (!this.player.level.isClientSide) {
      ServerPlayer playerMP = (ServerPlayer) this.player;
      ItemStack stack = ItemStack.EMPTY;
      MinecraftServer server = this.player.level.getServer();

      if (server == null) {
        return;
      }
      Optional<CraftingRecipe> recipe = server.getRecipeManager()
          .getRecipeFor(RecipeType.CRAFTING, this.craftMatrix, this.player.level);

      if (recipe.isPresent()) {
        CraftingRecipe craftingRecipe = recipe.get();
        if (this.craftResult.setRecipeUsed(this.player.level, playerMP, craftingRecipe)) {
          stack = craftingRecipe.assemble(this.craftMatrix);
        }
      }
      this.craftResult.setItem(0, stack);
      this.setRemoteSlot(0, stack);
      playerMP.connection.send(
          new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0,
              stack));
    }
  }

  @Override
  public void removed(@Nonnull Player playerIn) {
    super.removed(playerIn);
    this.craftResult.clearContent();

    if (!playerIn.level.isClientSide) {
      this.clearContainer(playerIn, this.craftMatrix);
    }
  }

  public boolean canScroll() {

    return this.curiosHandler.map(curios -> {

      if (curios.getVisibleSlots() > 8) {
        return 1;
      }
      return 0;
    }).orElse(0) == 1;
  }

  @Override
  public boolean stillValid(@Nonnull Player playerIn) {

    return true;
  }

  @Override
  public void setItem(int pSlotId, int pStateId, @Nonnull ItemStack pStack) {

    if (this.slots.size() > pSlotId) {
      super.setItem(pSlotId, pStateId, pStack);
    }
  }

  @Nonnull
  @Override
  public ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();
      EquipmentSlot entityequipmentslot = Mob.getEquipmentSlotForItem(itemstack);
      if (index == 0) {

        if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickCraft(itemstack1, itemstack);
      } else if (index < 5) {

        if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 9) {

        if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (entityequipmentslot.getType() == EquipmentSlot.Type.ARMOR
          && !this.slots.get(8 - entityequipmentslot.getIndex()).hasItem()) {
        int i = 8 - entityequipmentslot.getIndex();

        if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 46 && !CuriosApi.getCuriosHelper().getCurioTags(itemstack.getItem())
          .isEmpty()) {

        if (!this.moveItemStackTo(itemstack1, 46, this.slots.size(), false)) {
          return ItemStack.EMPTY;
        }
      } else if (entityequipmentslot == EquipmentSlot.OFFHAND && !(this.slots.get(45))
          .hasItem()) {

        if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 36) {
        if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 45) {
        if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(playerIn, itemstack1);

      if (index == 0) {
        playerIn.drop(itemstack1, false);
      }
    }

    return itemstack;
  }

  @Nonnull
  @Override
  public RecipeBookType getRecipeBookType() {
    return RecipeBookType.CRAFTING;
  }

  @Override
  public void fillCraftSlotsStackedContents(@Nonnull StackedContents itemHelperIn) {
    this.craftMatrix.fillStackedContents(itemHelperIn);
  }

  @Override
  public void clearCraftingContent() {
    this.craftMatrix.clearContent();
    this.craftResult.clearContent();
  }

  @Override
  public boolean recipeMatches(Recipe<? super CraftingContainer> recipeIn) {
    return recipeIn.matches(this.craftMatrix, this.player.level);
  }

  @Override
  public int getResultSlotIndex() {
    return 0;
  }

  @Override
  public int getGridWidth() {
    return this.craftMatrix.getWidth();
  }

  @Override
  public int getGridHeight() {
    return this.craftMatrix.getHeight();
  }

  @Override
  public int getSize() {
    return 5;
  }
}
