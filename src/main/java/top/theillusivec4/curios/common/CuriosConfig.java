package top.theillusivec4.curios.common;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.Curios;

import java.util.List;

public class CuriosConfig {

    private static final String CONFIG_PREFIX = "gui." + Curios.MODID + ".config.";

    public static class Common {

        public final ForgeConfigSpec.ConfigValue<List<String>> disabledCurios;
        public final ForgeConfigSpec.ConfigValue<List<String>> createCurios;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("common");

            disabledCurios = builder
                    .comment("List of curio types to disable by default")
                    .translation(CONFIG_PREFIX + "disabledCurios")
                    .define("disabledCurios", Lists.newArrayList());

            createCurios = builder
                    .comment("List of curio types to create")
                    .translation(CONFIG_PREFIX + "createCurios")
                    .define("createCurios", Lists.newArrayList());

            builder.pop();
        }
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue renderCurios;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client only settings, mostly things related to rendering")
                    .push("client");

            renderCurios = builder
                    .comment("Set to true to enable rendering curios")
                    .translation(CONFIG_PREFIX + "renderCurios")
                    .define("renderCurios", true);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;
    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }


    public static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }
}
