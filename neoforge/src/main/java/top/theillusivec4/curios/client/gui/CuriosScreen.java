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

package top.theillusivec4.curios.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.client.ICuriosScreen;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.CuriosClientConfig.Client;
import top.theillusivec4.curios.client.CuriosClientConfig.Client.ButtonCorner;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.network.client.CPacketPage;
import top.theillusivec4.curios.common.network.client.CPacketToggleRender;

public class CuriosScreen extends EffectRenderingInventoryScreen<CuriosContainer>
    implements RecipeUpdateListener, ICuriosScreen {

  static final ResourceLocation CURIO_INVENTORY =
      ResourceLocation.fromNamespaceAndPath(
          CuriosConstants.MOD_ID, "textures/gui/curios/inventory.png");

  private final RecipeBookComponent recipeBookGui = new RecipeBookComponent();
  private final Map<String, List<ItemStack>> indexedDisplays = new HashMap<>();
  public boolean widthTooNarrow;

  private ImageButton recipeBookButton;
  private CuriosButton buttonCurios;
  private CosmeticButton cosmeticButton;
  private PageButton nextPage;
  private PageButton prevPage;
  private boolean buttonClicked;
  private boolean isRenderButtonHovered;
  public int panelWidth = 0;

  public CuriosScreen(CuriosContainer curiosContainer, Inventory playerInventory, Component title) {
    super(curiosContainer, playerInventory, title);
  }

  public static Tuple<Integer, Integer> getButtonOffset(boolean isCreative) {
    Client client = CuriosClientConfig.CLIENT;
    ButtonCorner corner = client.buttonCorner.get();
    int x = 0;
    int y = 0;

    if (isCreative) {
      x += corner.getCreativeXoffset() + client.creativeButtonXOffset.get();
      y += corner.getCreativeYoffset() + client.creativeButtonYOffset.get();
    } else {
      x += corner.getXoffset() + client.buttonXOffset.get();
      y += corner.getYoffset() + client.buttonYOffset.get();
    }
    return new Tuple<>(x, y);
  }

  @Override
  public void init() {

    if (this.minecraft != null) {
      this.panelWidth = this.menu.panelWidth;
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      this.widthTooNarrow = true;
      this.recipeBookGui.init(this.width, this.height, this.minecraft, true, this.menu);
      this.addWidget(this.recipeBookGui);
      this.setInitialFocus(this.recipeBookGui);

      /*
       This may not be a perfect workaround as it doesn't return the book upon switching back
       to survival mode. Creative inventory doesn't have this problem because it doesn't have
       recipe book at all, but here we only have one Screen and must toggle it circumstantially,
       and sadly Curios' recipe book isn't and must not be an independent object. I can't think
       of better implementation at the moment though.

       Anyhow, this is better than letting the book persist in creative without a way to toggle it.
       @author Extegral
      */
      if (this.getMinecraft().player != null
          && this.getMinecraft().player.isCreative()
          && this.recipeBookGui.isVisible()) {
        this.recipeBookGui.toggleVisibility();
      }

      Tuple<Integer, Integer> offsets = getButtonOffset(false);
      this.buttonCurios =
          new CuriosButton(
              this,
              this.getGuiLeft() + offsets.getA() - 2,
              this.height / 2 + offsets.getB() - 2,
              10,
              10,
              CuriosButton.BIG);

      if (CuriosClientConfig.CLIENT.enableButton.get()) {
        this.addRenderableWidget(this.buttonCurios);
      }

      if (!this.menu.player.isCreative()) {
        this.recipeBookButton =
            new ImageButton(
                this.leftPos + 104,
                this.height / 2 - 22,
                20,
                18,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                (button) -> {
                  this.recipeBookGui.toggleVisibility();
                  button.setPosition(this.leftPos + 104, this.height / 2 - 22);
                  this.buttonCurios.setPosition(
                      this.leftPos + offsets.getA() + 2, this.height / 2 + offsets.getB());
                });
        this.addRenderableWidget(this.recipeBookButton);
      }
      this.updateRenderButtons();
    }
  }

  public void updateRenderButtons() {
    this.narratables.removeIf(
        widget ->
            widget instanceof RenderButton
                || widget instanceof CosmeticButton
                || widget instanceof PageButton);
    this.children.removeIf(
        widget ->
            widget instanceof RenderButton
                || widget instanceof CosmeticButton
                || widget instanceof PageButton);
    this.renderables.removeIf(
        widget ->
            widget instanceof RenderButton
                || widget instanceof CosmeticButton
                || widget instanceof PageButton);
    this.panelWidth = this.menu.panelWidth;

    if (this.menu.hasCosmetics) {
      this.cosmeticButton =
          new CosmeticButton(this, this.getGuiLeft() + 17, this.getGuiTop() - 18, 20, 17);
      this.addRenderableWidget(this.cosmeticButton);
    }

    if (this.menu.totalPages > 1) {
      this.nextPage =
          new PageButton(
              this, this.getGuiLeft() + 17, this.getGuiTop() + 2, 11, 12, PageButton.Type.NEXT);
      this.addRenderableWidget(this.nextPage);
      this.prevPage =
          new PageButton(
              this, this.getGuiLeft() + 17, this.getGuiTop() + 2, 11, 12, PageButton.Type.PREVIOUS);
      this.addRenderableWidget(this.prevPage);
    }

    for (Slot inventorySlot : this.menu.slots) {

      if (inventorySlot instanceof CurioSlot curioSlot
          && !(inventorySlot instanceof CosmeticCurioSlot)) {

        if (curioSlot.canToggleRender()) {
          this.addRenderableWidget(
              new RenderButton(
                  curioSlot,
                  this.leftPos + inventorySlot.x + 12,
                  this.topPos + inventorySlot.y - 1,
                  8,
                  8,
                  75,
                  0,
                  CURIO_INVENTORY,
                  (button) ->
                      PacketDistributor.sendToServer(
                          new CPacketToggleRender(
                              curioSlot.getIdentifier(), inventorySlot.getSlotIndex()))));
        }
      }
    }
  }

  @Override
  public void containerTick() {
    super.containerTick();
    this.recipeBookGui.tick();
  }

  @Override
  public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    this.indexedDisplays.clear();

    if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.recipeBookGui.render(guiGraphics, mouseX, mouseY, partialTicks);
    } else {
      this.recipeBookGui.render(guiGraphics, mouseX, mouseY, partialTicks);
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.recipeBookGui.renderGhostRecipe(
          guiGraphics, this.leftPos, this.topPos, true, partialTicks);

      boolean isButtonHovered = false;

      for (Renderable button : this.renderables) {

        if (button instanceof RenderButton) {
          ((RenderButton) button).renderButtonOverlay(guiGraphics, mouseX, mouseY, partialTicks);

          if (((RenderButton) button).isHovered()) {
            isButtonHovered = true;
          }
        }
      }
      this.isRenderButtonHovered = isButtonHovered;
      LocalPlayer clientPlayer = Minecraft.getInstance().player;

      if (!this.isRenderButtonHovered
          && clientPlayer != null
          && clientPlayer.inventoryMenu.getCarried().isEmpty()
          && this.getSlotUnderMouse() != null) {
        Slot slot = this.getSlotUnderMouse();

        if (slot instanceof CurioSlot slotCurio && this.minecraft != null) {
          ItemStack stack =
              slotCurio
                  .getSlotExtension()
                  .getDisplayStack(slotCurio.getSlotContext(), slot.getItem());

          if (stack.isEmpty()) {
            List<Component> slotTooltips = new ArrayList<>(
                slotCurio
                    .getSlotExtension()
                    .getSlotTooltip(
                        slotCurio.getSlotContext(),
                        ClientTooltipFlag.of(this.minecraft.options.advancedItemTooltips ?
                                                 TooltipFlag.Default.ADVANCED :
                                                 TooltipFlag.Default.NORMAL)));

            if (slotTooltips.isEmpty()) {
              slotTooltips.add(Component.literal(slotCurio.getSlotName()));
            }

            if (!slotCurio.isActiveState()) {
              slotTooltips.add(Component.translatable("curios.tooltip.inactive")
                                   .withStyle(ChatFormatting.RED));
            }
            guiGraphics.renderComponentTooltip(this.font, slotTooltips, mouseX, mouseY);
          }
        }
      }
    }
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }

  @Override
  protected void renderTooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    Minecraft mc = this.minecraft;

    if (mc != null) {
      LocalPlayer clientPlayer = mc.player;

      if (clientPlayer != null && clientPlayer.inventoryMenu.getCarried().isEmpty()) {

        if (this.isRenderButtonHovered) {
          guiGraphics.renderTooltip(
              this.font, Component.translatable("gui.curios.toggle"), mouseX, mouseY);
        } else if (this.hoveredSlot != null) {
          ItemStack stack = this.hoveredSlot.getItem();

          if (this.hoveredSlot instanceof CurioSlot curioSlot) {
            stack = curioSlot.getSlotExtension().getDisplayStack(curioSlot.getSlotContext(), stack);
          }

          if (!stack.isEmpty()) {
            List<Component> components = Screen.getTooltipFromItem(this.minecraft, stack);

            if (this.hoveredSlot instanceof CurioSlot curioSlot && !curioSlot.isActiveState()) {
              components.add(Component.empty());
              components.add(
                  Component.translatable("curios.tooltip.inactive").withStyle(ChatFormatting.RED));
            }
            guiGraphics.renderTooltip(this.font, components, stack.getTooltipImage(), mouseX,
                                      mouseY);
          }
        }
      }
    }
  }

  @Override
  public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {

    if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
      this.recipeBookGui.toggleVisibility();
      return true;
    } else if (KeyRegistry.openCurios.isActiveAndMatches(
        InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
      LocalPlayer playerEntity = this.getMinecraft().player;

      if (playerEntity != null) {
        playerEntity.closeContainer();
      }
      return true;
    } else {
      return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
  }

  @Override
  protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {

    if (this.minecraft != null && this.minecraft.player != null) {
      guiGraphics.drawString(this.font, this.title, 97, 6, 4210752, false);
    }
  }

  /**
   * Draws the background layer of this container (behind the item).
   */
  @Override
  public void renderBg(
      @Nonnull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

    if (this.minecraft != null && this.minecraft.player != null) {

      if (scrollCooldown > 0 && this.minecraft.player.tickCount % 5 == 0) {
        scrollCooldown--;
      }
      this.panelWidth = this.menu.panelWidth;
      int i = this.leftPos;
      int j = this.topPos;
      guiGraphics.blit(INVENTORY_LOCATION, i, j, 0, 0, 176, this.imageHeight);
      InventoryScreen.renderEntityInInventoryFollowsMouse(
          guiGraphics,
          i + 26,
          j + 8,
          i + 75,
          j + 78,
          30,
          0.0625F,
          mouseX,
          mouseY,
          this.minecraft.player);
      CuriosApi.getCuriosInventory(this.minecraft.player)
          .ifPresent(
              handler -> {
                int xOffset = -33;
                int yOffset = j;
                boolean pageOffset = this.menu.totalPages > 1;

                if (this.menu.hasCosmetics) {
                  guiGraphics.blit(CURIO_INVENTORY, i + xOffset + 2, yOffset - 23, 32, 0, 28, 24);
                }
                List<Integer> grid = this.menu.grid;
                xOffset -= (grid.size() - 1) * 18;

                // render backplate
                for (int r = 0; r < grid.size(); r++) {
                  int rows = grid.getFirst();
                  int upperHeight = 7 + rows * 18;
                  int xTexOffset = 91;

                  if (pageOffset) {
                    upperHeight += 8;
                  }

                  if (r != 0) {
                    xTexOffset += 7;
                  }
                  guiGraphics.blit(
                      CURIO_INVENTORY, i + xOffset, yOffset, xTexOffset, 0, 25, upperHeight);
                  guiGraphics.blit(
                      CURIO_INVENTORY, i + xOffset, yOffset + upperHeight, xTexOffset, 159, 25, 7);

                  if (grid.size() == 1) {
                    xTexOffset += 7;
                    guiGraphics.blit(
                        CURIO_INVENTORY, i + xOffset + 7, yOffset, xTexOffset, 0, 25, upperHeight);
                    guiGraphics.blit(
                        CURIO_INVENTORY,
                        i + xOffset + 7,
                        yOffset + upperHeight,
                        xTexOffset,
                        159,
                        25,
                        7);
                  }

                  if (r == 0) {
                    xOffset += 25;
                  } else {
                    xOffset += 18;
                  }
                }
                xOffset -= (grid.size()) * 18;

                if (pageOffset) {
                  yOffset += 8;
                }

                // render slots
                for (int rows : grid) {
                  int upperHeight = rows * 18;

                  guiGraphics.blit(
                      CURIO_INVENTORY, i + xOffset, yOffset + 7, 7, 7, 18, upperHeight);
                  xOffset += 18;
                }
                RenderSystem.enableBlend();

                for (Slot slot : this.menu.slots) {

                  if (slot instanceof CurioSlot curioSlot && curioSlot.isCosmetic()) {
                    guiGraphics.blit(
                        CURIO_INVENTORY,
                        slot.x + this.getGuiLeft() - 1,
                        slot.y + this.getGuiTop() - 1,
                        32,
                        50,
                        18,
                        18);
                  }
                }
                RenderSystem.disableBlend();
              });
    }
  }

  @Override
  protected void renderSlot(@Nonnull GuiGraphics guiGraphics, Slot slot) {
    int i = slot.x;
    int j = slot.y;
    ItemStack itemstack = slot.getItem();

    if (slot instanceof CurioSlot curioSlot) {
      itemstack =
          curioSlot.getSlotExtension().getDisplayStack(curioSlot.getSlotContext(), itemstack);
    }
    boolean flag = false;
    boolean flag1 =
        slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
    ItemStack itemstack1 = this.menu.getCarried();
    String s = null;

    if (slot == this.clickedSlot
        && !this.draggingItem.isEmpty()
        && this.isSplittingStack
        && !itemstack.isEmpty()) {
      itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
    } else if (this.isQuickCrafting
        && this.quickCraftSlots.contains(slot)
        && !itemstack1.isEmpty()) {

      if (this.quickCraftSlots.size() == 1) {
        return;
      }

      if (AbstractContainerMenu.canItemQuickReplace(slot, itemstack1, true)
          && this.menu.canDragTo(slot)) {
        flag = true;
        int k = Math.min(itemstack1.getMaxStackSize(), slot.getMaxStackSize(itemstack1));
        int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
        int i1 =
            AbstractContainerMenu.getQuickCraftPlaceCount(
                this.quickCraftSlots, this.quickCraftingType, itemstack1)
                + l;

        if (i1 > k) {
          i1 = k;
          s = ChatFormatting.YELLOW.toString() + k;
        }
        itemstack = itemstack1.copyWithCount(i1);
      } else {
        this.quickCraftSlots.remove(slot);
        this.recalculateQuickCraftRemaining();
      }
    }
    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);

    if (itemstack.isEmpty() && slot.isActive() && this.minecraft != null) {
      Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();

      if (pair != null) {
        TextureAtlasSprite textureatlassprite =
            this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
        guiGraphics.blit(i, j, 0, 16, 16, textureatlassprite);
        flag1 = true;
      }
    }

    if (!flag1) {

      if (flag) {
        guiGraphics.fill(i, j, i + 16, j + 16, -2130706433);
      }
      this.renderSlotContents(guiGraphics, itemstack, slot, s);
    }
    guiGraphics.pose().popPose();
  }

  /**
   * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth,
   * rectHeight, pointX, pointY
   */
  @Override
  protected boolean isHovering(
      int rectX, int rectY, int rectWidth, int rectHeight, double pointX, double pointY) {

    if (this.isRenderButtonHovered) {
      return false;
    }
    return (!this.widthTooNarrow || !this.recipeBookGui.isVisible())
        && super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  /**
   * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
   */
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

    if (this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
      return true;
    }
    return this.widthTooNarrow && this.recipeBookGui.isVisible()
        || super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseReleased(double mouseReleased1, double mouseReleased3, int mouseReleased5) {

    if (this.buttonClicked) {
      this.buttonClicked = false;
      return true;
    } else {
      return super.mouseReleased(mouseReleased1, mouseReleased3, mouseReleased5);
    }
  }

  private static int scrollCooldown = 0;

  @Override
  public boolean mouseScrolled(
      double p_94686_, double p_94687_, double p_94688_, double p_294830_) {

    if (this.menu.totalPages > 1
        && p_94686_ < this.getGuiLeft()
        && p_94686_ > this.getGuiLeft() - this.panelWidth
        && p_94687_ > this.getGuiTop()
        && p_94687_ < this.getGuiTop() + this.imageHeight
        && scrollCooldown <= 0) {
      PacketDistributor.sendToServer(new CPacketPage(this.getMenu().containerId, p_294830_ == -1));
      scrollCooldown = 2;
    }
    return super.mouseScrolled(p_94686_, p_94687_, p_94688_, p_294830_);
  }

  @Override
  protected boolean hasClickedOutside(
      double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
    boolean flag =
        mouseX < guiLeftIn
            || mouseY < guiTopIn
            || mouseX >= guiLeftIn + this.imageWidth
            || mouseY >= guiTopIn + this.imageHeight;
    return this.recipeBookGui.hasClickedOutside(
        mouseX,
        mouseY,
        this.leftPos,
        this.topPos,
        this.imageWidth,
        this.imageHeight,
        mouseButton)
        && flag;
  }

  @Override
  protected void slotClicked(
      @Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
    super.slotClicked(slotIn, slotId, mouseButton, type);
    this.recipeBookGui.slotClicked(slotIn);
  }

  @Override
  public void recipesUpdated() {
    this.recipeBookGui.recipesUpdated();
  }

  @Nonnull
  @Override
  public RecipeBookComponent getRecipeBookComponent() {
    return this.recipeBookGui;
  }
}
