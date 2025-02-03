package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.CreateItem.pendingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.cookRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.brewery.BrewRecipes.loadBrewRecipes
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.craftRecipesConfig
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.smokerRecipesConfig
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
                smokerRecipesConfig.getConfigurationSection("recipes.$recipe")?.let { cookRecipes(it, recipe) }
                craftRecipesConfig.getConfigurationSection("recipes.$recipe")?.let { craftingRecipes(it, recipe) }
            }
            log.info("Loaded Brewery Recipes!")
        }
    }
}