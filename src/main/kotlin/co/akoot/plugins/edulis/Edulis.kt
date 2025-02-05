package co.akoot.plugins.edulis

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.commands.*
import co.akoot.plugins.edulis.listeners.*
import co.akoot.plugins.edulis.util.Schematics.registerSchematics
import co.akoot.plugins.edulis.util.brewery.BrewItems
import co.akoot.plugins.edulis.util.loaders.ConfigLoader
import com.dre.brewery.recipe.PluginItem
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class Edulis : FoxPlugin("edulis") {

    companion object {
        val log = logger("Edulis")

        fun checkPlugin(name: String): Plugin? = Bukkit.getPluginManager().getPlugin(name)

        fun pluginEnabled(name: String): Boolean = checkPlugin(name)?.isEnabled == true
    }

    override fun load() {
        logger.info("hello!!!!!!!!")

        ConfigLoader.loadConfigs(this)

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

    override fun registerEvents() {
        registerEventListener(MobDrops())
        registerEventListener(PlayerEvent(this))
        registerEventListener(PluginEvent())
        registerEventListener(BlockEvent())
        registerEventListener(EntityEvent())
    }
}