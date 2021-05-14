package ink.ptms.tiphareth.pack

import com.google.common.collect.Lists
import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.packet.Packet
import io.izzel.taboolib.module.packet.TPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

object PackDispatcher {

    private val dispatchPlayer = Lists.newArrayList<String>()

    @TPacket(type = TPacket.Type.RECEIVE)
    private fun send(player: Player, packet: Packet): Boolean {
        if (packet.`is`("PacketPlayInPosition") && !dispatchPlayer.contains(player.name) && Tiphareth.conf.getBoolean("automatically-dispatch.dispatch")) {
            dispatchPlayer.add(player.name)
            Bukkit.getScheduler().runTask(Tiphareth.plugin, Runnable {
                send(player)
            })
        }
        if (packet.`is`("PacketPlayInResourcePackStatus")) {
            println("[Tiphareth] PacketPlayInResourcePackStatus of ${player.name} -> ${packet.read("status")}")
            Bukkit.getScheduler().runTask(Tiphareth.plugin, Runnable {
                when (packet.read("status").toString()) {
                    "DECLINED", "FAILED_DOWNLOAD" -> player.kickPlayer(TLocale.asString("force-to-accept"))
                    "ACCEPTED", "SUCCESSFULLY_LOADED" -> player.setMetadata("tiphareth:pack-accept", FixedMetadataValue(Tiphareth.plugin, true))
                }
            })
        }
        return true
    }

    fun send(player: Player) {
        if (player.hasPermission("tiphareth.bypass") && isForceToAcceptPermissionBypass()) {
            return
        }
        val url = if (PackUploader.isEnable) PackUploader.getPackURL() else getFileURL()
        if (url != null) {
            player.removeMetadata("tiphareth:pack-accept", Tiphareth.plugin)
            player.setResourcePack(url)
            Bukkit.getScheduler().runTaskLater(Tiphareth.plugin, Runnable {
                if (player.hasMetadata("tiphareth:pack-accept") || !player.isOnline) {
                    return@Runnable
                }
                player.kickPlayer(TLocale.asString("force-to-accept"))
            }, getForceToAcceptTimeout() * 20L)
        }
    }

    fun release(player: Player) {
        dispatchPlayer.remove(player.name)
        player.removeMetadata("tiphareth:pack-accept", Tiphareth.plugin)
    }

    private fun getFileURL(): String? = Tiphareth.conf.getString("automatically-dispatch.file-url")

    private fun isForceToAccept(): Boolean = Tiphareth.conf.getBoolean("automatically-dispatch.force-to-accept")

    private fun getForceToAcceptTimeout(): Int = Tiphareth.conf.getInt("automatically-dispatch.force-to-accept-timeout")

    private fun isForceToAcceptPermissionBypass(): Boolean = Tiphareth.conf.getBoolean("automatically-dispatch.force-to-accept-permission-bypass")


}