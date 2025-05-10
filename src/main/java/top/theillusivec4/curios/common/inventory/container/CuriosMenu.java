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

package top.theillusivec4.curios.common.inventory.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.network.server.SPacketPage;
import top.theillusivec4.curios.common.network.server.SPacketQuickMove;
import top.theillusivec4.curios.config.CuriosConfig;
import top.theillusivec4.curios.impl.CuriosRegistry;

public class CuriosMenu extends AbstractCraftingMenu implements ICuriosMenu {

  public final ICuriosItemHandler curiosHandler;
  public final Player player;

  private final boolean isLocalWorld;

  private final CraftingContainer craftMatrix = new TransientCraftingContainer(this, 2, 2);
  private final ResultContainer craftResult = new ResultContainer();
  public int currentPage;
  public int totalPages;
  public List<Integer> grid = new ArrayList<>();
  private final List<ProxySlot> proxySlots = new ArrayList<>();
  private int moveToPage = -1;
  private int moveFromIndex = -1;
  public boolean hasCosmetics;
  public boolean isViewingCosmetics;
  public int panelWidth;

  public CuriosMenu(int windowId, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
    this(windowId, playerInventory);
  }

  public CuriosMenu(int windowId, Inventory playerInventory) {
    super(CuriosRegistry.CURIO_MENU.get(), windowId, 2, 2);
    this.player = playerInventory.player;
    this.isLocalWorld = this.player.level().isClientSide;
    this.curiosHandler = CuriosApi.getCuriosInventory(this.player).orElse(null);
    this.resetSlots();
  }

  public void setPage(int page) {
    this.panelWidth = 0;
    int visibleSlots = 0;
    int maxSlotsPerPage = CuriosConfig.SERVER.maxSlotsPerPage.get();
    int startingIndex = page * maxSlotsPerPage;
    int columns = 0;

    if (this.curiosHandler != null) {
      visibleSlots = this.curiosHandler.getVisibleSlots();
      int slotsOnPage = Math.min(maxSlotsPerPage, visibleSlots - startingIndex);
      int calculatedColumns = (int) Math.ceil((double) slotsOnPage / 8);
      int minimumColumns = Math.min(slotsOnPage, CuriosConfig.SERVER.minimumColumns.get());
      columns = Mth.clamp(calculatedColumns, minimumColumns, 8);
      this.panelWidth = 14 + 18 * columns;
    }
    this.slots.clear();
    this.lastSlots.clear();
    this.remoteSlots.clear();
    this.addResultSlot(player, 154, 28);
    this.addCraftingGridSlots(98, 18);

    for (int i = 0; i < 4; i++) {
      EquipmentSlot equipmentslot = InventoryMenu.SLOT_IDS[i];
      ResourceLocation resourcelocation = InventoryMenu.TEXTURE_EMPTY_SLOTS.get(equipmentslot);
      this.addSlot(new ArmorSlot(this.player.getInventory(), this.player, equipmentslot, 39 - i, 8,
                                 8 + i * 18, resourcelocation));
    }
    this.addStandardInventorySlots(this.player.getInventory(), 8, 84);
    this.addSlot(new Slot(this.player.getInventory(), 40, 77, 62) {

      @Override
      public void setByPlayer(@Nonnull ItemStack newStack, @Nonnull ItemStack oldStack) {
        CuriosMenu.this.owner().onEquipItem(EquipmentSlot.OFFHAND, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
      }

      @Override
      public ResourceLocation getNoItemIcon() {
        return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
      }
    });

    if (this.curiosHandler != null) {
      Map<String, ICurioStacksHandler> curioMap = this.curiosHandler.getCurios();
      this.totalPages =
          (int) Math.ceil((double) visibleSlots / maxSlotsPerPage);
      int index = 0;
      int yOffset = 8;

      if (this.totalPages > 1) {
        yOffset += 8;
      }
      int currentColumn = 1;
      int currentRow = 1;
      int slots = 0;
      this.grid.clear();
      this.proxySlots.clear();
      int currentPage = 0;
      int endingIndex = startingIndex + maxSlotsPerPage;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        boolean isCosmetic = false;
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.hasCosmetic()) {
          this.hasCosmetics = true;

          if (this.isViewingCosmetics) {
            isCosmetic = true;
            stackHandler = stacksHandler.getCosmeticStacks();
          }
        }

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots(); i++) {

            if (index >= startingIndex && index < endingIndex) {

              if (isCosmetic) {
                this.addSlot(
                    new CurioSlot(this.player, stackHandler, i, identifier,
                                  (currentColumn - 1) * 18 + 7 - panelWidth,
                                  yOffset + (currentRow - 1) * 18, stacksHandler.getRenders(),
                                  stacksHandler.getActiveStates(),
                                  stacksHandler.canToggleRendering(), true));
              } else {
                this.addSlot(
                    new CurioSlot(this.player, stackHandler, i, identifier,
                                  (currentColumn - 1) * 18 + 7 - panelWidth,
                                  yOffset + (currentRow - 1) * 18, stacksHandler.getRenders(),
                                  stacksHandler.getActiveStates(),
                                  stacksHandler.canToggleRendering(), false));
              }

              if (this.grid.size() < currentColumn) {
                this.grid.add(1);
              } else {
                this.grid.set(currentColumn - 1, this.grid.get(currentColumn - 1) + 1);
              }

              if (currentColumn == columns) {
                currentColumn = 1;
                currentRow++;
              } else {
                currentColumn++;
              }
            } else {

              if (isCosmetic) {
                this.proxySlots.add(new ProxySlot(currentPage,
                                                  new CurioSlot(this.player, stackHandler, i,
                                                                identifier,
                                                                (currentColumn - 1) * 18 + 7
                                                                    - panelWidth,
                                                                yOffset + (currentRow - 1) * 18,
                                                                stacksHandler.getRenders(),
                                                                stacksHandler.getActiveStates(),
                                                                stacksHandler.canToggleRendering(),
                                                                true)));
              } else {
                this.proxySlots.add(new ProxySlot(currentPage,
                                                  new CurioSlot(this.player, stackHandler, i,
                                                                identifier,
                                                                (currentColumn - 1) * 18 + 7
                                                                    - panelWidth,
                                                                yOffset + (currentRow - 1) * 18,
                                                                stacksHandler.getRenders(),
                                                                stacksHandler.getActiveStates(),
                                                                stacksHandler.canToggleRendering(),
                                                                false)));
              }
            }
            slots++;

            if (slots >= maxSlotsPerPage) {
              slots = 0;
              currentPage++;
            }
            index++;
          }
        }
      }

      if (!this.isLocalWorld) {
        PacketDistributor.sendToPlayer((ServerPlayer) this.player,
                                       new SPacketPage(this.containerId, page));
      }
    }
    this.currentPage = page;
  }

  public void resetSlots() {
    this.setPage(this.currentPage);
  }

  public void toggleCosmetics() {
    this.isViewingCosmetics = !this.isViewingCosmetics;
    this.resetSlots();
  }

  @Override
  public void slotsChanged(@Nonnull Container inventoryIn) {

    if (this.player.level() instanceof ServerLevel serverlevel) {
      CraftingInput craftinginput = craftSlots.asCraftInput();
      ServerPlayer serverplayer = (ServerPlayer) player;
      ItemStack itemstack = ItemStack.EMPTY;
      Optional<RecipeHolder<CraftingRecipe>> optional = serverlevel.getServer()
          .getRecipeManager()
          .getRecipeFor(RecipeType.CRAFTING, craftinginput, serverlevel,
                        (RecipeHolder<CraftingRecipe>) null);

      if (optional.isPresent()) {
        RecipeHolder<CraftingRecipe> recipeholder = optional.get();
        CraftingRecipe craftingrecipe = recipeholder.value();

        if (this.resultSlots.setRecipeUsed(serverplayer, recipeholder)) {
          ItemStack itemstack1 =
              craftingrecipe.assemble(craftinginput, serverlevel.registryAccess());

          if (itemstack1.isItemEnabled(serverlevel.enabledFeatures())) {
            itemstack = itemstack1;
          }
        }
      }
      resultSlots.setItem(0, itemstack);
      this.setRemoteSlot(0, itemstack);
      serverplayer.connection.send(
          new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0,
                                                itemstack));
    }
  }

  @Override
  public void removed(@Nonnull Player playerIn) {
    super.removed(playerIn);
    this.craftResult.clearContent();

    if (!playerIn.level().isClientSide) {
      this.clearContainer(playerIn, this.craftMatrix);
    }
  }

  @Override
  public void setItem(int pSlotId, int pStateId, @Nonnull ItemStack pStack) {

    if (this.slots.size() > pSlotId) {
      super.setItem(pSlotId, pStateId, pStack);
    }
  }

  @Override
  public boolean stillValid(@Nonnull Player player) {
    return true;
  }

  @Nonnull
  @Override
  public ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();
      EquipmentSlot entityequipmentslot = playerIn.getEquipmentSlotForItem(itemstack);
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
      } else if (entityequipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
          && !this.slots.get(8 - entityequipmentslot.getIndex()).hasItem()) {
        int i = 8 - entityequipmentslot.getIndex();

        if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 46 &&
          !CuriosSlotTypes.getItemSlotTypes(itemstack, playerIn).isEmpty()) {

        if (!this.moveItemStackTo(itemstack1, 46, this.slots.size(), false)) {
          int page = this.findAvailableSlot(itemstack1);

          if (page != -1) {
            this.moveToPage = page;
            this.moveFromIndex = index;
          } else {
            return ItemStack.EMPTY;
          }
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

  protected int findAvailableSlot(ItemStack stack) {
    int result = -1;

    if (stack.isStackable()) {

      for (ProxySlot proxySlot : this.proxySlots) {
        Slot slot = proxySlot.slot();
        ItemStack itemstack = slot.getItem();

        if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemstack)) {
          int j = itemstack.getCount() + stack.getCount();
          int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());

          if (j <= maxSize || itemstack.getCount() < maxSize) {
            result = proxySlot.page();
            break;
          }
        }
      }
    }

    if (!stack.isEmpty() && result == -1) {

      for (ProxySlot proxySlot : this.proxySlots) {
        Slot slot1 = proxySlot.slot();
        ItemStack itemstack1 = slot1.getItem();
        if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
          result = proxySlot.page();
          break;
        }
      }
    }
    return result;
  }

  @Nonnull
  @Override
  public RecipeBookType getRecipeBookType() {
    return RecipeBookType.CRAFTING;
  }

  @Nonnull
  @Override
  public Slot getResultSlot() {
    return this.slots.getFirst();
  }

  @Nonnull
  @Override
  public List<Slot> getInputGridSlots() {
    return this.slots.subList(1, 5);
  }

  @Override
  public int getGridWidth() {
    return this.craftMatrix.getWidth();
  }

  @Override
  public int getGridHeight() {
    return this.craftMatrix.getHeight();
  }

  @Nonnull
  @Override
  protected Player owner() {
    return this.player;
  }

  public void nextPage() {
    this.setPage(Math.min(this.currentPage + 1, this.totalPages - 1));
  }

  public void prevPage() {
    this.setPage(Math.max(this.currentPage - 1, 0));
  }

  public void checkQuickMove() {

    if (this.moveToPage != -1) {
      this.setPage(this.moveToPage);
      this.quickMoveStack(this.player, this.moveFromIndex);
      this.moveToPage = -1;

      if (!this.isLocalWorld) {
        PacketDistributor.sendToPlayer((ServerPlayer) this.player,
                                       new SPacketQuickMove(this.containerId, this.moveFromIndex));
      }
    }
  }

  @Nonnull
  @Override
  public Slot getSlot(int index) {

    if (index < 0) {
      return super.getSlot(0);
    } else if (index >= this.slots.size()) {
      return super.getSlot(this.slots.size() - 1);
    }
    return super.getSlot(index);
  }

  private record ProxySlot(int page, Slot slot) {

  }
}
