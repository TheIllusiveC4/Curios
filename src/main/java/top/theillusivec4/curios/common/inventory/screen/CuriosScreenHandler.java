package top.theillusivec4.curios.common.inventory.screen;

import static net.minecraft.screen.PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE;
import static net.minecraft.screen.PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE;
import static net.minecraft.screen.PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE;
import static net.minecraft.screen.PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.network.NetworkPackets;
import top.theillusivec4.curios.mixin.IScreenHandlerAccessor;

public class CuriosScreenHandler extends CraftingScreenHandler {

  private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES;
  private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER;

  private final CraftingInventory craftingInput = new CraftingInventory(this, 2, 2);
  private final CraftingResultInventory craftingResult = new CraftingResultInventory();
  private final PlayerEntity owner;
  private final ICuriosItemHandler curiosHandler;
  private final boolean onServer;

  private int lastScrollIndex;
  private boolean cosmeticColumn;

  static {
    EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{EMPTY_BOOTS_SLOT_TEXTURE,
        EMPTY_LEGGINGS_SLOT_TEXTURE, EMPTY_CHESTPLATE_SLOT_TEXTURE, EMPTY_HELMET_SLOT_TEXTURE};
    EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
        EquipmentSlot.LEGS, EquipmentSlot.FEET};
  }

  public CuriosScreenHandler(int syncId, PlayerInventory playerInventory) {
    super(syncId, playerInventory);
    this.slots.clear();
    this.owner = playerInventory.player;
    this.onServer = !playerInventory.player.world.isClient();
    this.addSlot(
        new CraftingResultSlot(playerInventory.player, this.craftingInput, this.craftingResult, 0,
            154, 28));

    int n;
    int m;

    for (n = 0; n < 2; ++n) {

      for (m = 0; m < 2; ++m) {
        this.addSlot(new Slot(this.craftingInput, m + n * 2, 98 + m * 18, 18 + n * 18));
      }
    }

    for (n = 0; n < 4; ++n) {
      final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[n];
      this.addSlot(new Slot(playerInventory, 39 - n, 8, 8 + n * 18) {
        public int getMaxStackAmount() {
          return 1;
        }

        public boolean canInsert(ItemStack stack) {
          return equipmentSlot == MobEntity.getPreferredEquipmentSlot(stack);
        }

        public boolean canTakeItems(PlayerEntity playerEntity) {
          ItemStack itemStack = this.getStack();
          return (itemStack.isEmpty() || playerEntity.isCreative() || !EnchantmentHelper
              .hasBindingCurse(itemStack)) && super.canTakeItems(playerEntity);
        }

        @Environment(EnvType.CLIENT)
        public Pair<Identifier, Identifier> getBackgroundSprite() {
          return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
              EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
        }
      });
    }

    for (n = 0; n < 3; ++n) {
      for (m = 0; m < 9; ++m) {
        this.addSlot(new Slot(playerInventory, m + (n + 1) * 9, 8 + m * 18, 84 + n * 18));
      }
    }

    for (n = 0; n < 9; ++n) {
      this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 142));
    }

    this.addSlot(new Slot(playerInventory, 40, 77, 62) {
      @Environment(EnvType.CLIENT)
      public Pair<Identifier, Identifier> getBackgroundSprite() {
        return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
      }
    });
    this.curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(playerInventory.player)
        .orElse(null);
    this.getCuriosHandler().ifPresent(curios -> {
      Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
      int slots = 0;
      int yOffset = 12;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.size() && slots < 8; i++) {
            this.addSlot(new CurioSlot(owner, stackHandler, i, identifier, -18, yOffset,
                stacksHandler.getRenders()));

            if (stacksHandler.hasCosmetic()) {
              IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
              this.cosmeticColumn = true;
              this.addSlot(
                  new CosmeticCurioSlot(owner, cosmeticHandler, i, identifier, -37, yOffset));
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

  public Optional<ICuriosItemHandler> getCuriosHandler() {
    return Optional.of(curiosHandler);
  }

  public void scrollToIndex(int indexIn) {
    this.getCuriosHandler().ifPresent(curios -> {
      Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
      int slots = 0;
      int yOffset = 12;
      int index = 0;
      this.slots.subList(46, this.slots.size()).clear();
      DefaultedList<ItemStack> stacks = ((IScreenHandlerAccessor) this).getTrackedStacks();

      if (stacks != null) {
        stacks.subList(46, stacks.size()).clear();
      }

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.size() && slots < 8; i++) {

            if (index >= indexIn) {
              this.addSlot(new CurioSlot(owner, stackHandler, i, identifier, -18, yOffset,
                  stacksHandler.getRenders()));

              if (stacksHandler.hasCosmetic()) {
                IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
                this.cosmeticColumn = true;
                this.addSlot(
                    new CosmeticCurioSlot(owner, cosmeticHandler, i, identifier, -37, yOffset));
              }
              yOffset += 18;
              slots++;
            }
            index++;
          }
        }
      }

      if (!this.owner.world.isClient()) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeInt(this.syncId);
        packetByteBuf.writeInt(indexIn);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(owner, NetworkPackets.SCROLL, packetByteBuf);
      }
      lastScrollIndex = indexIn;
    });
  }

  public void scrollToPosition(float pos) {
    this.getCuriosHandler().ifPresent(curios -> {
      int k = (curios.getSlots() - 8);
      int j = (int) ((double) (pos * (float) k) + 0.5D);

      if (j < 0) {
        j = 0;
      }

      if (j == this.lastScrollIndex) {
        return;
      }

      if (!this.onServer) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeInt(this.syncId);
        packetByteBuf.writeInt(j);
        ClientSidePacketRegistry.INSTANCE.sendToServer(NetworkPackets.SCROLL, packetByteBuf);
      }
    });
  }

  public boolean canScroll() {
    return this.getCuriosHandler().map(curios -> curios.getSlots() > 8).orElse(false);
  }

  @Override
  public ScreenHandlerType<?> getType() {
    return CuriosRegistry.CURIOS_SCREENHANDLER;
  }

  @Override
  public boolean canUse(PlayerEntity player) {
    return true;
  }

  @Override
  public void populateRecipeFinder(RecipeFinder finder) {
    this.craftingInput.provideRecipeInputs(finder);
  }

  @Override
  public void clearCraftingSlots() {
    this.craftingResult.clear();
    this.craftingInput.clear();
  }

  @Override
  public void onContentChanged(Inventory inventory) {
    if (!this.owner.world.isClient) {
      ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) owner;
      ItemStack itemStack = ItemStack.EMPTY;
      MinecraftServer server = this.owner.world.getServer();

      if (server != null) {
        Optional<CraftingRecipe> optional = this.owner.world.getServer().getRecipeManager()
            .getFirstMatch(RecipeType.CRAFTING, this.craftingInput, this.owner.world);
        if (optional.isPresent()) {
          CraftingRecipe craftingRecipe = optional.get();
          if (this.craftingResult
              .shouldCraftRecipe(this.owner.world, serverPlayerEntity, craftingRecipe)) {
            itemStack = craftingRecipe.craft(this.craftingInput);
          }
        }
        this.craftingResult.setStack(0, itemStack);
        serverPlayerEntity.networkHandler
            .sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, 0, itemStack));
      }
    }
  }

  @Override
  public void close(PlayerEntity player) {
    super.close(player);
    this.craftingResult.clear();

    if (!player.world.isClient) {
      this.dropInventory(player, player.world, this.craftingInput);
    }
  }

  @Override
  public ItemStack transferSlot(PlayerEntity playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot != null && slot.hasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      EquipmentSlot entityequipmentslot = MobEntity.getPreferredEquipmentSlot(itemstack);
      if (index == 0) {

        if (!this.insertItem(itemstack1, 9, 45, true)) {
          return ItemStack.EMPTY;
        }
        slot.onStackChanged(itemstack1, itemstack);
      } else if (index < 5) {

        if (!this.insertItem(itemstack1, 9, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 9) {

        if (!this.insertItem(itemstack1, 9, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (entityequipmentslot.getType() == EquipmentSlot.Type.ARMOR && !this.slots
          .get(8 - entityequipmentslot.getEntitySlotId()).hasStack()) {
        int i = 8 - entityequipmentslot.getEntitySlotId();

        if (!this.insertItem(itemstack1, i, i + 1, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 46 && !CuriosApi.getCuriosHelper().getCurioTags(itemstack.getItem())
          .isEmpty()) {

        if (!this.insertItem(itemstack1, 46, this.slots.size(), false)) {
          return ItemStack.EMPTY;
        }
      } else if (entityequipmentslot == EquipmentSlot.OFFHAND && !(this.slots.get(45)).hasStack()) {

        if (!this.insertItem(itemstack1, 45, 46, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 36) {
        if (!this.insertItem(itemstack1, 36, 45, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index < 45) {
        if (!this.insertItem(itemstack1, 9, 36, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.insertItem(itemstack1, 9, 45, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.setStack(ItemStack.EMPTY);
      } else {
        slot.markDirty();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      ItemStack itemstack2 = slot.onTakeItem(playerIn, itemstack1);

      if (index == 0) {
        playerIn.dropItem(itemstack2, false);
      }
    }
    return itemstack;
  }

  @Override
  public boolean matches(Recipe<? super CraftingInventory> recipe) {
    return recipe.matches(this.craftingInput, this.owner.world);
  }

  @Override
  public int getCraftingResultSlotIndex() {
    return 0;
  }

  @Override
  public int getCraftingWidth() {
    return this.craftingInput.getWidth();
  }

  @Override
  public int getCraftingHeight() {
    return this.craftingInput.getHeight();
  }

  @Override
  @Environment(EnvType.CLIENT)
  public int getCraftingSlotCount() {
    return 5;
  }
}
