package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.plushies.util.ItemCreator
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.Recipes
import com.dre.brewery.api.BreweryApi
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

object Materials {

    val pendingRecipes: MutableSet<String> = mutableSetOf()

    fun Material.matches(material: Material): Boolean = this == material

    // get recipe input items
    fun getInput(input: String, recipeName: String): RecipeChoice? {
        // brewery items
        if (input.startsWith("brewery:")) {
            // Check if the Brewery plugin is enabled
            if (pluginEnabled("BreweryX")) {
                BreweryApi.createBrewItem(
                    BreweryApi.getRecipe(
                        input.removePrefix("brewery:")
                            .replace("_", " ")
                    ), 10 // i hate how this is auto formatted but oh well
                )?.let { brewItem ->
                    return RecipeChoice.ExactChoice(brewItem)
                }
            } else {
                pendingRecipes.add(recipeName.lowercase())
                return null
            }
        }

        Recipes.getInput(input)?.let { return it }

        return null
    }


    // try to get material from config
    fun getMaterial(input: String, amount: Int = 1, recipeName: String = "".replace("edulis:", "")): ItemStack? {
            // Brewery items
        if (input.startsWith("brewery:")) {
            if (pluginEnabled("BreweryX")) {
                val brewItem = BreweryApi.createBrewItem(
                    input.removePrefix("brewery:").replace("_", " "),
                    10
                )
                brewItem?.amount = amount
                return brewItem
            } else {
                pendingRecipes.add(recipeName.lowercase())
                return null
            }
        }

        return Recipes.getMaterial(input)
    }

    fun loadItems(config: FoxConfig) {
        for (key in config.getKeys()) {
            customItems[key.lowercase()] = ItemCreator.createItem(config, key, foodKey)?: continue
        }
    }
}