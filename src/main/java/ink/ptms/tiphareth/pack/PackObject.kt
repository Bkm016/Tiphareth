package ink.ptms.tiphareth.pack

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import taboolib.common.io.newFile
import taboolib.library.xseries.XItemStack
import taboolib.module.configuration.SecuredFile
import java.io.File

class PackObject(val packFile: File, val packType: PackType) {

    val root = SecuredFile.loadConfiguration(packFile)

    val item = XItemStack.deserialize(root.getConfigurationSection("item"))!!

    val itemHide = root.getBoolean("item.hide")

    val model = root.getString("model")!!

    fun getPackName(): String {
        return packFile.name.replace(".yml", "")
    }

    fun getModelName(): String {
        return item.type.toString().toLowerCase()
    }

    fun getTextures(): List<String> {
        val textures = arrayListOf<String>()
        textures.add(getModelName())
        if (item.itemMeta is LeatherArmorMeta || item.itemMeta is PotionMeta) {
            textures.add(getModelName() + "_overlay")
        }
        return textures
    }

    fun getIconFile(): File {
        return File(packFile.parent, packFile.name.replace(".yml", ".png"))
    }

    fun getMetaFile(): File {
        return File(packFile.parent, packFile.name.replace(".yml", ".png") + ".mcmeta")
    }

    fun generateModel(folderModel: File) {
        newFile(folderModel, packFile.name.replace(".yml", ".json")).writeText(model)
    }

    fun generateTexture(folderTexture: File) {
        getIconFile().run {
            if (exists()) {
                copyTo(newFile(folderTexture, this.name))
            }
        }
        getMetaFile().run {
            if (exists()) {
                copyTo(newFile(folderTexture, this.name))
            }
        }
    }

    fun buildItem(): ItemStack {
        val item = item.clone()
        try {
            val meta = item.itemMeta!!
            meta.setCustomModelData(PackGenerator.generateCustomData(this))
            item.itemMeta = meta
        } catch (t: Throwable) {
            println(toString())
            t.printStackTrace()
        }
        return item
    }

    override fun toString(): String {
        return "PackObject(packFile=$packFile, packType=$packType, isHide=$itemHide, item=$item, model=$model)"
    }
}