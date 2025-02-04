package co.akoot.plugins.edulis.util.loaders

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.log
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object ConfigLoader {

    lateinit var itemsConfig: FileConfiguration
    lateinit var mobDropsConfig: FileConfiguration
    lateinit var smokerRecipesConfig: FileConfiguration
    lateinit var craftRecipesConfig: FileConfiguration
    lateinit var cakesConfig: FileConfiguration
    lateinit var brewRecipesConfig: FileConfiguration
    lateinit var overlayConfig: FileConfiguration
    lateinit var tradeConfig: FileConfiguration

    fun loadConfigs(plugin: FoxPlugin) {
        itemsConfig = loadConfig(plugin, "items/items.yml")
        mobDropsConfig = loadConfig(plugin, "items/mob_drops.yml")
        smokerRecipesConfig = loadConfig(plugin, "recipes/smoker.yml")
        craftRecipesConfig = loadConfig(plugin, "recipes/crafting.yml")
        cakesConfig = loadConfig(plugin, "items/cakes.yml")
        brewRecipesConfig = loadConfig(plugin, "recipes/brew.yml")
        overlayConfig = loadConfig(plugin, "data/block_overlays.yml")
        tradeConfig = loadConfig(plugin,"data/trades.yml" )

        ItemLoader().loadItems()
    }

    private fun loadConfig(plugin: FoxPlugin, path: String): FileConfiguration {
        val file = File(plugin.dataFolder, path)
        // Check if the file exists, if not, save it
        if (!file.exists()) {
            plugin.saveResource(path, false)
            log.info("Saved config: $path")
        }
        return YamlConfiguration.loadConfiguration(file)
    }
}
