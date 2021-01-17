package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackLoader.items
import ink.ptms.tiphareth.pack.PackLoader.loadItems
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.locale.logger.TLogger
import io.izzel.taboolib.module.inject.TSchedule
import ink.ptms.tiphareth.pack.PackObject
import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.dependency.Dependency
import java.util.Comparator

@Dependency(maven = "com.aliyuncs:oss:3.5.0", url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/aliyun-sdk-oss-3.5.0.jar")
object Tiphareth : Plugin() {

    @TInject(migrate = true)
    lateinit var conf: TConfig
        private set

    @TInject
    lateinit var logger: TLogger
        private set

    @TSchedule
    fun reloadPack() {
        items.clear()
        items.addAll(loadItems())
        items.sortBy { it.getPackName() }
        logger.info("Loaded " + items.size + " Custom Items.")
    }
}