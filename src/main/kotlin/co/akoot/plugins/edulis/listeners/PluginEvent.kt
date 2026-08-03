package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.util.Util.registerEdulisRecipes
import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PluginEvent: Listener {
    @EventHandler
    fun ServerResourcesReloadedEvent.onReload() {
        registerEdulisRecipes()
    }
}