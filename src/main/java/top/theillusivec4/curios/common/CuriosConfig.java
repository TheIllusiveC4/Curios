package top.theillusivec4.curios.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.common.CuriosConfig.CuriosSettings.CuriosSetting;

public class CuriosConfig {

  public static final ForgeConfigSpec SERVER_SPEC;
  public static final Server SERVER;
  public static List<CuriosSetting> curios = new ArrayList<>();

  static {
    final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
        .configure(Server::new);
    SERVER_SPEC = specPair.getRight();
    SERVER = specPair.getLeft();
  }

  public static void transformCurios(CommentedConfig configData) {
    SERVER.curiosSettings = new ObjectConverter().toObject(configData, CuriosSettings::new);
    curios = SERVER.curiosSettings.curiosSettings;
  }

  public static class Server {

    public CuriosSettings curiosSettings;

    public Server(ForgeConfigSpec.Builder builder) {
      builder.comment("List of curio slot type settings").define("curiosSettings", new ArrayList<>());
      builder.build();
    }
  }

  public static class CuriosSettings {

    public List<CuriosSetting> curiosSettings;

    public static class CuriosSetting {

      public String identifier;
      public String icon;
      public Integer priority;
      public Integer size;
      public Boolean locked;
      public Boolean visible;
      public Boolean hasCosmetic;
      public Boolean override;
    }
  }
}
