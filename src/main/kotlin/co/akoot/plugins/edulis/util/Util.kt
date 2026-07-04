package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.edulis.Edulis.Companion.cakeConfig
import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.itemConfig
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.smithConfig
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.blocks.cuttingboard.createCuttingBoards
import co.akoot.plugins.edulis.util.Schematics.registerSchematics
import co.akoot.plugins.plushies.util.ItemCreator
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.Items.registerItem
import co.akoot.plugins.plushies.util.Recipes.configRecipes
import co.akoot.plugins.plushies.util.Recipes.smeltingRecipes
import co.akoot.plugins.plushies.util.Recipes.smithingRecipes
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import kotlin.collections.set

object Util {

    val ItemStack.isFood: Boolean
        get() = itemMeta.hasPDC(foodKey)

    val ItemStack.foodid: String
        get() = itemMeta.getPDC<String>(foodKey) ?: type.name

    fun loadEverything() {
        loadItems()
        configRecipes(craftingConfig, "edulis")
        smeltingRecipes(smokerConfig, "edulis")
        smithingRecipes(smithConfig,"edulis" )
        registerSchematics()
    }

    fun loadItems() {
        for (config in listOf(itemConfig, cakeConfig)) {
            for (key in config.getKeys()) {
                registerItem(key.lowercase(),ItemCreator.createItem(config, key, foodKey) ?: continue)
            }
        }
        createCuttingBoards()
    }

    fun loadYamlConfig(plugin: FoxPlugin, path: String): FileConfiguration {
        val file = File(plugin.dataFolder, path)
        // Check if the file exists, if not, save it
        if (!file.exists()) {
            plugin.saveResource(path, false)
            log.info("Saved config: $path")
        }
        return YamlConfiguration.loadConfiguration(file)
    }
}