package co.akoot.plugins.edulis

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.commands.*
import co.akoot.plugins.edulis.listeners.*
import co.akoot.plugins.edulis.listeners.tasks.pauseCovid
import co.akoot.plugins.edulis.util.Util.loadEverything
import co.akoot.plugins.edulis.util.Util.loadYamlConfig
import co.akoot.plugins.edulis.util.brewery.BrewItems
import co.akoot.plugins.edulis.util.brewery.DrinksCommand
import com.dre.brewery.recipe.PluginItem
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class Edulis : FoxPlugin("edulis") {

    companion object {
        lateinit var traderConfig: FoxConfig
        lateinit var smokerConfig: FoxConfig
        lateinit var itemConfig: FoxConfig
        lateinit var cakeConfig: FoxConfig
        lateinit var mobDropConfig: FoxConfig
        lateinit var craftingConfig: FoxConfig
        lateinit var overlayConfig: FoxConfig
        lateinit var leafConfig: FoxConfig

        lateinit var brewRecipesConfig: FileConfiguration

        fun key(key: String): NamespacedKey {
            return NamespacedKey("edulis", key)
        }

        val foodKey = key("food")

        val log = logger("Edulis")
        fun checkPlugin(name: String): Plugin? = Bukkit.getPluginManager().getPlugin(name)
        fun pluginEnabled(name: String): Boolean = checkPlugin(name)?.isEnabled == true
    }

    override fun load() {
        logger.info("hello!!!!!!!!")

        loadEverything(this)

        brewRecipesConfig = loadYamlConfig(this, "recipes/brew.yml")

        if (checkPlugin("Brewery") != null) {
            PluginItem.registerForConfig("edulis") { BrewItems() }
            logger.info("Items are now compatible with Brewery.")
        }
    }

    override fun unload() {
        server.onlinePlayers.forEach { player ->
            pauseCovid(player)
        }
        logger.info("goodbye!!!!!!!!")
    }

    override fun registerCommands() {
        registerCommand(FoodCommand(this))
        registerCommand(CovidCommand(this))
        registerCommand(InfectCommand(this))
        registerCommand(CureCommand(this))
        registerCommand(ImmuneCommand(this))
        registerCommand(ReloadCommand(this))
        registerCommand(DrinksCommand(this))
    }

    override fun registerConfigs() {
        traderConfig = registerConfig("trades", "data/trades.conf")
        smokerConfig = registerConfig("smoker", "recipes/smoker.conf")
        itemConfig = registerConfig("items", "items/items.conf")
        cakeConfig = registerConfig("cakes", "items/cakes.conf")
        mobDropConfig = registerConfig("mob_drops", "items/mob_drops.conf")
        craftingConfig = registerConfig("crafting", "recipes/crafting.conf")
        overlayConfig = registerConfig("overlay", "data/overlays.conf")
        leafConfig = registerConfig("leafDrop", "data/leaf_drops.conf")
    }

    override fun registerEvents() {
        registerEventListener(MobDrops())
        registerEventListener(PlayerEvent(this))
        registerEventListener(PluginEvent())
        registerEventListener(BlockEvent())
        registerEventListener(EntityEvent())
    }
}