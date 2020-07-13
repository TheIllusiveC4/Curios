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

package top.theillusivec4.curios.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.client.CuriosClientConfig.ButtonCorner;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.common.CuriosNetwork;

public class CuriosScreen extends HandledScreen<CuriosScreenHandler> implements RecipeBookProvider {

  public static final Identifier CURIO_INVENTORY = new Identifier(CuriosApi.MODID,
      "textures/gui/inventory.png");
  public static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier(
      "minecraft:textures/gui/recipe_button.png");
  private static final Identifier CREATIVE_INVENTORY_TABS = new Identifier(
      "textures/gui/container/creative_inventory/tabs.png");

  private static float currentScroll;

  private final RecipeBookWidget recipeBook = new RecipeBookWidget();

  public boolean hasScrollBar;
  public boolean isNarrow;

  private CuriosButton buttonCurios;
  private boolean isScrolling;
  private boolean buttonClicked;
  private boolean isRenderButtonHovered;

  public CuriosScreen(CuriosScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
    this.passEvents = true;
    this.titleX = 97;
  }

  public static Pair<Integer, Integer> getButtonOffset(boolean isCreative) {
    ButtonCorner corner = ButtonCorner.TOP_LEFT;
    int left = 0;
    int right = 0;

    if (isCreative) {
      left += corner.getCreativeXoffset();
      right += corner.getCreativeYoffset();
    } else {
      left += corner.getXoffset();
      right += corner.getYoffset();
    }
    return new Pair<>(left, right);
  }

  @Override
  protected void init() {
    super.init();

    if (this.client != null) {

      if (this.client.player != null) {
        hasScrollBar = CuriosApi.getCuriosHelper().getCuriosHandler(this.client.player)
            .map(handler -> handler.getSlots() > 8).orElse(false);

        if (hasScrollBar) {
          this.getScreenHandler().scrollToPosition(currentScroll);
        }
      }
      this.isNarrow = this.width < (hasScrollBar ? 461 : 491);
      this.recipeBook.initialize(this.width, this.height, this.client, this.isNarrow, this.handler);
      this.updateScreenPosition();
      this.children.add(this.recipeBook);
      this.setInitialFocus(this.recipeBook);
      Pair<Integer, Integer> offsets = getButtonOffset(false);
      this.buttonCurios = new CuriosButton(this, this.x + offsets.getLeft(),
          this.height / 2 + offsets.getRight(), 14, 14, 50, 0, 14, CURIO_INVENTORY);
      this.addButton(this.buttonCurios);

      if (!this.playerInventory.player.isCreative()) {

        this.addButton(
            new TexturedButtonWidget(this.x + 104, this.height / 2 - 22, 20, 18, 0, 0, 19,
                RECIPE_BUTTON_TEXTURE, (buttonWidget) -> {
              this.recipeBook.reset(this.isNarrow);
              this.recipeBook.toggleOpen();
              this.x = this.recipeBook
                  .findLeftEdge(this.isNarrow, this.width, this.backgroundWidth);
              ((TexturedButtonWidget) buttonWidget).setPos(this.x + 104, this.height / 2 - 22);
              this.buttonCurios
                  .setPos(this.x + offsets.getLeft(), this.height / 2 + offsets.getRight());
            }));
      }
      this.updateRenderButtons();
    }
  }

  public void updateRenderButtons() {
    this.buttons.removeIf(widget -> widget instanceof RenderToggleButton);
    int yOffset = 9;

    for (Slot inventorySlot : this.getScreenHandler().slots) {

      if (inventorySlot instanceof CurioSlot && !(inventorySlot instanceof CosmeticCurioSlot)) {
        this.addButton(
            new RenderToggleButton((CurioSlot) inventorySlot, this.x - 8, this.y + yOffset, 8, 8,
                75, 0, 8, CURIO_INVENTORY, (button) -> {
              int index = ((CurioSlot) inventorySlot).getIndex();
              String id = ((CurioSlot) inventorySlot).getIdentifier();
              PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
              packetByteBuf.writeInt(index);
              packetByteBuf.writeString(id, 25);
              ((CurioSlot) inventorySlot).toggleRenderStatus();
              ClientSidePacketRegistry.INSTANCE
                  .sendToServer(CuriosNetwork.TOGGLE_RENDER, packetByteBuf);
            }));
        yOffset += 18;
      }
    }
  }

  private void updateScreenPosition() {
    int i;

    if (this.recipeBook.isOpen() && !this.isNarrow) {
      i = 177 + (this.width - this.backgroundWidth - (hasScrollBar ? 118 : 148)) / 2;
    } else {
      i = (this.width - this.backgroundWidth) / 2;
    }
    this.x = i;
  }

  @Override
  public void tick() {
    super.tick();
    this.recipeBook.update();
  }

  private boolean inScrollBar(double mouseX, double mouseY) {
    int i = this.x;
    int j = this.y;
    int k = i - 34;
    int l = j + 12;
    int i1 = k + 14;
    int j1 = l + 139;

    if (this.getScreenHandler().hasCosmeticColumn()) {
      i1 -= 19;
      k -= 19;
    }
    return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) i1
        && mouseY < (double) j1;
  }

  @Override
  public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    this.renderBackground(matrices);

    if (this.recipeBook.isOpen() && this.isNarrow) {
      this.drawBackground(matrices, delta, mouseX, mouseY);
      this.recipeBook.render(matrices, mouseX, mouseY, delta);
    } else {
      this.recipeBook.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
      this.recipeBook.drawGhostSlots(matrices, this.x, this.y, false, delta);

      boolean isButtonHovered = false;

      for (AbstractButtonWidget button : this.buttons) {

        if (button instanceof RenderToggleButton) {
          ((RenderToggleButton) button).renderButtonOverlay(matrices, mouseX, mouseY, delta);

          if (button.isHovered()) {
            isButtonHovered = true;
            break;
          }
        }
      }
      this.isRenderButtonHovered = isButtonHovered;
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;

      if (!this.isRenderButtonHovered && clientPlayerEntity != null && clientPlayerEntity.inventory
          .getCursorStack().isEmpty() && this.focusedSlot != null) {
        Slot slot = this.focusedSlot;

        if (slot instanceof CurioSlot && !slot.hasStack()) {
          this.renderTooltip(matrices, new LiteralText(((CurioSlot) slot).getSlotName()), mouseX,
              mouseY);
        }
      }
    }
    this.drawMouseoverTooltip(matrices, mouseX, mouseY);
  }

  @Override
  protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {

    if (this.client != null && this.client.player != null && this.client.player.inventory
        .getCursorStack().isEmpty()) {

      if (this.isRenderButtonHovered) {
        this.renderTooltip(matrices, new TranslatableText("gui.curios.toggle"), x, y);
      } else if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
        this.renderTooltip(matrices, this.focusedSlot.getStack(), x, y);
      }
    }
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (this.recipeBook.isOpen() && this.isNarrow) {
      this.recipeBook.toggleOpen();
      this.updateScreenPosition();
      return true;
    } else if (KeyRegistry.openCurios.matchesKey(keyCode, scanCode)) {

      if (this.client != null && this.client.player != null) {
        this.client.player.closeScreen();
      }
      return true;
    } else {
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    if (this.client != null && this.client.player != null) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
      int i = this.x;
      int j = this.y;
      this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
      InventoryScreen
          .drawEntity(i + 51, j + 75, 30, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY,
              this.client.player);
      CuriosApi.getCuriosHelper().getCuriosHandler(this.client.player).ifPresent(handler -> {
        int slotCount = handler.getSlots();
        int upperHeight = 7 + slotCount * 18;
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.client.getTextureManager().bindTexture(CURIO_INVENTORY);
        int xTexOffset = 0;
        int width = 27;
        int xOffset = -26;

        if (this.getScreenHandler().hasCosmeticColumn()) {
          xTexOffset = 92;
          width = 46;
          xOffset -= 19;
        }
        this.drawTexture(matrices, i + xOffset, j + 4, xTexOffset, 0, width, upperHeight);

        if (slotCount <= 8) {
          this.drawTexture(matrices, i + xOffset, j + 4 + upperHeight, xTexOffset, 151, width, 7);
        } else {
          this.drawTexture(matrices, i + xOffset - 16, j + 4, 27, 0, 23, 158);
          this.client.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
          this.drawTexture(matrices, i + xOffset - 8, j + 12 + (int) (127f * currentScroll), 232, 0,
              12, 15);
        }

        for (Slot slot : this.getScreenHandler().slots) {

          if (slot instanceof CosmeticCurioSlot) {
            int x = this.x + slot.x - 1;
            int y = this.y + slot.y - 1;
            this.client.getTextureManager().bindTexture(CURIO_INVENTORY);
            this.drawTexture(matrices, x, y, 138, 0, 18, 18);
          }
        }
      });
    }
  }

  @Override
  protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    this.textRenderer.draw(matrices, this.title, (float) this.titleX, (float) this.titleY, 4210752);
  }

  @Override
  protected boolean isPointWithinBounds(int xPosition, int yPosition, int width, int height,
      double pointX, double pointY) {

    if (this.isRenderButtonHovered) {
      return false;
    }
    return (!this.isNarrow || !this.recipeBook.isOpen()) && super
        .isPointWithinBounds(xPosition, yPosition, width, height, pointX, pointY);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
      return true;
    } else if (this.inScrollBar(mouseX, mouseY)) {
      this.isScrolling = this.needsScrollBars();
      return true;
    }
    return (!this.isNarrow || !this.recipeBook.isOpen()) && super
        .mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == 0) {
      this.isScrolling = false;
    }

    if (this.buttonClicked) {
      this.buttonClicked = false;
      return true;
    } else {
      return super.mouseReleased(mouseX, mouseY, button);
    }
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX,
      double deltaY) {

    if (this.isScrolling) {
      int i = this.y + 8;
      int j = i + 148;
      currentScroll = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
      currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
      this.getScreenHandler().scrollToPosition(currentScroll);
      return true;
    } else {
      return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double amount) {

    if (!this.needsScrollBars()) {
      return false;
    } else {
      int i = this.getScreenHandler().getCuriosHandler().map(ICuriosItemHandler::getSlots)
          .orElse(1);
      currentScroll = (float) ((double) currentScroll - amount / (double) i);
      currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
      this.getScreenHandler().scrollToPosition(currentScroll);
      return true;
    }
  }

  private boolean needsScrollBars() {
    return this.getScreenHandler().canScroll();
  }

  @Override
  protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top,
      int button) {
    int offset = -20;

    if (hasScrollBar) {
      offset -= 20;
    }

    if (this.getScreenHandler().hasCosmeticColumn()) {
      offset -= 20;
    }
    boolean bl = mouseX < (double) (left + offset) || mouseY < (double) top || mouseX >= (double) (left
        + this.backgroundWidth) || mouseY >= (double) (top + this.backgroundHeight);
    return this.recipeBook
        .isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth,
            this.backgroundHeight, button) && bl;
  }

  @Override
  protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
    super.onMouseClick(slot, invSlot, clickData, actionType);
    this.recipeBook.slotClicked(slot);
  }

  @Override
  public void refreshRecipeBook() {
    this.recipeBook.refresh();
  }

  @Override
  public void removed() {
    this.recipeBook.close();
    super.removed();
  }

  @Override
  public RecipeBookWidget getRecipeBookWidget() {
    return this.recipeBook;
  }
}
