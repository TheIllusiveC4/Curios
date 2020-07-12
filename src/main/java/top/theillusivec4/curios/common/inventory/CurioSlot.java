package top.theillusivec4.curios.common.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CurioSlot extends Slot {

  protected final String identifier;
  protected final PlayerEntity player;
  protected final int index;

  private DefaultedList<Boolean> renderStatuses;

  public CurioSlot(PlayerEntity player, IDynamicStackHandler handler, int index, String identifier,
      int xPosition, int yPosition, DefaultedList<Boolean> renders) {
    super(handler, index, xPosition, yPosition);
    this.identifier = identifier;
    this.index = index;
    this.renderStatuses = renders;
    this.player = player;
  }

  @Environment(EnvType.CLIENT)
  @Override
  public Pair<Identifier, Identifier> getBackgroundSprite() {
    return new Pair<>(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
        player.getEntityWorld().isClient() ? CuriosApi.getIconHelper().getIcon(identifier)
            : new Identifier(CuriosApi.MODID, "item/empty_curio_slot"));
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public int getIndex() { return this.index; }

  public boolean getRenderStatus() {
    return this.renderStatuses.get(this.index);
  }

  @Environment(EnvType.CLIENT)
  public String getSlotName() {
    return I18n.translate("curios.identifier." + identifier);
  }

  @Override
  public boolean canInsert(ItemStack stack) {
    return hasValidTag(CuriosApi.getCuriosHelper().getCurioTags(stack.getItem())) && CuriosApi
        .getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(identifier, player))
        .orElse(true) && super.canInsert(stack);
  }

  protected boolean hasValidTag(Set<String> tags) {
    return tags.contains(identifier) || tags.contains("curio");
  }

  @Override
  public boolean canTakeItems(PlayerEntity playerIn) {
    ItemStack stack = this.getStack();
    return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
        && CuriosApi.getCuriosHelper().getCurio(stack)
        .map(curio -> curio.canUnequip(identifier, playerIn)).orElse(true) && super
        .canTakeItems(playerIn);
  }
}
