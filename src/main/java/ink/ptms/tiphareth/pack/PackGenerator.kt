package ink.ptms.tiphareth.pack

import com.google.common.collect.Maps
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.apache.commons.lang.time.DateFormatUtils
import org.bukkit.Material
import taboolib.common.io.deepCopyTo
import taboolib.common.io.deepDelete
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.library.xseries.parseToMaterial
import taboolib.module.configuration.Local
import taboolib.module.configuration.SecuredFile
import taboolib.module.configuration.createLocal
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PackGenerator {

    val mapping = createLocal("data-mapping.yml")

    fun file(): File = newFile(getDataFolder(), "tiphareth-pack.zip", create = false)

    fun generate(packObject: List<PackObject>) {
        val folder = File(getDataFolder(), "tiphareth-pack")
        if (folder.exists()) {
            folder.deepDelete()
        }
        folder.mkdirs()
        // pack info
        newFile(getDataFolder(), "pack/pack.mcmeta").copyTo(newFile(folder, "pack.mcmeta"))
        newFile(getDataFolder(), "pack/pack.png").copyTo(newFile(folder, "pack.png"))
        // pack resources
        val resources = File(getDataFolder(), "pack/resources")
        resources.mkdirs()
        resources.deepCopyTo(File(folder, "assets"))
        // pack folder
        val folderModels = File(folder, "assets/minecraft/models").also { it.mkdirs() }
        val folderTextures = File(folder, "assets/minecraft/textures").also { it.mkdirs() }
        // placeholder
        val time = System.currentTimeMillis()
        newFile(folder, "pack.mcmeta").writeText(newFile(folder, "pack.mcmeta").readText().checkPlaceholder(time))
        File(folder, "assets/minecraft/lang").listFiles()?.forEach { file ->
            file.writeText(file.readText().checkPlaceholder(time))
        }
        // generate pack model
        val mapItem = Maps.newHashMap<String, PackModel>()
        packObject.forEach { pack ->
            val material = pack.item!!.type
            when (pack.packType) {
                PackType.ITEM -> {
                    mapItem.computeIfAbsent(pack.getModelName()) { PackModel(material, getParent(material), pack.getTextures()) }.packOverride.add(
                        PackOverride(
                            pack.getPackName(),
                            generateCustomData(pack)
                        )
                    )
                }
            }
        }
        mapItem.forEach { entry ->
            val json = JsonObject()
            json.addProperty("parent", entry.value.parent)
            val textures = JsonObject()
            entry.value.textures.forEachIndexed { index, texture ->
                if (entry.value.material.isBlock) {
                    textures.addProperty("layer$index", "block/$texture")
                } else {
                    textures.addProperty("layer$index", "item/$texture")
                }
            }
            json.add("textures", textures)
            val overrides = JsonArray()
            entry.value.packOverride.sortedBy { it.customData }.forEach { override ->
                val overridesJson = JsonObject()
                val predicateJson = JsonObject()
                // 鱼竿
                if (entry.value.material == Material.FISHING_ROD && override.name.endsWith("_cast")) {
                    val parentName = override.name.substring(0, override.name.length - "_cast".length)
                    val parentOverride = entry.value.packOverride.firstOrNull { it.name == parentName }
                    if (parentOverride != null) {
                        predicateJson.addProperty("custom_model_data", parentOverride.customData)
                        predicateJson.addProperty("cast", 1)
                    }
                } else {
                    predicateJson.addProperty("custom_model_data", override.customData)
                }
                overridesJson.addProperty("model", override.name)
                overridesJson.add("predicate", predicateJson)
                overrides.add(overridesJson)
            }
            json.add("overrides", overrides)
            newFile(File(folderModels, "item"), entry.key + ".json").writeText(GsonBuilder().setPrettyPrinting().create().toJson(json))
        }
        // generate pack object
        packObject.forEach { pack ->
            pack.generateModel(folderModels)
            pack.generateTexture(folderTextures)
        }
        // generate zip
        FileOutputStream(file()).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                folder.listFiles()?.forEach { toZip(zipOutputStream, it, "") }
            }
        }
        folder.deepDelete()
    }

    fun generateNoModels() {
        val folderIn = File(getDataFolder(), "pack/item#pre")
        val folderOut = File(getDataFolder(), "pack/item/generated")
        folderIn.listFiles()?.filter { it.isDirectory }?.forEach { materialFile ->
            val material = materialFile.name.parseToMaterial()
            materialFile.listFiles()?.forEach { file ->
                if (file.name.endsWith(".png")) {
                    val name = file.name.replace(".png", "").replace(Regex("[() ]"), "").toLowerCase()
                    val conf = SecuredFile()
                    val json = JsonObject()
                    json.addProperty("credit", "Made with Tiphareth")
                    when (material) {
                        Material.FISHING_ROD -> {
                            if (name.endsWith("_cast")) {
                                val parent = name.substring(0, name.length - "_cast".length)
                                json.addProperty("parent", parent)
                                conf.set("item.hide", true)
                            } else {
                                json.addProperty("parent", "item/handheld_rod")
                            }
                        }
                        else -> {
                            json.addProperty("parent", getParent(material))
                        }
                    }
                    json.add("textures", run {
                        val textures = JsonObject()
                        textures.addProperty("layer0", name)
                        return@run textures
                    })
                    conf.set("item.material", material.toString())
                    conf.set("item.name", name)
                    conf.set("item.lore", listOf("", "Made with Tiphareth"))
                    conf.set("model", GsonBuilder().setPrettyPrinting().create().toJson(json))
                    file.copyTo(newFile(folderOut, "$name.png"))
                    newFile(folderOut, "$name.yml").writeText(conf.saveToString())
                    if (File(file.parentFile, file.name + ".mcmeta").exists()) {
                        File(file.parentFile, file.name + ".mcmeta").copyTo(newFile(folderOut, "$name.png.mcmeta"))
                    }
                }
            }
        }
    }

    // 16777215
    fun generateCustomData(pack: PackObject): Int {
        val material = pack.item?.type?.name ?: "unknown"
        if (mapping.contains("$material.${pack.getPackName()}")) {
            return mapping.getInt("$material.${pack.getPackName()}")
        }
        mapping.set("$material.__index__", mapping.getInt("$material.__index__") + 1)
        mapping.set("$material.${pack.getPackName()}", mapping.getInt("$material.__index__"))
        return mapping.getInt("$material.__index__")
    }

    private fun getParent(material: Material): String {
        if (material.isBlock) {
            return "block/cube_all"
        }
        if (arrayOf("_PICKAXE", "_SWORD", "_HOE", "_AXE", "_SHOVEL").any { tool -> material.toString().endsWith(tool) }) {
            return "item/handheld"
        }
        if (material == Material.FISHING_ROD) {
            return "item/handheld_rod"
        }
        if (material == Material.SHIELD) {
            return "builtin/entity"
        }
        return "item/generated"
    }

    private fun toZip(zipOutputStream: ZipOutputStream, file: File, path: String) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { toZip(zipOutputStream, it, "$path${file.name}/") }
        } else {
            FileInputStream(file).use { fileInputStream ->
                zipOutputStream.putNextEntry(ZipEntry(path + file.name))
                zipOutputStream.write(fileInputStream.readBytes())
                zipOutputStream.flush()
                zipOutputStream.closeEntry()
            }
        }
    }

    private fun getSourceName(from: String, file: File): String {
        return file.absolutePath.replace(from + "\\", "").replace("\\", "/")
    }

    private fun getSourceFiles(fromFile: File): List<File> {
        val fileList = ArrayList<File>()
        fromFile.listFiles()?.forEach { file ->
            if (file.isFile) {
                fileList.add(file)
            } else {
                fileList.addAll(getSourceFiles(file))
            }
        }
        return fileList
    }

    private fun String.checkPlaceholder(time: Long): String {
        return this
            .replace("@date", DateFormatUtils.format(System.currentTimeMillis(), "yyyy/MM/dd"))
            .replace("@hash", time.toString().digest("sha-1").substring(0, 8))
    }
}