package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.util.TimeUtil.parseTime
import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.util.CreateItem.getInput
import co.akoot.plugins.edulis.util.CreateItem.getMaterial
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.SmokingRecipe

object CreateRecipes {

    fun smeltingRecipes(key: String) {
        val result = smokerConfig.getString("$key.output") ?: return
        val input = smokerConfig.getString("$key.input") ?: return
        val time = parseTime(smokerConfig.getString("$key.cookTime") ?: "10s", true).toInt()
        val xp = smokerConfig.getDouble("$key.xp") ?: 0.7f

        val smokerKey = NamespacedKey("edulis", "${key}_smoker")
        val campfireKey = NamespacedKey("edulis", "${key}_campfire")
        val furnaceKey = NamespacedKey("edulis", "${key}_furnace")

        val parts = result.split("/")
        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1

        Bukkit.removeRecipe(furnaceKey)
        Bukkit.addRecipe(
            FurnaceRecipe(
                furnaceKey,
                getMaterial(parts[0], amount = amount, recipeName = key) ?: return,
                getInput(input, key) ?: return,
                xp.toFloat(),
                time
            )
        )

        Bukkit.removeRecipe(campfireKey)
        Bukkit.addRecipe(
            CampfireRecipe(
                campfireKey,
                getMaterial(parts[0], amount = amount, recipeName = key) ?: return,
                getInput(input, key) ?: return,
                xp.toFloat(),
                time * 3
            )
        )

        Bukkit.removeRecipe(smokerKey)
        Bukkit.addRecipe(
            SmokingRecipe(
                smokerKey,
                getMaterial(parts[0], amount = amount, recipeName = key) ?: return,
                getInput(input, key) ?: return,
                xp.toFloat(),
                time / 2
            )
        )
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