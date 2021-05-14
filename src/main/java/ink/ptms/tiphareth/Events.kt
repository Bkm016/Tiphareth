package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackDispatcher
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

@TListener
class Events : Listener {

    @EventHandler
    fun e(e: PlayerQuitEvent) {
        PackDispatcher.release(e.player)
    }
}