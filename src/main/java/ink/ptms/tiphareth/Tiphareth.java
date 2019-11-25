package ink.ptms.tiphareth;

import com.google.common.collect.Lists;
import ink.ptms.tiphareth.pack.PackLoader;
import ink.ptms.tiphareth.pack.PackObject;
import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.module.locale.logger.TLogger;

import java.util.List;

@Plugin.Version(5.11)
public final class Tiphareth extends Plugin {

    @TInject
    public static final TConfig CONF = null;
    @TInject
    public static final TLogger LOGGER = null;
    public static final List<PackObject> PACK_ITEMS = Lists.newArrayList();

    @TSchedule
    public void reloadPack() {
        PACK_ITEMS.clear();
        PACK_ITEMS.addAll(PackLoader.INSTANCE.loadItems());
        LOGGER.info("Loaded " + PACK_ITEMS.size() + " Custom Items.");
    }
}
