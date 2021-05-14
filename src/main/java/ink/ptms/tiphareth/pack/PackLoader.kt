package ink.ptms.tiphareth.pack

import com.google.common.collect.Lists
import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.util.Files
import java.io.File

object PackLoader {

    val items: MutableList<PackObject> = Lists.newArrayList()

    fun getByName(name: String): PackObject? = items.firstOrNull { it.getPackName() == name }

    fun loadItems(): List<PackObject> {
        if (File(Tiphareth.plugin.dataFolder, "pack").exists()) {
            return loadItem(Files.folder(Tiphareth.plugin.dataFolder, "pack/item"))
        }
        Tiphareth.plugin.saveResource("pack/pack.mcmeta", true)
        Tiphareth.plugin.saveResource("pack/pack.png", true)
        Tiphareth.plugin.saveResource("pack/item/diamond_sword_1_1.png", true)
        Tiphareth.plugin.saveResource("pack/item/diamond_sword_1_1.yml", true)
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