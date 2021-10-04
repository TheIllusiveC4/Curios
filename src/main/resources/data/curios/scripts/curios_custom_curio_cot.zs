#loader contenttweaker
// Curios supports ContentTweaker. You can add your custom curio by ZenCode.
// This script only registers items.
import mods.contenttweaker.item.ItemBuilder;
import mods.curios.contenttweaker.ItemBuilderCurio;

// Creates a simple custom curio. See curio_custom_curio_crt.zs for more information.
new ItemBuilder()
    .withMaxStackSize(1)
    .withType<ItemBuilderCurio>()
    .build("custom_curio");
