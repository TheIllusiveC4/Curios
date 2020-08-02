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

package top.theillusivec4.curios.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.api.inventory.SlotCurio;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.CuriosConfig.Client;
import top.theillusivec4.curios.common.CuriosConfig.Client.ButtonCorner;
import top.theillusivec4.curios.common.inventory.CuriosContainer;

public class CuriosScreen extends ContainerScreen<CuriosContainer> implements IRecipeShownListener {

  static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(Curios.MODID,
      "textures/gui/inventory.png");
  static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation(
      "minecraft:textures/gui/recipe_button.png");

  private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation(
      "textures/gui/container/creative_inventory/tabs.png");

  private static float currentScroll;

  private final RecipeBookGui recipeBookGui = new RecipeBookGui();

  public boolean hasScrollBar;
  public boolean widthTooNarrow;

  private GuiButtonCurios buttonCurios;
  private boolean isScrolling;
  private boolean buttonClicked;

  public CuriosScreen(CuriosContainer curiosContainer, PlayerInventory playerInventory,
      ITextComponent title) {
    super(curiosContainer, playerInventory, title);
    this.passEvents = true;
  }

  @Override
  public void init() {
    super.init();

    if (this.minecraft != null) {

      if (this.minecraft.player != null) {
        hasScrollBar = CuriosAPI.getCuriosHandler(this.minecraft.player)
            .map(handler -> handler.getVisibleSlots() > 8).orElse(false);

        if (hasScrollBar) {
          this.container.scrollTo(currentScroll);
        }
      }
      this.widthTooNarrow = this.width < (hasScrollBar ? 461 : 491);
      this.recipeBookGui
          .init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.container);
      this.updateScreenPosition();
      this.children.add(this.recipeBookGui);
      this.setFocusedDefault(this.recipeBookGui);
      Tuple<Integer, Integer> offsets = getButtonOffset(false);
      this.buttonCurios = new GuiButtonCurios(this, this.getGuiLeft() + offsets.getA(),
          this.height / 2 + offsets.getB(), 14, 14, 50, 0, 14, CURIO_INVENTORY);
      this.addButton(this.buttonCurios);

      if (!this.playerInventory.player.isCreative()) {
        this.addButton(new ImageButton(this.guiLeft + 104, this.height / 2 - 22, 20, 18, 0, 0, 19,
            RECIPE_BUTTON_TEXTURE, (button) -> {
          this.recipeBookGui.initSearchBar(this.widthTooNarrow);
          this.recipeBookGui.toggleVisibility();
          this.updateScreenPosition();
          ((ImageButton) button).setPosition(this.guiLeft + 104, this.height / 2 - 22);
          this.buttonCurios
              .setPosition(this.guiLeft + offsets.getA(), this.height / 2 + offsets.getB());
        }));
      }
    }
  }

  private void updateScreenPosition() {
    int i;

    if (this.recipeBookGui.isVisible() && !this.widthTooNarrow) {
      i = 177 + (this.width - this.xSize - (hasScrollBar ? 118 : 148)) / 2;
    } else {
      i = (this.width - this.xSize) / 2;
    }
    this.guiLeft = i;
  }

  @Override
  public void tick() {
    super.tick();
    this.recipeBookGui.tick();
  }

  public static Tuple<Integer, Integer> getButtonOffset(boolean isCreative) {
    Client client = CuriosConfig.CLIENT;
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

  private boolean inScrollBar(double mouseX, double mouseY) {
    int i = this.guiLeft;
    int j = this.guiTop;
    int k = i - 34;
    int l = j + 12;
    int i1 = k + 14;
    int j1 = l + 139;
    return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) i1
        && mouseY < (double) j1;
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    this.renderBackground();

    if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
      this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
      this.recipeBookGui.render(mouseX, mouseY, partialTicks);
    } else {
      this.recipeBookGui.render(mouseX, mouseY, partialTicks);
      super.render(mouseX, mouseY, partialTicks);
      this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
    }
    this.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {

    if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
      this.recipeBookGui.toggleVisibility();
      this.updateScreenPosition();
      return true;
    } else if (KeyRegistry.openCurios
        .isActiveAndMatches(InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_))) {
      ClientPlayerEntity playerEntity = this.getMinecraft().player;

      if (playerEntity != null) {
        playerEntity.closeScreen();
      }
      return true;
    } else {
      return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

    if (this.minecraft != null && this.minecraft.player != null) {
      this.minecraft.fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);

      if (this.minecraft.player.inventory.getItemStack().isEmpty()
          && this.getSlotUnderMouse() != null) {
        Slot slot = this.getSlotUnderMouse();
        if (slot instanceof SlotCurio && !slot.getHasStack()) {
          SlotCurio slotCurio = (SlotCurio) slot;
          this.renderTooltip(slotCurio.getSlotName(), mouseX - this.guiLeft, mouseY - this.guiTop);
        }
      }
    }
  }

  /**
   * Draws the background layer of this container (behind the item).
   */
  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    if (this.minecraft != null && this.minecraft.player != null) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
      int i = this.guiLeft;
      int j = this.guiTop;
      this.blit(i, j, 0, 0, this.xSize, this.ySize);
      InventoryScreen.drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - mouseX,
          (float) (j + 75 - 50) - mouseY, this.minecraft.player);
      CuriosAPI.getCuriosHandler(this.minecraft.player).ifPresent(handler -> {
        int slotCount = 0;

        for (CurioStackHandler stacks : handler.getCurioMap().values()) {

          if (!stacks.isHidden()) {
            slotCount += stacks.getSlots();
          }
        }
        int upperHeight = 7 + slotCount * 18;
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.getMinecraft().getTextureManager().bindTexture(CURIO_INVENTORY);
        this.blit(i - 26, j + 4, 0, 0, 27, upperHeight);

        if (slotCount <= 8) {
          this.blit(i - 26, j + 4 + upperHeight, 0, 151, 27, 7);
        } else {
          this.blit(i - 42, j + 4, 27, 0, 23, 158);
          this.getMinecraft().getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
          this.blit(i - 34, j + 12 + (int) (127f * currentScroll), 232, 0, 12, 15);
        }
      });
    }
  }

  /**
   * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth,
   * rectHeight, pointX, pointY
   */
  @Override
  protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight,
      double pointX, double pointY) {
    return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super
        .isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  /**
   * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
   */
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

    if (this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
      return true;
    } else if (this.inScrollBar(mouseX, mouseY)) {
      this.isScrolling = this.needsScrollBars();
      return true;
    }
    return this.widthTooNarrow && this.recipeBookGui.isVisible() || super
        .mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseReleased(double mouseReleased1, double mouseReleased3, int mouseReleased5) {

    if (mouseReleased5 == 0) {
      this.isScrolling = false;
    }

    if (this.buttonClicked) {
      this.buttonClicked = false;
      return true;
    } else {
      return super.mouseReleased(mouseReleased1, mouseReleased3, mouseReleased5);
    }
  }

  @Override
  public boolean mouseDragged(double pMouseDragged1, double pMouseDragged3, int pMouseDragged5,
      double pMouseDragged6, double pMouseDragged8) {

    if (this.isScrolling) {
      int i = this.guiTop + 8;
      int j = i + 148;
      currentScroll = ((float) pMouseDragged3 - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
      currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
      this.container.scrollTo(currentScroll);
      return true;
    } else {
      return super.mouseDragged(pMouseDragged1, pMouseDragged3, pMouseDragged5, pMouseDragged6,
          pMouseDragged8);
    }
  }

  @Override
  public boolean mouseScrolled(double pMouseScrolled1, double pMouseScrolled3,
      double pMouseScrolled5) {

    if (!this.needsScrollBars()) {
      return false;
    } else {
      int i = (this.container).curios.map(ICurioItemHandler::getVisibleSlots).orElse(1);
      currentScroll = (float) ((double) currentScroll - pMouseScrolled5 / (double) i);
      currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
      this.container.scrollTo(currentScroll);
      return true;
    }
  }

  private boolean needsScrollBars() {
    return this.container.canScroll();
  }

  protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn,
      int mouseButton) {
    boolean flag =
        mouseX < (double) guiLeftIn || mouseY < (double) guiTopIn || mouseX >= (double) (guiLeftIn
            + this.xSize) || mouseY >= (double) (guiTopIn + this.ySize);
    return this.recipeBookGui
        .func_195604_a(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize,
            mouseButton) && flag;
  }

  @Override
  protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
    super.handleMouseClick(slotIn, slotId, mouseButton, type);
    this.recipeBookGui.slotClicked(slotIn);
  }

  @Override
  public void recipesUpdated() {
    this.recipeBookGui.recipesUpdated();
  }

  @Override
  public void removed() {
    this.recipeBookGui.removed();
    super.removed();
  }

  @Nonnull
  @Override
  public RecipeBookGui getRecipeGui() {
    return this.recipeBookGui;
  }
}
