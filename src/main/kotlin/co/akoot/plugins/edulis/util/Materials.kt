package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.plushies.util.ItemCreator
import com.dre.brewery.api.BreweryApi
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

object Materials {

    val resolvedResults: MutableMap<String, ItemStack> = HashMap()
    val pendingRecipes: MutableSet<String> = mutableSetOf()

    fun Material.matches(material: Material): Boolean = this == material

    // get recipe input items
    fun getInput(input: String, recipeName: String): RecipeChoice? {
        // brewery items
        if (input.startsWith("brewery:")) {
            // Check if the Brewery plugin is enabled
            if (pluginEnabled("Brewery")) {
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

        // material tags
        if (input.startsWith("tag.")) {
            val tag = when (input.removePrefix("tag.").uppercase()) {
                "WOOL" -> Tag.WOOL
                "LEAVES" -> Tag.LEAVES
                "PLANKS" -> Tag.PLANKS
                "LOGS" -> Tag.LOGS
                else -> {
                    // if the tag is not found, skip.
                    // i would like this to handle every tag without having to list them eventually
                    log.error("$input not found, skipping ingredient")
                    return null
                }
            }
            return RecipeChoice.MaterialChoice(tag)
        }

        // if no prefix, check for flugin item or vanilla material.
        resolvedResults.keys.find { it.equals(input, ignoreCase = true) }?.let { key ->
            resolvedResults[key]?.let {
                return RecipeChoice.ExactChoice(it)
            }
        }

        Material.getMaterial(input.uppercase())?.let { return RecipeChoice.MaterialChoice(it) }

        return null
    }


    // try to get material from config
    fun getMaterial(input: String, amount: Int = 1, recipeName: String = "".replace("edulis:", "")): ItemStack? {

            // Brewery items
        if (input.startsWith("brewery:")) {
            if (pluginEnabled("Brewery")) {
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

        // if no prefix, check for flugin item or vanilla material.
        resolvedResults.keys.find { it.equals(input, ignoreCase = true) }?.let { key ->
            resolvedResults[key]?.let {
                it.amount = amount
                return it
            }
        }

        Material.getMaterial(input.uppercase())?.let {
            val itemStack = ItemStack(it)
            itemStack.amount = amount
            return itemStack
        }

        return null
    }

    fun loadItems(config: FoxConfig) {
        for (key in config.getKeys()) {
            resolvedResults[key.lowercase()] = ItemCreator.createItem(config, key, foodKey)?: continue
        }
    }
}