package top.theillusivec4.curios.common.slot;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.data.ISlotData;

public final class SlotType implements ISlotType {

  private final String id;
  private final int order;
  private final int size;
  private final boolean useNativeGui;
  private final boolean hasCosmetic;
  private final ResourceLocation icon;
  private final DropRule dropRule;
  private final boolean renderToggle;
  private final Set<ResourceLocation> validators;
  private final Set<EntityType<?>> entities;

  public SlotType(String id, int order, int size, boolean useNativeGui, boolean hasCosmetic,
                  ResourceLocation icon, DropRule dropRule, boolean renderToggle,
                  Set<ResourceLocation> validators, Set<EntityType<?>> entities) {
    this.id = id;
    this.order = order;
    this.size = size;
    this.useNativeGui = useNativeGui;
    this.hasCosmetic = hasCosmetic;
    this.icon = icon;
    this.dropRule = dropRule;
    this.renderToggle = renderToggle;
    this.validators = validators;
    this.entities = entities;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getIdentifier() {
    return this.getId();
  }

  @Override
  public ResourceLocation getIcon() {
    return this.icon;
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public boolean useNativeGui() {
    return this.useNativeGui;
  }

  @Override
  public boolean hasCosmetic() {
    return this.hasCosmetic;
  }

  @Override
  public boolean canToggleRendering() {
    return this.renderToggle;
  }

  @Override
  public DropRule getDropRule() {
    return this.dropRule;
  }

  @Override
  public Set<ResourceLocation> getValidators() {
    return ImmutableSet.copyOf(this.validators);
  }

  @Override
  public Set<EntityType<?>> getDefaultEntityTypes() {
    return ImmutableSet.copyOf(this.entities);
  }

  public static class Builder {

    private final String id;
    private Integer order = null;
    private Integer size = null;
    private int sizeMod = 0;
    private Boolean useNativeGui = null;
    private Boolean hasCosmetic = null;
    private Boolean renderToggle = null;
    private ResourceLocation icon = null;
    private DropRule dropRule = null;
    private Set<ResourceLocation> validators = null;
    private Set<EntityType<?>> entityTypes = null;

    public Builder(String id) {
      this.id = id;
    }

    public void apply(ISlotData.Entry entry, HolderLookup.Provider provider) {
      boolean replace = entry.replace();
      this.order = entry.order()
          .map(order -> replace || this.order == null ? order : Math.min(this.order, order))
          .orElse(this.order);
      String op = entry.operation().orElse("set").toLowerCase();
      int size = entry.size().orElse(1);

      if (op.equalsIgnoreCase("remove")) {
        this.sizeMod = replace ? -size : this.sizeMod - size;
      } else if (op.equalsIgnoreCase("add")) {
        this.sizeMod = replace ? size : this.sizeMod + size;
      } else {
        this.size = replace || this.size == null ? size : Math.max(this.size, size);

        if (replace) {
          this.sizeMod = 0;
        }
      }
      entry.useNativeGui().ifPresent(useNativeGui -> this.useNativeGui =
          replace || this.useNativeGui == null ? useNativeGui : this.useNativeGui && useNativeGui);
      entry.renderToggle().ifPresent(renderToggle -> this.renderToggle =
          replace || this.renderToggle == null ? renderToggle : this.renderToggle && renderToggle);
      entry.hasCosmetic().ifPresent(hasCosmetic -> this.hasCosmetic =
          replace || this.hasCosmetic == null ? hasCosmetic : this.hasCosmetic || hasCosmetic);
      this.icon = entry.icon().orElse(this.icon);
      this.dropRule = entry.dropRule().orElse(this.dropRule);

      if (this.validators == null) {
        this.validators = new HashSet<>();
      }
      entry.validators().ifPresentOrElse(list -> {
        if (replace) {
          this.validators.clear();
        }
        this.validators.addAll(Set.copyOf(list));
      }, () -> {

        if (!replace) {
          this.validators.add(CuriosResources.resource("tag"));
        }
      });

      if (this.entityTypes == null) {
        this.entityTypes = new HashSet<>();
      }
      entry.entities().ifPresentOrElse(list -> {
        if (replace) {
          this.entityTypes.clear();
        }
        HolderLookup.RegistryLookup<EntityType<?>> registry =
            provider.lookupOrThrow(Registries.ENTITY_TYPE);
        for (Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>> value : list) {
          List<EntityType<?>> entities = new ArrayList<>();
          value.ifRight(entity -> {
            registry.get(entity).ifPresent(val -> entities.add(val.value()));
          });
          value.ifLeft(entityTag -> {
            registry.get(entityTag).ifPresent(val -> {
              for (Holder<EntityType<?>> entityTypeHolder : val) {
                entities.add(entityTypeHolder.value());
              }
            });
          });
          this.entityTypes.addAll(entities);
        }
      }, () -> {

        if (!replace) {
          BuiltInRegistries.ENTITY_TYPE.get(CuriosTags.PLAYER_LIKE)
              .ifPresent(entityType -> {
                for (Holder<EntityType<?>> holder : entityType) {
                  this.entityTypes.add(holder.value());
                }
              });
        }
      });
    }

    public ISlotType build() {

      if (this.order == null) {
        this.order = 1000;
      }

      if (this.size == null) {
        this.size = 1;
      }

      if (this.icon == null) {
        this.icon = ISlotType.GENERIC_ICON;
      }
      this.size += this.sizeMod;
      this.size = Math.max(this.size, 0);

      if (this.dropRule == null) {
        this.dropRule = DropRule.DEFAULT;
      }

      if (this.useNativeGui == null) {
        this.useNativeGui = true;
      }

      if (this.hasCosmetic == null) {
        this.hasCosmetic = false;
      }

      if (this.renderToggle == null) {
        this.renderToggle = true;
      }

      if (this.validators == null) {
        this.validators = Set.of(CuriosResources.resource("tag"));
      }

      if (this.entityTypes == null) {
        this.entityTypes = new HashSet<>();
        BuiltInRegistries.ENTITY_TYPE.get(CuriosTags.PLAYER_LIKE)
            .ifPresent(entityType -> {
              for (Holder<EntityType<?>> holder : entityType) {
                this.entityTypes.add(holder.value());
              }
            });
      }
      return new SlotType(this.id, this.order, this.size, this.useNativeGui, this.hasCosmetic,
                          this.icon, this.dropRule, this.renderToggle, this.validators,
                          this.entityTypes);
    }
  }
}
