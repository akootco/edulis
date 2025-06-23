package co.akoot.plugins.edulis.util

import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.smithConfig
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.util.Materials.getInput
import co.akoot.plugins.edulis.util.Materials.getMaterial
import co.akoot.plugins.plushies.util.builders.CookRecipe
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import co.akoot.plugins.plushies.util.builders.SmithRecipe
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.inventory.ItemStack

object CreateRecipes {

    private fun errorIn(input: String, key: String) {
        log.error("Invalid input: $input for recipe $key.")
    }
    private fun errorOut(output: String, key: String) {
        log.error("Invalid output: $output for recipe $key.")
    }

    fun smeltingRecipes(key: String) {
        val result = smokerConfig.getString("$key.output") ?: return
        val input = smokerConfig.getString("$key.input") ?: return

        val parts = result.split("/")
        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1

        CookRecipe.builder(
            key,
            getInput(input, key) ?: return errorIn(input,key),
            getMaterial(parts[0],amount,key) ?: return errorOut(input,key),
            smokerConfig.getString("$key.cookTime"),
            smokerConfig.getDouble("$key.xp")
        ).smelt("edulis").smoke("edulis")
    }

    fun smithingRecipes(key: String) {
        val result = smithConfig.getString("$key.output") ?: return
        val base = smithConfig.getString("$key.base") ?: return
        val template = smithConfig.getString("$key.template") ?: return
        val addition = smithConfig.getString("$key.addition") ?: return

        val parts = result.split("/")
        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1

        SmithRecipe.builder(
            key,
            getInput(template, key) ?: return errorIn(template,key),
            getInput(base, key) ?: return errorIn(base,key),
            getInput(addition, key) ?: return errorIn(addition,key),
            getMaterial(parts[0],amount,key) ?: return errorOut(result,key)
        ).add("edulis")
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
                ItemStack(getMaterial(input[0],inputAmount,key) ?:  return errorOut(input[0],key))
            )// skip if output is invalid
                .apply {
                    for (ingredient in craftingConfig.getStringList("$key.ingredients")) {
                        val parts = ingredient.split("/")
                        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        val material = getInput(parts[0], key)
                        if (material == null) {
                            errorIn(parts[0],key)
                            continue // Skip if invalid
                        }

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
                ItemStack(getMaterial(input[0],inputAmount,key) ?: return errorOut(input[0],key)))
                .shape(shape[0], shape[1], shape[2])
                .apply {
                    for (ingredient in craftingConfig.getKeys("$key.ingredients")) {
                        val material = craftingConfig.getString("$key.ingredients.$ingredient")
                            ?.let { getInput(it, key) }

                        if (material == null) {
                            errorIn(ingredient,key)
                            continue // Skip if input is invalid
                        }

                        // all valid, add ingredient
                        ingredient(ingredient[0], material)
                    }
                }.shaped("edulis")
        }
    }
}