package ink.ptms.tiphareth;

import com.google.common.collect.Lists;
import ink.ptms.tiphareth.pack.*;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.dependency.TDependency;
import io.izzel.taboolib.module.dependency.TDependencyInjector;
import io.izzel.taboolib.module.dependency.TDependencyLoader;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.module.locale.logger.TLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Plugin.Version(5.11)
@Dependency(maven = "com.aliyuncs:oss:3.5.0", url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/aliyun-sdk-oss-3.5.0.jar")
public final class Tiphareth extends Plugin {

    @TInject
    public static final TConfig CONF = null;
    @TInject
    public static final TLogger LOGGER = null;

    @TSchedule
    public static void reloadPack() {
        PackLoader.INSTANCE.getItems().clear();
        PackLoader.INSTANCE.getItems().addAll(PackLoader.INSTANCE.loadItems());
        LOGGER.info("Loaded " + PackLoader.INSTANCE.getItems().size() + " Custom Items.");
    }
}
