package top.theillusivec4.curiostest.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curiostest.CuriosTest;

public class CuriosLayerDefinitions {

  public static final ModelLayerLocation CROWN =
      new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "crown"),
          "crown");
  public static final ModelLayerLocation KNUCKLES =
      new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "knuckles"),
          "knuckles");
  public static final ModelLayerLocation AMULET =
      new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID, "amulet"),
          "amulet");

}
