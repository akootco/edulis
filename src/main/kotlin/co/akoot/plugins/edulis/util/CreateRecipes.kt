package co.akoot.plugins.edulis.util

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.CreateItem.createItem
import co.akoot.plugins.edulis.util.CreateItem.getInput
import co.akoot.plugins.plushies.util.builders.CookRecipe
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

object CreateRecipes {
    fun craftingRecipes(config: ConfigurationSection, recipeName: String): Recipe? {
        // i let the intelliJ suggestions go crazy lol, worked out great!
        if (!config.contains("shape")) {
            return shapelessCraftingRecipes(config, recipeName)
        }

        val shape = config.getStringList("shape")
        // make sure the shape is 3x3 so paper doesn't start crying
        if (shape.size != 3 || !shape.all { row -> row.length == 3 }) {
            log.error("Invalid shape for recipe $recipeName. Shape must be 3x3.") // Shape is not 3x3
            return null
        }

        return CraftRecipe.builder(recipeName,
            ItemStack(config.getConfigurationSection("result")?.let

            { createItem(it, recipeName) } ?: return null)) // set the output item or material

            .shape(*shape.toTypedArray()) // set the shape of the recipe

            .apply { config.getConfigurationSection("ingredients")?.getKeys(false)?.forEach { key ->
                    ingredient(key[0], config.getString("ingredients.$key.material")?.let
                    { getInput(it, recipeName) } ?: return null)
                }

            }.shaped("edulis") // add recipe
    }

    private fun shapelessCraftingRecipes(config: ConfigurationSection, recipeName: String): Recipe? {
        return CraftRecipe.builder(recipeName,

            ItemStack(config.getConfigurationSection("result")?.let
            { createItem(it, recipeName) } ?: return null)) // set the output item or material

            .apply { config.getStringList("ingredients").forEach { ingredientMaterial ->
                ingredient(getInput(ingredientMaterial, recipeName) ?: return null) }

        }.shapeless("edulis") // add recipe
    }

    fun cookRecipes(config: ConfigurationSection, recipeName: String): CookRecipe? {
        return CookRecipe.builder(recipeName,
            config.getString("input")?.let { getInput(it, recipeName) } ?: return null,

            ItemStack(config.getConfigurationSection("result")?.let
            { createItem(it, recipeName) } ?: return null), // set the output item or material

            config.getInt("cookTime"),
            config.getDouble("xp").toFloat()

        ).smoke("edulis") // add recipes
    }
}