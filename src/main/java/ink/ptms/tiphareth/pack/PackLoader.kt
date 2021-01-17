package ink.ptms.tiphareth.pack

import com.google.common.collect.Lists
import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.util.Files
import java.io.File

object PackLoader {

    val items: MutableList<PackObject> = Lists.newArrayList()

    fun getByName(name: String): PackObject? = items.firstOrNull { it.getPackName() == name }

    fun loadItems(): List<PackObject> {
        if (File(Tiphareth.getPlugin().dataFolder, "pack").exists()) {
            return loadItem(Files.folder(Tiphareth.getPlugin().dataFolder, "pack/item"))
        }
        Tiphareth.getPlugin().saveResource("pack/pack.mcmeta", true)
        Tiphareth.getPlugin().saveResource("pack/pack.png", true)
        Tiphareth.getPlugin().saveResource("pack/item/diamond_sword_1_1.png", true)
        Tiphareth.getPlugin().saveResource("pack/item/diamond_sword_1_1.yml", true)
        return loadItems()
    }

    fun loadItem(file: File): List<PackObject> {
        if (file.isDirectory) {
            return file.listFiles()?.flatMap { loadItem(it) }?.toList() ?: emptyList()
        } else if (file.name.endsWith(".yml")) {
            return listOf(PackObject(file, PackType.ITEM))
        }
        return emptyList()
    }
}