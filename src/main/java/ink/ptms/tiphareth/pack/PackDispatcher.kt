package ink.ptms.tiphareth.pack

import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.packet.Packet
import io.izzel.taboolib.module.packet.TPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

object PackDispatcher {

    @TPacket(type = TPacket.Type.RECEIVE)
    private fun send(player: Player, packet: Packet) {
        if (packet.`is`("PacketPlayInResourcePackStatus")) {
            when (packet.read("status").toString()) {
                "DECLINED", "FAILED_DOWNLOAD" -> player.kickPlayer(TLocale.asString("force-to-accept"))
                "ACCEPTED", "SUCCESSFULLY_LOADED" -> player.setMetadata("tiphareth:pack-accept", FixedMetadataValue(Tiphareth.getPlugin(), true))
            }
        }
    }

    fun send(player: Player) {
        val url = if (PackUploader.isEnable) PackUploader.packURL else getFileURL()
        if (url != null) {
            player.removeMetadata("tiphareth:pack-accept", Tiphareth.getPlugin())
            player.setResourcePack(url)
            Bukkit.getScheduler().runTaskLater(Tiphareth.getPlugin(), Runnable {
                if (player.hasMetadata("tiphareth:pack-accept") || !player.isOnline) {
                    return@Runnable
                }
                player.kickPlayer(TLocale.asString("force-to-accept"))
            }, getForceToAcceptTimeout() * 20L)
        }
    }

    private fun getFileURL(): String? = Tiphareth.CONF.getString("automatically-dispatch.file-url")

    private fun isForceToAccept(): Boolean = Tiphareth.CONF.getBoolean("automatically-dispatch.force-to-accept")

    private fun getForceToAcceptTimeout(): Int = Tiphareth.CONF.getInt("automatically-dispatch.force-to-accept-timeout")

}