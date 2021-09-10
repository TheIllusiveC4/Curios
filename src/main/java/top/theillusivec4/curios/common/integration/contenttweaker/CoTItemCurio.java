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

package top.theillusivec4.curios.common.integration.contenttweaker;

import com.blamejared.contenttweaker.actions.ActionSetFunction;
import com.blamejared.contenttweaker.items.types.advance.CoTItemAdvanced;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

import javax.annotation.Nullable;

@ZenRegister
@ZenCodeType.Name("mods.curios.contenttweaker.CoTItemCurio")
@Document("mods/Curios/ContentTweaker/CoTItemCurio")
public class CoTItemCurio extends CoTItemAdvanced {
    public CoTItemCurio(Properties properties, ResourceLocation location) {
        super(properties, location);
    }

    private final ZenCurio curio = new ZenCurio();

    /**
     * The function will be called every tick on both client and server while the item is equipped.
     * @param func an ICurioTick function
     * @return the CoTItemCurio, used for method chaining
     */
    @ZenCodeType.Method
    public CoTItemCurio setCurioTick(ICurioTick func) {
        ActionSetFunction.applyNewAction("curioTick", this, func, (item, function) -> item.curio.curioTick = function);
        return this;
    }

    /**
     * Sets what will happen when the curio is equipped. The second parameter of the function is
     * the previous {@link IItemStack} in the slot
     * @param func ion an IEquipCallback function
     * @return the CoTItemCurio, used for method chaining
     */
    @ZenCodeType.Method
    public CoTItemCurio setOnEquipped(IEquipCallback func) {
        ActionSetFunction.applyNewAction("onEquipped", this, func, (item, function) -> item.curio.onEquipped = function);
        return this;
    }

    /**
     * Sets what will happen when the curio is unequipped. The second parameter of the function is
     * the new {@link IItemStack} in the slot
     * @param func an IEquipCallback function
     * @return the CoTItemCurio, used for method chaining
     */
    @ZenCodeType.Method
    public CoTItemCurio setOnUnequipped(IEquipCallback func) {
        ActionSetFunction.applyNewAction("onUnequipped", this, func, (item, function) -> item.curio.onUnequipped = function);
        return this;
    }

    /**
     * Sets the checker that determined the item can be equipped.
     * @param func an IEquipChecker function
     * @return the CoTItemCurio, used for method chaining
     */
    @ZenCodeType.Method
    public CoTItemCurio setCanEquip(IEquipChecker func) {
        ActionSetFunction.applyNewAction("canEquip", this, func, (item, function) -> item.curio.canEquip = function);
        return this;
    }

    /**
     * Sets the checker that determined the item can be unequipped.
     * @param func an IEquipChecker function
     * @return the CoTItemCurio, used for method chaining
     */
    @ZenCodeType.Method
    public CoTItemCurio setCanUnequip(IEquipChecker func) {
        ActionSetFunction.applyNewAction("canUnequip", this, func, (item, function) -> item.curio.canUnequip = function);
        return this;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return CurioItemCapability.createProvider(curio);
    }

    public static class ZenCurio implements ICurio {
        private ICurioTick curioTick;
        private IEquipCallback onEquipped;
        private IEquipCallback onUnequipped;
        private IEquipChecker canEquip;
        private IEquipChecker canUnequip;

        @Override
        public void curioTick(String identifier, int index, LivingEntity livingEntity) {
            if (curioTick != null) {
                curioTick.tick(identifier, index, livingEntity);
            }
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack prevStack) {
            if (onEquipped != null) {
                onEquipped.apply(slotContext, new MCItemStack(prevStack));
            }
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack) {
            if (onUnequipped != null) {
                onUnequipped.apply(slotContext, new MCItemStack(newStack));
            }
        }

        @Override
        public boolean canEquip(String identifier, LivingEntity livingEntity) {
            if (canEquip != null) {
                return canEquip.check(identifier, livingEntity);
            }
            return true;
        }

        @Override
        public boolean canUnequip(String identifier, LivingEntity livingEntity) {
            if (canUnequip != null) {
                return canUnequip.check(identifier, livingEntity);
            }
            return true;
        }
    }
}
