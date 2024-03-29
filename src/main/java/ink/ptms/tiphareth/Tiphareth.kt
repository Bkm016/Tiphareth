package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackLoader.items
import ink.ptms.tiphareth.pack.PackLoader.loadItems
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

//@RuntimeDependency("com.aliyuncs:oss:3.5.0", test = "com.aliyun.oss.OSS", repository = "http://repo.ptms.ink/repository/maven-releases")
object Tiphareth : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    override fun onActive() {
        reloadPack()
    }

    fun reloadPack() {
        items.clear()
        items.addAll(loadItems())
        items.sortBy { it.getPackName() }
        info("Loaded " + items.size + " Custom Items.")
    }
}