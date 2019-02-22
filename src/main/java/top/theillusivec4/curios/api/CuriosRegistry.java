package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.Curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CuriosRegistry {

    private static Map<String, CurioType> idToType = Maps.newHashMap();
    private static Map<String, Tag<Item>> idToTag = Maps.newHashMap();
    private static Map<Item, Set<String>> itemToTypes = Maps.newHashMap();

    static Map<String, ResourceLocation> icons = Maps.newHashMap();

    public static CurioType registerType(@Nonnull String identifier) {

        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be empty!");
        }
        return idToType.merge(identifier, new CurioType(identifier), (key, value) -> value);
    }

    @Nullable
    public static CurioType getType(String identifier) {
        return idToType.get(identifier);
    }

    public static ImmutableSet<String> getTypeIdentifiers() { return ImmutableSet.copyOf(idToType.keySet()); }

    public static Set<String> getCurioTags(Item item) {

        if (idToTag.isEmpty()) {
            refreshTags();
        }

        if (itemToTypes.containsKey(item)) {
            return itemToTypes.get(item);
        } else {
            Set<String> tags = Sets.newHashSet();

            for (String identifier : idToTag.keySet()) {

                if (idToTag.get(identifier).contains(item)) {
                    tags.add(identifier);
                }
            }
            itemToTypes.put(item, tags);
            return tags;
        }
    }

    public static Set<ResourceLocation> getResources() {
        Set<ResourceLocation> resources = Sets.newHashSet(icons.values());
        resources.add(new ResourceLocation("curios:item/empty_generic_slot"));
        return resources;
    }

    private static void refreshTags() {
        idToTag = ItemTags.getCollection().getTagMap().entrySet()
                .stream()
                .filter(map -> map.getKey().getNamespace().equals(Curios.MODID))
                .collect(Collectors.toMap(entry -> entry.getKey().getPath(), Map.Entry::getValue));
        itemToTypes.clear();
    }
}
