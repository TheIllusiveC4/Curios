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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.CuriosClientConfig.Client;
import top.theillusivec4.curios.client.CuriosClientConfig.Client.ButtonCorner;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketToggleRender;

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

  private CuriosButton buttonCurios;
  private boolean isScrolling;
  private boolean buttonClicked;
  private boolean isRenderButtonHovered;

  public CuriosScreen(CuriosContainer curiosContainer, PlayerInventory playerInventory,
      ITextComponent title) {
    super(curiosContainer, playerInventory, title);
    this.field_230711_n_ = true;
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
  public void func_231160_c_() {
    super.func_231160_c_();

    if (this.field_230706_i_ != null) {

      if (this.field_230706_i_.player != null) {
        hasScrollBar = CuriosApi.getCuriosHelper().getCuriosHandler(this.field_230706_i_.player)
            .map(handler -> handler.getSlots() > 8).orElse(false);

        if (hasScrollBar) {
          this.container.scrollTo(currentScroll);
        }
      }
      this.widthTooNarrow = this.field_230708_k_ < (hasScrollBar ? 461 : 491);
      this.recipeBookGui.init(this.field_230708_k_, this.field_230709_l_, this.field_230706_i_,
          this.widthTooNarrow, this.container);
      this.updateScreenPosition();
      this.field_230705_e_.add(this.recipeBookGui);
      this.setFocusedDefault(this.recipeBookGui);
      Tuple<Integer, Integer> offsets = getButtonOffset(false);
      this.buttonCurios = new CuriosButton(this, this.getGuiLeft() + offsets.getA(),
          this.field_230709_l_ / 2 + offsets.getB(), 14, 14, 50, 0, 14, CURIO_INVENTORY);
      this.func_230480_a_(this.buttonCurios);

      if (!this.playerInventory.player.isCreative()) {
        this.func_230480_a_(
            new ImageButton(this.guiLeft + 104, this.field_230709_l_ / 2 - 22, 20, 18, 0, 0, 19,
                RECIPE_BUTTON_TEXTURE, (button) -> {
              this.recipeBookGui.initSearchBar(this.widthTooNarrow);
              this.recipeBookGui.toggleVisibility();
              this.updateScreenPosition();
              ((ImageButton) button).setPosition(this.guiLeft + 104, this.field_230709_l_ / 2 - 22);
              this.buttonCurios.setPosition(this.guiLeft + offsets.getA(),
                  this.field_230709_l_ / 2 + offsets.getB());
            }));
      }
      this.updateRenderButtons();
    }
  }

  public void updateRenderButtons() {
    this.field_230710_m_.removeIf(widget -> widget instanceof RenderButton);
    int yOffset = 9;

    for (Slot inventorySlot : this.container.inventorySlots) {

      if (inventorySlot instanceof CurioSlot && !(inventorySlot instanceof CosmeticCurioSlot)) {
        this.func_230480_a_(
            new RenderButton((CurioSlot) inventorySlot, this.guiLeft - 8, this.guiTop + yOffset, 8,
                8, 75, 0, 8, CURIO_INVENTORY, (button) -> NetworkHandler.INSTANCE
                .send(PacketDistributor.SERVER.noArg(),
                    new CPacketToggleRender(((CurioSlot) inventorySlot).getIdentifier(),
                        inventorySlot.getSlotIndex()))));
        yOffset += 18;
      }
    }
  }

  private void updateScreenPosition() {
    int i;

    if (this.recipeBookGui.isVisible() && !this.widthTooNarrow) {
      i = 177 + (this.field_230708_k_ - this.xSize - (hasScrollBar ? 118 : 148)) / 2;
    } else {
      i = (this.field_230708_k_ - this.xSize) / 2;
    }
    this.guiLeft = i;
  }

  @Override
  public void func_231023_e_() {
    super.func_231023_e_();
    this.recipeBookGui.tick();
  }

  private boolean inScrollBar(double mouseX, double mouseY) {
    int i = this.guiLeft;
    int j = this.guiTop;
    int k = i - 34;
    int l = j + 12;
    int i1 = k + 14;
    int j1 = l + 139;

    if (this.container.hasCosmeticColumn()) {
      i1 -= 19;
      k -= 19;
    }
    return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) i1
        && mouseY < (double) j1;
  }

  @Override
  public void func_230430_a_(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY,
      float partialTicks) {
    this.func_230446_a_(matrixStack);

    if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
      this.func_230450_a_(matrixStack, partialTicks, mouseX, mouseY);
      this.recipeBookGui.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
    } else {
      this.recipeBookGui.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
      super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
      this.recipeBookGui.func_230477_a_(matrixStack, this.guiLeft, this.guiTop, true, partialTicks);

      boolean isButtonHovered = false;

      for (Widget button : this.field_230710_m_) {

        if (button instanceof RenderButton) {
          ((RenderButton) button).renderButtonOverlay(matrixStack, mouseX, mouseY, partialTicks);

          if (button.func_230449_g_()) {
            isButtonHovered = true;
          }
        }
      }
      this.isRenderButtonHovered = isButtonHovered;
      ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;

      if (!this.isRenderButtonHovered && clientPlayer != null && clientPlayer.inventory
          .getItemStack().isEmpty() && this.getSlotUnderMouse() != null) {
        Slot slot = this.getSlotUnderMouse();

        if (slot instanceof CurioSlot && !slot.getHasStack()) {
          CurioSlot slotCurio = (CurioSlot) slot;
          this.func_238652_a_(matrixStack, new StringTextComponent(slotCurio.getSlotName()), mouseX,
              mouseY);
        }
      }
    }
    this.func_230459_a_(matrixStack, mouseX, mouseY);
  }

  @Override
  protected void func_230459_a_(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY) {
    Minecraft mc = this.field_230706_i_;

    if (mc != null) {
      ClientPlayerEntity clientPlayer = mc.player;

      if (clientPlayer != null && clientPlayer.inventory.getItemStack().isEmpty()) {

        if (this.isRenderButtonHovered) {
          this.func_238652_a_(matrixStack, new TranslationTextComponent("gui.curios.toggle"),
              mouseX, mouseY);
        } else if (this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {
          this.func_230457_a_(matrixStack, this.hoveredSlot.getStack(), mouseX, mouseY);
        }
      }
    }
  }

  @Override
  public boolean func_231046_a_(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {

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
      return super.func_231044_a_(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
  }

  @Override
  protected void func_230451_b_(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY) {

    if (this.field_230706_i_ != null && this.field_230706_i_.player != null) {
      this.field_230706_i_.fontRenderer
          .func_238405_a_(matrixStack, I18n.format("container.crafting"), 97, 8, 4210752);
    }
  }

  /**
   * Draws the background layer of this container (behind the item).
   */
  @SuppressWarnings("deprecation")
  @Override
  protected void func_230450_a_(@Nonnull MatrixStack matrixStack, float partialTicks, int mouseX,
      int mouseY) {

    if (this.field_230706_i_ != null && this.field_230706_i_.player != null) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_230706_i_.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
      int i = this.guiLeft;
      int j = this.guiTop;
      this.func_238474_b_(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
      InventoryScreen.drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - mouseX,
          (float) (j + 75 - 50) - mouseY, this.field_230706_i_.player);
      CuriosApi.getCuriosHelper().getCuriosHandler(this.field_230706_i_.player)
          .ifPresent(handler -> {
            int slotCount = handler.getSlots();
            int upperHeight = 7 + slotCount * 18;
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.getMinecraft().getTextureManager().bindTexture(CURIO_INVENTORY);
            int xTexOffset = 0;
            int width = 27;
            int xOffset = -26;

            if (this.container.hasCosmeticColumn()) {
              xTexOffset = 92;
              width = 46;
              xOffset -= 19;
            }
            this.func_238474_b_(matrixStack, i + xOffset, j + 4, xTexOffset, 0, width, upperHeight);

            if (slotCount <= 8) {
              this.func_238474_b_(matrixStack, i + xOffset, j + 4 + upperHeight, xTexOffset, 151,
                  width, 7);
            } else {
              this.func_238474_b_(matrixStack, i + xOffset - 16, j + 4, 27, 0, 23, 158);
              this.getMinecraft().getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
              this.func_238474_b_(matrixStack, i + xOffset - 8,
                  j + 12 + (int) (127f * currentScroll), 232, 0, 12, 15);
            }

            for (Slot slot : this.container.inventorySlots) {

              if (slot instanceof CosmeticCurioSlot) {
                int x = this.guiLeft + slot.xPos - 1;
                int y = this.guiTop + slot.yPos - 1;
                this.getMinecraft().getTextureManager().bindTexture(CURIO_INVENTORY);
                this.func_238474_b_(matrixStack, x, y, 138, 0, 18, 18);
              }
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

    if (isRenderButtonHovered) {
      return false;
    }
    return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super
        .isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  /**
   * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
   */
  @Override
  public boolean func_231044_a_(double mouseX, double mouseY, int mouseButton) {

    if (this.recipeBookGui.func_231044_a_(mouseX, mouseY, mouseButton)) {
      return true;
    } else if (this.inScrollBar(mouseX, mouseY)) {
      this.isScrolling = this.needsScrollBars();
      return true;
    }
    return this.widthTooNarrow && this.recipeBookGui.isVisible() || super
        .func_231044_a_(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean func_231048_c_(double mouseReleased1, double mouseReleased3, int mouseReleased5) {

    if (mouseReleased5 == 0) {
      this.isScrolling = false;
    }

    if (this.buttonClicked) {
      this.buttonClicked = false;
      return true;
    } else {
      return super.func_231048_c_(mouseReleased1, mouseReleased3, mouseReleased5);
    }
  }

  @Override
  public boolean func_231045_a_(double pMouseDragged1, double pMouseDragged3, int pMouseDragged5,
      double pMouseDragged6, double pMouseDragged8) {

    if (this.isScrolling) {
      int i = this.guiTop + 8;
      int j = i + 148;
      currentScroll = ((float) pMouseDragged3 - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
      currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
      this.container.scrollTo(currentScroll);
      return true;
    } else {
      return super.func_231045_a_(pMouseDragged1, pMouseDragged3, pMouseDragged5, pMouseDragged6,
          pMouseDragged8);
    }
  }

  @Override
  public boolean func_231043_a_(double pMouseScrolled1, double pMouseScrolled3,
      double pMouseScrolled5) {

    if (!this.needsScrollBars()) {
      return false;
    } else {
      int i = (this.container).curiosHandler.map(ICuriosItemHandler::getSlots).orElse(1);
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
  protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton,
      @Nonnull ClickType type) {
    super.handleMouseClick(slotIn, slotId, mouseButton, type);
    this.recipeBookGui.slotClicked(slotIn);
  }

  @Override
  public void recipesUpdated() {
    this.recipeBookGui.recipesUpdated();
  }

  @Override
  public void func_231164_f_() {
    this.recipeBookGui.removed();
    super.func_231164_f_();
  }

  @Nonnull
  @Override
  public RecipeBookGui getRecipeGui() {
    return this.recipeBookGui;
  }
}
