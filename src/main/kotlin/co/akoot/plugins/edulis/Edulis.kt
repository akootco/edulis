package co.akoot.plugins.edulis

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.commands.*
import co.akoot.plugins.edulis.listeners.BlockEvent
import co.akoot.plugins.edulis.listeners.MobDrops
import co.akoot.plugins.edulis.listeners.PlayerEvent
import co.akoot.plugins.edulis.listeners.PluginEvent
import co.akoot.plugins.edulis.util.brewery.BrewItems
import co.akoot.plugins.edulis.util.loaders.ConfigLoader
import com.dre.brewery.recipe.PluginItem
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.Bukkit

class Edulis : FoxPlugin("edulis") {

    companion object {
        val log = logger("Edulis")

        fun pluginEnabled(name: String): Boolean {
            val plugin = Bukkit.getPluginManager().getPlugin(name)
            return plugin != null && plugin.isEnabled
        }
    }

    override fun load() {
        logger.info("hello!!!!!!!!")

        ConfigLoader.loadConfigs(this)

        Bukkit.getPluginManager().getPlugin("Brewery")?.let {
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
    }
}