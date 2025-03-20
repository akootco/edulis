package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.Materials.pendingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.smeltingRecipes
import co.akoot.plugins.edulis.util.brewery.BrewRecipes.loadBrewRecipes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent

class PluginEvent: Listener {

    @EventHandler
    fun onBreweryEnable(event: PluginEnableEvent) {
        // we cant load the recipes until brewery is enabled smh
        if (event.plugin.name == "Brewery") {
            loadBrewRecipes()
            for (recipe in pendingRecipes) {
                smeltingRecipes(recipe)
                craftingRecipes(recipe)
            }
            log.info("Loaded Brewery Recipes!")
        }
    }
}