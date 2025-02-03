package co.akoot.plugins.edulis

import org.bukkit.Bukkit
import co.akoot.plugins.bluefox.api.FoxPlugin

import co.akoot.plugins.edulis.commands.CovidCommand
import co.akoot.plugins.edulis.commands.CureCommand
import co.akoot.plugins.edulis.commands.FoodCommand
import co.akoot.plugins.edulis.commands.ImmuneCommand
import co.akoot.plugins.edulis.listeners.Block

import co.akoot.plugins.edulis.listeners.MobDrops
import co.akoot.plugins.edulis.listeners.Player

import co.akoot.plugins.edulis.util.loaders.ConfigLoader
import co.akoot.plugins.edulis.util.brewery.BrewItems
import co.akoot.plugins.edulis.util.brewery.BrewRecipes

import com.dre.brewery.recipe.PluginItem
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger


class Edulis : FoxPlugin("edulis") {

    companion object {
        val log = logger("Edulis")
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
    }

    override fun registerEvents() {
        registerEventListener(MobDrops())
        registerEventListener(Player(this))
        registerEventListener(BrewRecipes())
        registerEventListener(Block())
    }
}