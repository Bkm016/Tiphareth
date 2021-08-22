package ink.ptms.tiphareth.pack

import com.google.common.collect.Lists
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File

object PackLoader {

    val items: MutableList<PackObject> = Lists.newArrayList()

    fun getByName(name: String): PackObject? {
        return items.firstOrNull { it.getPackName() == name }
    }

    fun loadItems(): List<PackObject> {
        if (File(getDataFolder(), "pack").exists()) {
            return loadItem(File(getDataFolder(), "pack/item"))
        }
        releaseResourceFile("pack/pack.mcmeta", true)
        releaseResourceFile("pack/pack.png", true)
        releaseResourceFile("pack/item/diamond_sword_1_1.png", true)
        releaseResourceFile("pack/item/diamond_sword_1_1.yml", true)
        return loadItems()
    }

    fun loadItem(file: File): List<PackObject> {
        return when {
            file.isDirectory -> {
                file.listFiles()?.flatMap { loadItem(it) }?.toList() ?: emptyList()
            }
            file.name.endsWith(".yml") -> {
                listOf(PackObject(file, PackType.ITEM))
            }
            else -> {
                emptyList()
            }
        }
    }
}