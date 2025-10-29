package top.theillusivec4.curios.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public final class CuriosNbtIO {
	private CuriosNbtIO() {}

	public static CompoundTag writeHandler(HolderLookup.Provider provider, IDynamicStackHandler handler) {
		if (handler instanceof ItemStackHandler ish) {
			TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
			ish.serialize(out);
			return out.buildResult();
		}
		// Fallback for any non-ISH impls (none today)
		@SuppressWarnings("deprecation")
		CompoundTag legacy = handler.serializeNBT(provider);
		return legacy;
	}

	@SuppressWarnings("deprecation")
	public static void readHandler(HolderLookup.Provider provider, IDynamicStackHandler handler, CompoundTag tag) {
		if (handler instanceof ItemStackHandler ish) {
			ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, provider, tag == null ? new CompoundTag() : tag);
			ish.deserialize(in);
			return;
		}

		handler.deserializeNBT(provider, tag);
	}
}
