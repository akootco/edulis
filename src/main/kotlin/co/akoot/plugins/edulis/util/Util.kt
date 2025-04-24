package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.edulis.Edulis.Companion.cakeConfig
import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.headConfig
import co.akoot.plugins.edulis.Edulis.Companion.itemConfig
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.util.Materials.loadItems
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.smeltingRecipes
import co.akoot.plugins.edulis.util.Materials.resolvedResults
import co.akoot.plugins.edulis.util.Schematics.registerSchematics
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.PlayerInventory
import java.io.File

object Util {
    fun loadEverything(plugin: FoxPlugin) {

        loadItems(itemConfig)
        loadItems(cakeConfig)
        loadItems(headConfig)

        // remove all flugin recipes
        val iterator = Bukkit.recipeIterator()
        while (iterator.hasNext()) {
            val recipe = iterator.next()

            if (recipe is Keyed) {
                val key = (recipe as Keyed).key // erm?
                if (key.namespace == "edulis") {
                    Bukkit.removeRecipe(key)
                }
            }
        }

        for (key in smokerConfig.getKeys()) {
            smeltingRecipes(key)
        }

        for (key in craftingConfig.getKeys()) {
            craftingRecipes(key)
        }

        // remove all flugin schematics
        val structureManager = Bukkit.getStructureManager()
        for (structure in structureManager.structures) {
            if (structure is Keyed) {
                val key = structure.key
                if (key.namespace == "edulis") {
                    structureManager.unregisterStructure(key)
                }
            }
        }

        registerSchematics(plugin)
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

    fun updateItem(inv: PlayerInventory) {
        val hand = inv.itemInMainHand.clone()
        if (hand.isEmpty) return

        val meta = hand.itemMeta ?: return
        // if not flugin item, return
        val pdc = meta.getPDC<String>(foodKey) ?: return

        // save name and lore to re add later
        val ogName = meta.customName()
        val ogLore = meta.lore()

        // remove name lore for comparison
        hand.itemMeta = hand.itemMeta?.apply {
            customName(null)
            lore(null)
        }

        // if item is not in the list or is unchanged, do nothing
        val item = resolvedResults[pdc] ?: return
        if (hand.isSimilar(item)) return

        // update item and re-add name and lore
        val updatedItem = item.clone().apply {
            amount = hand.amount
            itemMeta = itemMeta?.apply {
                if (ogName != null) customName(ogName)
                if (ogLore != null) lore(ogLore)
            }
        }

        inv.setItemInMainHand(updatedItem)
    }
}