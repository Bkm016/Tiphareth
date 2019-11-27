package ink.ptms.tiphareth.pack

import com.google.common.collect.Maps
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.module.db.local.Local
import io.izzel.taboolib.util.Files
import io.izzel.taboolib.util.IO
import io.izzel.taboolib.util.item.Items
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.util.NumberConversions
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.ArrayList


object PackGenerator {

    fun file(): File = Files.file(File(Tiphareth.getPlugin().dataFolder, "tiphareth-pack.zip"))

    fun generate(packObject: List<PackObject>) {
        val folder = Files.folder(Tiphareth.getPlugin().dataFolder, "tiphareth-pack")
        Files.deepDelete(folder)
        Files.folder(folder)
        // pack info
        Files.copy(File(Tiphareth.getPlugin().dataFolder, "pack/pack.mcmeta"), Files.file(folder, "pack.mcmeta"))
        Files.copy(File(Tiphareth.getPlugin().dataFolder, "pack/pack.png"), Files.file(folder, "pack.png"))
        // pack folder
        val folderModels = Files.folder(folder, "assets/minecraft/models")
        val folderTextures = Files.folder(folder, "assets/minecraft/textures")
        // generate pack model
        val mapItem = Maps.newHashMap<String, PackModel>()
        packObject.forEach { pack ->
            when (pack.packType) {
                PackType.ITEM -> {
                    mapItem.computeIfAbsent(pack.getModelName()) { _ ->
                        PackModel(getParent(pack.item!!.type), pack.getTextures())
                    }.packOverride.add(PackOverride(pack.getPackName(), generateCustomData(pack.getPackName())))
                }
            }
        }
        mapItem.forEach { entry ->
            val json = JsonObject()
            json.addProperty("parent", entry.value.parent)
            val textures = JsonObject()
            entry.value.textures.forEachIndexed { index, texture ->
                textures.addProperty("layer$index", "item/$texture")
            }
            json.add("textures", textures)
            val overrides = JsonArray()
            entry.value.packOverride.forEach { override ->
                val overridesJson = JsonObject()
                val predicateJson = JsonObject()
                predicateJson.addProperty("custom_model_data", override.customData)
                overridesJson.addProperty("model", override.name)
                overridesJson.add("predicate", predicateJson)
                overrides.add(overridesJson)
            }
            json.add("overrides", overrides)
            Files.toFile(GsonBuilder().setPrettyPrinting().create().toJson(json), Files.file(Files.folder(folderModels, "item"), entry.key + ".json"))
        }
        // generate pack object
        packObject.forEach { pack ->
            pack.generateModel(folderModels)
            pack.generateTexture(folderTextures)
        }
        // generate zip
        FileOutputStream(file()).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                folder.listFiles().forEach { toZip(zipOutputStream, it, "") }
            }
        }
        Files.deepDelete(folder)
    }

    fun generateNoModels() {
        val folderIn = Files.folder(Tiphareth.getPlugin().dataFolder, "pack/item#pre")
        val folderOut = Files.folder(Tiphareth.getPlugin().dataFolder, "pack/item/generated")
        folderIn.listFiles().filter { it.isDirectory }.forEach { materialFile ->
            val material = Items.asMaterial(materialFile.name)
            materialFile.listFiles().forEach { file ->
                if (file.name.endsWith(".png")) {
                    val name = file.name.replace(".png", "").replace(Regex("[() ]"), "")
                    val json = JsonObject()
                    json.addProperty("credit", "Made with Tiphareth")
                    json.addProperty("parent", getParent(material))
                    json.add("textures", run {
                        val textures = JsonObject()
                        textures.addProperty("layer0", name)
                        return@run textures
                    })
                    val conf = YamlConfiguration()
                    conf.set("item.material", material.toString())
                    conf.set("item.name", name)
                    conf.set("item.lore", listOf("", "Made with Tiphareth"))
                    conf.set("model", GsonBuilder().setPrettyPrinting().create().toJson(json))
                    if (File(folderOut, "$name.png.mcmeta").exists()) {
                        Files.copy(file, Files.file(folderOut, "$name.png.mcmeta"))
                    }
                    Files.copy(file, Files.file(folderOut, "$name.png"))
                    Files.toFile(conf.saveToString(), Files.file(folderOut, "$name.yml"))
                }
            }
        }
    }

    fun generateCustomData(id: String): Int {
        val mapping = Local.get().get("data-mapping")
        if (mapping.contains(id)) {
            return mapping.getInt(id)
        }
        val map = mapping.getValues(false)
        for (index in 1..16777215) {
            if (!map.containsValue(index)) {
                mapping.set(id, index)
                return index;
            }
        }
        return -1
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
        return "item/generated"
    }

    private fun toZip(zipOutputStream: ZipOutputStream, file: File, path: String) {
        if (file.isDirectory) {
            file.listFiles().forEach { toZip(zipOutputStream, it, "$path${file.name}/") }
        } else {
            FileInputStream(file).use { fileInputStream ->
                zipOutputStream.putNextEntry(ZipEntry(path + file.name))
                zipOutputStream.write(IO.readFully(fileInputStream))
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
        for (file in fromFile.listFiles()) {
            if (file.isFile) {
                fileList.add(file)
            } else {
                fileList.addAll(getSourceFiles(file))
            }
        }
        return fileList
    }
}