#loader crafttweaker
// Curios supports ContentTweaker. You can add your custom curio by ZenCode.
// This script sets function and available slot type of curios.
import mods.curios.contenttweaker.CoTItemCurio;
import crafttweaker.api.player.MCPlayerEntity;
import crafttweaker.api.data.MapData;

// Marking Items with Curio Types
// Run `/ct dump curiosslottype` command to get all available curio types
<curiosslottype:head>.asTag().add(<item:contenttweaker:custom_curio>);

// Using ContentTweaker Advanced Item Bracket Handler. Then cast it to CoTItemCurio class and set what happened when
// players equip, uneqip, are wearing the curio.
(<advanceditem:custom_curio> as CoTItemCurio).setOnEquipped((context, stack) => {
    print("equipped");
    if (context.wearer is MCPlayerEntity) {
        val player = context.wearer as MCPlayerEntity;
        for type, stacks in player.curiosItemHandler.curios {
            print(type.identifier);
            print((stacks as MapData).asString());
        }
    }
});
