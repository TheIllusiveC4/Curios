package top.theillusivec4.curios.common.integration.contenttweaker;

import com.blamejared.contenttweaker.VanillaFactory;
import com.blamejared.contenttweaker.api.items.ItemTypeBuilder;
import com.blamejared.contenttweaker.items.ItemBuilder;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.curios.contenttweaker.ItemBuilderCurio")
@Document("mods/Curios/Contenttweaker/ItemBuilderCurio")
public class ItemBuilderCurio extends ItemTypeBuilder {
    public ItemBuilderCurio(ItemBuilder itemBuilder) {
        super(itemBuilder);
    }

    @Override
    public void build(ResourceLocation resourceLocation) {
        VanillaFactory.queueItemForRegistration(new CoTItemCurio(this.itemBuilder.getItemProperties(), resourceLocation));
    }
}
