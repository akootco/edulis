package co.akoot.plugins.edulis

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.commands.*
import co.akoot.plugins.edulis.listeners.*
import co.akoot.plugins.edulis.util.CreateItem.loadItems
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.smeltingRecipes
import co.akoot.plugins.edulis.util.Schematics.registerSchematics
import co.akoot.plugins.edulis.util.brewery.BrewItems
import com.dre.brewery.recipe.PluginItem
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class Edulis : FoxPlugin("edulis") {

    companion object {
        lateinit var traderConfig: FoxConfig
        lateinit var smokerConfig: FoxConfig
        lateinit var itemConfig: FoxConfig
        lateinit var cakeConfig: FoxConfig
        lateinit var mobDropConfig: FoxConfig
        lateinit var craftingConfig: FoxConfig
        lateinit var overlayConfig: FoxConfig
        lateinit var brewRecipesConfig: FileConfiguration

        val log = logger("Edulis")
        fun checkPlugin(name: String): Plugin? = Bukkit.getPluginManager().getPlugin(name)
        fun pluginEnabled(name: String): Boolean = checkPlugin(name)?.isEnabled == true
    }

    override fun load() {
        logger.info("hello!!!!!!!!")

        loadItems(itemConfig)
        loadItems(cakeConfig)

        for (key in smokerConfig.getKeys()) {
            smeltingRecipes(key)
        }

        for (key in craftingConfig.getKeys()) {
            craftingRecipes(key)
        }

        registerSchematics(this)

        brewRecipesConfig = loadYamlConfig(this,"recipes/brew.yml")

        if (checkPlugin("Brewery") != null) {
            PluginItem.registerForConfig("edulis") { BrewItems() }
            logger.info("Items are now compatible with Brewery.")
        }
    }

    override fun unload() {
        logger.info("goodbye!!!!!!!!")
    }

    override fun registerCommands() {
        registerCommand(FoodCommand(this))
        registerCommand(CovidCommand(this))
        registerCommand(CureCommand(this))
        registerCommand(ImmuneCommand(this))
        registerCommand(ReloadCommand(this))
    }

    override fun registerConfigs() {
        traderConfig = registerConfig("trades", "data/trades.conf")
        smokerConfig = registerConfig("smoker", "recipes/smoker.conf")
        itemConfig = registerConfig("items", "items/items.conf")
        cakeConfig = registerConfig("cakes", "items/cakes.conf")
        mobDropConfig = registerConfig("mob_drops", "items/mob_drops.conf")
        craftingConfig = registerConfig("crafting", "recipes/crafting.conf")
        overlayConfig = registerConfig("overlay", "data/overlays.conf")
    }

    override fun registerEvents() {
        registerEventListener(MobDrops())
        registerEventListener(PlayerEvent(this))
        registerEventListener(PluginEvent())
        registerEventListener(BlockEvent())
        registerEventListener(EntityEvent())
    }

    private fun loadYamlConfig(plugin: FoxPlugin, path: String): FileConfiguration {
        val file = File(plugin.dataFolder, path)
        // Check if the file exists, if not, save it
        if (!file.exists()) {
            plugin.saveResource(path, false)
            log.info("Saved config: $path")
        }
        return YamlConfiguration.loadConfiguration(file)
    }
}