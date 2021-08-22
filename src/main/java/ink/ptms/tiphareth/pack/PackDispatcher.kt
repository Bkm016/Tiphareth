package ink.ptms.tiphareth.pack

import com.google.common.collect.Lists
import ink.ptms.tiphareth.Tiphareth
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.PacketReceiveEvent
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.asLangText

object PackDispatcher {

    internal val dispatchPlayer = Lists.newArrayList<String>()

    @SubscribeEvent
    internal fun e(e: PacketReceiveEvent) {
        if (e.packet.name == "PacketPlayInPosition" && !dispatchPlayer.contains(e.player.name) && Tiphareth.conf.getBoolean("automatically-dispatch.dispatch")) {
            dispatchPlayer.add(e.player.name)
            submit {
                send(e.player)
            }
        }
        if (e.packet.name == "PacketPlayInResourcePackStatus") {
            val status = e.packet.read<Any>(if (MinecraftVersion.isUniversal) "a" else "status").toString()
            info("PacketPlayInResourcePackStatus : ${e.player.name} -> $status")
            submit {
                when (status) {
                    "DECLINED", "FAILED_DOWNLOAD" -> e.player.kickPlayer(e.player.asLangText("force-to-accept"))
                    "ACCEPTED", "SUCCESSFULLY_LOADED" -> e.player.setMetadata("tiphareth:pack-accept", FixedMetadataValue(BukkitPlugin.getInstance(), true))
                }
            }
        }
    }

    fun send(player: Player) {
        if (player.hasPermission("tiphareth.bypass") && isForceToAcceptPermissionBypass()) {
            return
        }
        val url = if (PackUploader.isEnable) PackUploader.getPackURL() else getFileURL()
        if (url != null) {
            player.removeMetadata("tiphareth:pack-accept", BukkitPlugin.getInstance())
            player.setResourcePack(url)
            submit(delay = getForceToAcceptTimeout() * 20L) {
                if (player.hasMetadata("tiphareth:pack-accept") || !player.isOnline) {
                    return@submit
                }
                player.kickPlayer(player.asLangText("force-to-accept"))
            }
        }
    }

    fun release(player: Player) {
        dispatchPlayer.remove(player.name)
        player.removeMetadata("tiphareth:pack-accept", BukkitPlugin.getInstance())
    }

    private fun getFileURL(): String? {
        return Tiphareth.conf.getString("automatically-dispatch.file-url")
    }

    private fun isForceToAccept(): Boolean {
        return Tiphareth.conf.getBoolean("automatically-dispatch.force-to-accept")
    }

    private fun getForceToAcceptTimeout(): Int {
        return Tiphareth.conf.getInt("automatically-dispatch.force-to-accept-timeout")
    }

    private fun isForceToAcceptPermissionBypass(): Boolean {
        return Tiphareth.conf.getBoolean("automatically-dispatch.force-to-accept-permission-bypass")
    }
}