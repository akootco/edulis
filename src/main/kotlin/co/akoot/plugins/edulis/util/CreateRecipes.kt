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
            // Shape is not 3x3
            log.error("Invalid shape for recipe $recipeName. Shape must be 3x3.")
            return null
        }

        return CraftRecipe.builder(
            recipeName,
            // set the output item or material
            ItemStack(config.getConfigurationSection("result")?.let { createItem(it, recipeName) }
                ?: return error("output", recipeName))
        ).shape(*shape.toTypedArray()) // set the shape of the recipe
            .apply { // add ingredients from the config
                config.getConfigurationSection("ingredients")?.getKeys(false)?.forEach { key ->
                    ingredient(
                        key[0],
                        config.getString("ingredients.$key.material")?.let { getInput(it) } ?: return error(
                            "input",
                            recipeName
                        ))
                }
            }.shaped("edulis") // add recipe
    }

    fun shapelessCraftingRecipes(config: ConfigurationSection, recipeName: String): Recipe? {
        return CraftRecipe.builder(
            recipeName,
            // set the output item or material
            ItemStack(config.getConfigurationSection("result")?.let { createItem(it, recipeName) }
                ?: return error("output", recipeName))
        ).apply { // add ingredients from the config
            config.getStringList("ingredients").forEach { ingredientMaterial ->
                ingredient(getInput(ingredientMaterial) ?: return error("input", recipeName))
            }
        }.shapeless("edulis") // add recipe
    }

    fun cookRecipes(config: ConfigurationSection, recipeName: String): CookRecipe? {
        return CookRecipe.builder(
            recipeName,
            config.getString("input.material")?.let { getInput(it) } ?: return cookError("input", recipeName),
            ItemStack(config.getConfigurationSection("result")?.let { createItem(it, recipeName) }
                ?: return cookError("output", recipeName)),
            config.getInt("cookTime"),
            config.getDouble("xp").toFloat()
        ).smoke("edulis") // add recipes
    }

    // erm
    fun error(type: String, recipeName: String): Recipe? {
        log.error("Invalid $type material for crafting recipe: $recipeName")
        return null
    }

    fun cookError(type: String, recipeName: String): CookRecipe? {
        log.error("Invalid $type material for smelting recipe: $recipeName")
        return null
    }
}