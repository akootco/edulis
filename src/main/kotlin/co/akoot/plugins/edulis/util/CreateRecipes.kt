package co.akoot.plugins.edulis.util

import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.util.CreateItem.getInput
import co.akoot.plugins.edulis.util.CreateItem.getMaterial
import co.akoot.plugins.plushies.util.builders.CookRecipe
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.inventory.ItemStack

object CreateRecipes {

    fun smeltingRecipes(key: String) {
        val result = smokerConfig.getString("$key.output") ?: return
        val input = smokerConfig.getString("$key.input") ?: return

        val parts = result.split("/")
        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1

        CookRecipe.builder(
            key,
            getInput(input, key) ?: return,
            getMaterial(parts[0], amount = amount, recipeName = key) ?: return,
            smokerConfig.getString("$key.cookTime"),
            smokerConfig.getDouble("$key.xp")
        ).smelt("edulis").smoke("edulis")
    }

    fun craftingRecipes(key: String) {
        val shape = craftingConfig.getStringList("$key.shape")
        val result = craftingConfig.getString("$key.result") ?: return
        val input = result.split("/")
        val inputAmount = input.getOrNull(1)?.toIntOrNull() ?: 1

        if (shape.isEmpty()) {

            // shapeless recipe
            CraftRecipe.builder(
                key,
                ItemStack(getMaterial(input[0],amount = inputAmount, recipeName = key) ?: return)
            )// skip if output is invalid
                .apply {
                    for (ingredient in craftingConfig.getStringList("$key.ingredients")) {
                        val parts = ingredient.split("/")
                        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        val material = getInput(parts[0], key) ?: continue // skip if input is invalid

                        // nice!, add ingredient to recipe
                        ingredient(material, amount)
                    }
                }.shapeless("edulis")
        } else {

            if (shape.size < 3) {
                logger("Edulis").warn("Invalid shape for recipe $key: shape size = ${shape.size}, shape = $shape")
                return
            }

            CraftRecipe.builder(
                key,
                ItemStack(getMaterial(input[0],amount = inputAmount, recipeName = key) ?: return)
            ) // skip if output is invalid
                .shape(shape[0], shape[1], shape[2])
                .apply {
                    for (ingredient in craftingConfig.getKeys("$key.ingredients")) {
                        val material = craftingConfig.getString("$key.ingredients.$ingredient")
                            ?.let { getInput(it, key) } ?: continue // skip if input is invalid

                        // all valid, add ingredient
                        ingredient(ingredient[0], material)
                    }
                }.shaped("edulis")
        }
    }
}