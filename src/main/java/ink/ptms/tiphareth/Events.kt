package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackDispatcher
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

internal object Events {

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        PackDispatcher.release(e.player)
    }
}