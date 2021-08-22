package ink.ptms.tiphareth.event

import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.PacketReceiveEvent
import taboolib.platform.type.BukkitProxyEvent

/**
 * @Author sky
 * @Since 2019-12-14 23:20
 */
class AsyncResourceLoadedEvent(val player: Player) : BukkitProxyEvent() {

    companion object {

        @SubscribeEvent
        fun e(e: PacketReceiveEvent) {
            if (e.packet.name == "PacketPlayInResourcePackStatus") {
                if (e.packet.read<Any>(if (MinecraftVersion.isUniversal) "a" else "status").toString() == "SUCCESSFULLY_LOADED") {
                    AsyncResourceLoadedEvent(e.player).call()
                }
            }
        }
    }
}