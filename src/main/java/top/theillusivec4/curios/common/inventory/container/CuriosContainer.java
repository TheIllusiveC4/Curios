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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
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

public class CuriosContainer extends PlayerContainer {

  private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
      PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS,
      PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
  private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[] {
      EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS,
      EquipmentSlotType.FEET};

  public final LazyOptional<ICuriosItemHandler> curiosHandler;

  private final PlayerEntity player;
  private final boolean isLocalWorld;

  private CraftingInventory craftMatrix = new CraftingInventory(this, 2, 2);
  private CraftResultInventory craftResult = new CraftResultInventory();
  private int lastScrollIndex;
  private boolean cosmeticColumn;

  public CuriosContainer(int windowId, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
    this(windowId, playerInventory);
  }

  public CuriosContainer(int windowId, PlayerInventory playerInventory) {
    super(playerInventory, playerInventory.player.world.isRemote, playerInventory.player);
    this.containerType = CuriosRegistry.CONTAINER_TYPE;
    this.windowId = windowId;
    this.inventoryItemStacks.clear();
    this.inventorySlots.clear();
    this.player = playerInventory.player;
    this.isLocalWorld = this.player.world.isRemote;
    this.curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(this.player);
    this.addSlot(
        new CraftingResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154,
            28));

    for (int i = 0; i < 2; ++i) {

      for (int j = 0; j < 2; ++j) {
        this.addSlot(new Slot(this.craftMatrix, j + i * 2, 98 + j * 18, 18 + i * 18));
      }
    }

    for (int k = 0; k < 4; ++k) {
      final EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
      this.addSlot(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18) {

        @Override
        public int getSlotStackLimit() {
          return 1;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
          return stack.canEquip(equipmentslottype, CuriosContainer.this.player);
        }

        @Override
        public boolean canTakeStack(@Nonnull PlayerEntity playerIn) {
          ItemStack itemstack = this.getStack();
          return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper
              .hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
        }


        @OnlyIn(Dist.CLIENT)
        @Override
        public Pair<ResourceLocation, ResourceLocation> getBackground() {
          return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE,
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
      public Pair<ResourceLocation, ResourceLocation> getBackground() {
        return Pair
            .of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
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
      this.inventorySlots.subList(46, this.inventorySlots.size()).clear();

      if (this.inventoryItemStacks != null) {
        this.inventoryItemStacks.subList(46, this.inventoryItemStacks.size()).clear();
      }

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {

            if (index >= indexIn) {
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

            if (index >= indexIn) {

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
            .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.player),
                new SPacketScroll(this.windowId, indexIn));
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
            .send(PacketDistributor.SERVER.noArg(), new CPacketScroll(this.windowId, j));
      }
    });
  }

  @Override
  public void onCraftMatrixChanged(@Nonnull IInventory inventoryIn) {

    if (!this.player.world.isRemote) {
      ServerPlayerEntity playerMP = (ServerPlayerEntity) this.player;
      ItemStack stack = ItemStack.EMPTY;
      MinecraftServer server = this.player.world.getServer();

      if (server == null) {
        return;
      }

      Optional<ICraftingRecipe> recipe = server.getRecipeManager()
          .getRecipe(IRecipeType.CRAFTING, this.craftMatrix, this.player.world);

      if (recipe.isPresent()) {
        ICraftingRecipe craftingRecipe = recipe.get();
        if (this.craftResult.canUseRecipe(this.player.world, playerMP, craftingRecipe)) {
          stack = craftingRecipe.getCraftingResult(this.craftMatrix);
        }
      }
      this.craftResult.setInventorySlotContents(0, stack);
      playerMP.connection.sendPacket(new SSetSlotPacket(this.windowId, 0, stack));
    }
  }

  @Override
  public void onContainerClosed(@Nonnull PlayerEntity playerIn) {
    super.onContainerClosed(playerIn);
    this.craftResult.clear();

    if (!playerIn.world.isRemote) {
      this.clearContainer(playerIn, playerIn.world, this.craftMatrix);
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
  public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {

    return true;
  }

  @Nonnull
  @Override
  public ItemStack transferStackInSlot(@Nonnull PlayerEntity playerIn, int index) {

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      EquipmentSlotType entityequipmentslot = MobEntity.getSlotForItemStack(itemstack);
      if (index == 0) {

        if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
          return ItemStack.EMPTY;
        }
        slot.onSlotChange(itemstack1, itemstack);
      } else if (index < 5) {

        if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 9) {

        if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (entityequipmentslot.getSlotType() == EquipmentSlotType.Group.ARMOR
          && !this.inventorySlots.get(8 - entityequipmentslot.getIndex()).getHasStack()) {
        int i = 8 - entityequipmentslot.getIndex();

        if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 46 && !CuriosApi.getCuriosHelper().getCurioTags(itemstack.getItem())
          .isEmpty()) {

        if (!this.mergeItemStack(itemstack1, 46, this.inventorySlots.size(), false)) {
          return ItemStack.EMPTY;
        }
      } else if (entityequipmentslot == EquipmentSlotType.OFFHAND && !(this.inventorySlots.get(45))
          .getHasStack()) {

        if (!this.mergeItemStack(itemstack1, 45, 46, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 36) {
        if (!this.mergeItemStack(itemstack1, 36, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 45) {
        if (!this.mergeItemStack(itemstack1, 9, 36, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);

      if (index == 0) {
        playerIn.dropItem(itemstack2, false);
      }
    }

    return itemstack;
  }

  @Nonnull
  @Override
  public RecipeBookCategory func_241850_m() {
    return RecipeBookCategory.CRAFTING;
  }

  @Override
  public void fillStackedContents(@Nonnull RecipeItemHelper itemHelperIn) {
    this.craftMatrix.fillStackedContents(itemHelperIn);
  }

  @Override
  public void clear() {
    this.craftMatrix.clear();
    this.craftResult.clear();
  }

  @Override
  public boolean matches(IRecipe<? super CraftingInventory> recipeIn) {
    return recipeIn.matches(this.craftMatrix, this.player.world);
  }

  @Override
  public int getOutputSlot() {
    return 0;
  }

  @Override
  public int getWidth() {
    return this.craftMatrix.getWidth();
  }

  @Override
  public int getHeight() {
    return this.craftMatrix.getHeight();
  }

  @Override
  public int getSize() {
    return 5;
  }
}
