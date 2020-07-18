package ink.ptms.tiphareth.pack

import io.izzel.taboolib.util.Files
import io.izzel.taboolib.util.item.Items
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import java.io.File

/**
 * @Author sky
 * @Since 2019-11-25 15:04
 */
class PackObject(val packFile: File, val packType: PackType) {

    var isHide: Boolean = false
        private set

    var item: ItemStack? = null
        private set

    var model: String? = null
        private set

    init {
        val conf = Files.loadYaml(packFile)
        isHide = conf.getBoolean("item.hide")
        item = Items.loadItem(conf.getConfigurationSection("item"))
        model = conf.getString("model")
    }

    fun getPackName(): String = packFile.name.replace(".yml", "")

    fun getModelName(): String = item!!.type.toString().toLowerCase()

    fun getTextures(): List<String> {
        val textures = arrayListOf<String>()
        textures.add(getModelName())
        if (item!!.itemMeta is LeatherArmorMeta || item!!.itemMeta is PotionMeta) {
            textures.add(getModelName() + "_overlay")
        }
        return textures
    }

    fun getIconFile(): File = File(packFile.parent, packFile.name.replace(".yml", ".png"))

    fun getMetaFile(): File = File(packFile.parent, packFile.name.replace(".yml", ".png") + ".mcmeta")

    fun generateModel(folderModel: File) {
        Files.toFile(model, Files.file(folderModel, packFile.name.replace(".yml", ".json")))
    }

    fun generateTexture(folderTexture: File) {
        getIconFile().run {
            if (this.exists()) {
                Files.copy(this, Files.file(folderTexture, this.name))
            }
        }
        getMetaFile().run {
            if (this.exists()) {
                Files.copy(this, Files.file(folderTexture, this.name))
            }
        }
    }

    fun buildItem(): ItemStack {
        val item = item!!.clone()
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
        return "PackObject(packFile=$packFile, packType=$packType, isHide=$isHide, item=$item, model=$model)"
    }


}