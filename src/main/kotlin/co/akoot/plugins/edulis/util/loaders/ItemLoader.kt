package co.akoot.plugins.edulis.util.loaders

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.CreateItem.createItem
import co.akoot.plugins.edulis.util.CreateItem.getMaterial
import co.akoot.plugins.edulis.util.CreateRecipes.cookRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.cakesConfig
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.craftRecipesConfig
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.itemsConfig
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.mobDropsConfig
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.smokerRecipesConfig
import org.bukkit.configuration.file.FileConfiguration

class ItemLoader {

    fun loadItems() {
        loadItem(itemsConfig, "items")
        loadItem(mobDropsConfig, "drops")
        loadItem(cakesConfig, "cakes")

        loadRecipeItems(craftRecipesConfig)
        loadRecipeItems(smokerRecipesConfig)

        loadRecipes(craftRecipesConfig, "crafting")
        loadRecipes(smokerRecipesConfig, "smoker")
    }


    // load item configs (non recipe)
    private fun loadItem(config: FileConfiguration, configSection: String) {
        val itemSection = config.getConfigurationSection(configSection)
            ?: return log.error("$configSection is empty")

        // Loop through items
        itemSection.getKeys(false).forEach { key ->
            itemSection.getConfigurationSection(key)?.let { createItem(it, key) }
        }
    }

    // i can probably just use the loadItems for this but i am lazy!
    private fun loadRecipeItems(config: FileConfiguration) {
        val recipesSection = config.getConfigurationSection("recipes")
            ?: return log.error("Missing 'recipes' section in config.")

        // Loop through the recipes
        recipesSection.getKeys(false).forEach { key ->
            val recipeSection = recipesSection.getConfigurationSection(key) ?: return
            val resultSection = recipeSection.getConfigurationSection("result") ?: return

            // try to get material
            val resultMaterialName = getMaterial(resultSection, "material")
            if (resultMaterialName != null) {
                createItem(resultSection, key)
            }
        }
    }

    private fun loadRecipes(config: FileConfiguration, recipeType: String) {
        val recipesSection = config.getConfigurationSection("recipes")
            ?: return log.error("Missing 'recipes' section in $recipeType.yml")

        // Loop through recipes
        recipesSection.getKeys(false).forEach { key ->
            val recipeSection = recipesSection.getConfigurationSection(key) ?: return

            when (recipeType) { // Check type
                "smoker" -> cookRecipes(recipeSection, key)
                "crafting" -> craftingRecipes(recipeSection, key)
                else -> log.error("Unknown recipe type: $recipeType")
            }
        }
    }
}