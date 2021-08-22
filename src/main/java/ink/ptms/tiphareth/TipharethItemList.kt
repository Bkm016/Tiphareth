package ink.ptms.tiphareth

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.giveItem
import taboolib.platform.util.inventoryCenterSlots

object TipharethItemList {

    fun openMenu(player: Player, page: Int) {
        val objects = TipharethAPI.LOADER.items.groupBy { it.item.type }
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 2f)
        player.openMenu<Linked<Material>>("Tiphareth Items - %p") {
            setProperty("page", page)
            rows(6)
            slots(inventoryCenterSlots)
            elements {
                objects.keys.toList()
            }
            onGenerate { _, element, _, _ ->
                buildItem(XMaterial.matchXMaterial(element)) {
                    name = "§7${Tiphareth.conf.getString("group-name.$element") ?: element.name}"
                }
            }
            onClick { _, element ->
                openMenu(player, element, 0)
            }
            setNextPage(51) { _, hasNextPage ->
                if (hasNextPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "§7下一页" }
                } else {
                    buildItem(XMaterial.ARROW) { name = "§8下一页" }
                }
            }
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "§7上一页" }
                } else {
                    buildItem(XMaterial.ARROW) { name = "§8上一页" }
                }
            }
        }
    }

    fun openMenu(player: Player, material: Material, page: Int) {
        val objects = TipharethAPI.LOADER.items.filter { it.item.type == material }
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 2f)
        player.openMenu<Linked<String>>("Tiphareth Items - $material - %p") {
            setProperty("page", page)
            rows(6)
            slots(inventoryCenterSlots)
            elements {
                objects.map { it.getPackName() }.toList()
            }
            onGenerate { _, element, _, _ ->
                val packObject = objects.first { it.getPackName() == element }
                if (packObject.itemHide) {
                    buildItem(XMaterial.BARRIER) { name = "§c[隐藏: ${packObject.getPackName()}]" }
                } else {
                    packObject.buildItem()
                }
            }
            onClick { _, element ->
                player.giveItem(objects.first { it.getPackName() == element }.buildItem())
            }
            setNextPage(51) { _, hasNextPage ->
                if (hasNextPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "§7下一页" }
                } else {
                    buildItem(XMaterial.ARROW) { name = "§8下一页" }
                }
            }
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "§7上一页" }
                } else {
                    buildItem(XMaterial.ARROW) { name = "§8上一页" }
                }
            }
        }
    }
}