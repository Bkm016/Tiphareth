package ink.ptms.tiphareth.event

import io.izzel.taboolib.module.event.EventNormal
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.packet.Packet
import io.izzel.taboolib.module.packet.TPacketListener
import org.bukkit.entity.Player

/**
 * @Author sky
 * @Since 2019-12-14 23:20
 */
class AsyncResourceLoadedEvent(val player: Player) : EventNormal<AsyncResourceLoadedEvent>() {

    companion object {

        @TInject
        val packet = object : TPacketListener() {

            override fun onReceive(player: Player, packet: Packet): Boolean {
                if (packet.`is`("PacketPlayInResourcePackStatus") && packet.read("status").toString() == "SUCCESSFULLY_LOADED") {
                    AsyncResourceLoadedEvent(player).async(true).call()
                }
                return true
            }
        }
    }
}