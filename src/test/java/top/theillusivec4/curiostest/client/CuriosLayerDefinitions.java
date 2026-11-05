package top.theillusivec4.curiostest.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import top.theillusivec4.curiostest.CuriosTest;

public class CuriosLayerDefinitions {

  public static final ModelLayerLocation CROWN =
      new ModelLayerLocation(Identifier.fromNamespaceAndPath(CuriosTest.MODID, "crown"),
          "crown");
  public static final ModelLayerLocation KNUCKLES =
      new ModelLayerLocation(Identifier.fromNamespaceAndPath(CuriosTest.MODID, "knuckles"),
          "knuckles");
  public static final ModelLayerLocation AMULET =
      new ModelLayerLocation(Identifier.fromNamespaceAndPath(CuriosTest.MODID, "amulet"),
          "amulet");

}
