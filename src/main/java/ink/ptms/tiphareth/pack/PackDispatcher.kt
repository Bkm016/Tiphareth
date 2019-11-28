package ink.ptms.tiphareth.pack

import com.google.common.collect.Lists
import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.packet.Packet
import io.izzel.taboolib.module.packet.TPacket
import io.izzel.taboolib.util.Strings
import net.minecraft.server.v1_14_R1.PacketPlayOutResourcePackSend
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.math.BigInteger
import java.net.URL


object PackDispatcher {

    private val dispatchPlayer = Lists.newArrayList<String>()

    @TPacket(type = TPacket.Type.RECEIVE)
    private fun send(player: Player, packet: Packet): Boolean {
        if (packet.`is`("PacketPlayInPosition") && !dispatchPlayer.contains(player.name)) {
            dispatchPlayer.add(player.name)
            Bukkit.getScheduler().runTask(Tiphareth.getPlugin(), Runnable {
                send(player)
            })
        }
        if (packet.`is`("PacketPlayInResourcePackStatus")) {
            Bukkit.getScheduler().runTask(Tiphareth.getPlugin(), Runnable {
                when (packet.read("status").toString()) {
                    "DECLINED", "FAILED_DOWNLOAD" -> player.kickPlayer(TLocale.asString("force-to-accept"))
                    "ACCEPTED", "SUCCESSFULLY_LOADED" -> player.setMetadata("tiphareth:pack-accept", FixedMetadataValue(Tiphareth.getPlugin(), true))
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

    fun release(player: Player) {
        dispatchPlayer.remove(player.name)
    }

    private fun getFileURL(): String? = Tiphareth.CONF.getString("automatically-dispatch.file-url")

    private fun isForceToAccept(): Boolean = Tiphareth.CONF.getBoolean("automatically-dispatch.force-to-accept")

    private fun getForceToAcceptTimeout(): Int = Tiphareth.CONF.getInt("automatically-dispatch.force-to-accept-timeout")

    private fun isForceToAcceptPermissionBypass(): Boolean = Tiphareth.CONF.getBoolean("automatically-dispatch.force-to-accept-permission-bypass")


}