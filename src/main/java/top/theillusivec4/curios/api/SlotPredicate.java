package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;

public class SlotPredicate {

  public static final SlotPredicate ANY = new SlotPredicate();

  private final Set<String> identifiers;
  private final MinMaxBounds.Ints indices;

  public SlotPredicate() {
    this.identifiers = new HashSet<>();
    this.indices = MinMaxBounds.Ints.ANY;
  }

  public SlotPredicate(Set<String> identifiers, MinMaxBounds.Ints indices) {
    this.identifiers = identifiers;
    this.indices = indices;
  }

  public boolean matches(SlotContext slotContext) {

    if (this == ANY) {
      return true;
    } else if (!this.identifiers.contains(slotContext.identifier())) {
      return false;
    } else {
      return this.indices.matches(slotContext.index());
    }
  }

  public static SlotPredicate fromJson(@Nullable JsonElement pJson) {

    if (pJson != null && !pJson.isJsonNull()) {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "curios:slot");
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(jsonobject.get("index"));

      Set<String> set;
      JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "slots", new JsonArray());
      ImmutableSet.Builder<String> builder = ImmutableSet.builder();

      for (JsonElement jsonelement : jsonarray) {
        builder.add(jsonelement.getAsString());
      }
      set = builder.build();
      return new SlotPredicate(set, minmaxbounds$ints);
    } else {
      return ANY;
    }
  }

  public JsonElement serializeToJson() {

    if (this == ANY) {
      return JsonNull.INSTANCE;
    } else {
      JsonObject jsonobject = new JsonObject();
      JsonArray jsonarray = new JsonArray();

      for (String id : this.identifiers) {
        jsonarray.add(id);
      }
      jsonobject.add("slots", jsonarray);
      jsonobject.add("index", this.indices.serializeToJson());
      return jsonobject;
    }
  }

  public static class Builder {

    private Set<String> identifiers = new HashSet<>();
    private MinMaxBounds.Ints indices = MinMaxBounds.Ints.ANY;

    private Builder() {
    }

    public static SlotPredicate.Builder slot() {
      return new SlotPredicate.Builder();
    }

    public SlotPredicate.Builder of(String... identifiers) {
      this.identifiers = Stream.of(identifiers).collect(ImmutableSet.toImmutableSet());
      return this;
    }

    public SlotPredicate.Builder withIndex(MinMaxBounds.Ints index) {
      this.indices = index;
      return this;
    }

    public SlotPredicate build() {
      return new SlotPredicate(this.identifiers, this.indices);
    }
  }
}
