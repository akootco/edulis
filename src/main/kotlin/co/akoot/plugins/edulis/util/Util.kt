package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.util.async
import co.akoot.plugins.bluefox.util.sync
import co.akoot.plugins.edulis.Edulis.Companion.cakeConfig
import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.itemConfig
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.smithConfig
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.util.CreateRecipes.smeltingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.smithingRecipes
import co.akoot.plugins.edulis.util.Materials.loadItems
import co.akoot.plugins.edulis.util.Schematics.registerSchematics
import co.akoot.plugins.plushies.util.Recipes.configRecipes
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object Util {

    val ItemStack.isFood: Boolean
        get() = itemMeta.hasPDC(foodKey)

    val ItemStack.foodid: String
        get() = itemMeta.getPDC<String>(foodKey) ?: type.name

    fun loadEverything(plugin: FoxPlugin) {
        async {
            loadItems(itemConfig)
            loadItems(cakeConfig)

            // okay ig
            val smokerKeys = smokerConfig.getKeys()
            val smithKeys = smithConfig.getKeys()

            sync {
                // remove all flugin recipes
                val iterator = Bukkit.recipeIterator()
                while (iterator.hasNext()) {
                    val recipe = iterator.next()
                    if (recipe is Keyed && recipe.key.namespace == "edulis") {
                        Bukkit.removeRecipe(recipe.key)
                    }
                }

                for (key in smokerKeys) smeltingRecipes(key)
                configRecipes(craftingConfig, "edulis")
                for (key in smithKeys) smithingRecipes(key)

                // remove all flugin schematics
                val structureManager = Bukkit.getStructureManager()
                for (structure in structureManager.structures) {
                    if (structure is Keyed && structure.key.namespace == "edulis") {
                        structureManager.unregisterStructure(structure.key)
                    }
                }

                // ya, one day ill use this
                registerSchematics(plugin)
            }
        }
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