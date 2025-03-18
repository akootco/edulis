package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.plushies.util.builders.FoodBuilder
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import com.dre.brewery.api.BreweryApi
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

object CreateItem {

    val resolvedResults: MutableMap<String, ItemStack> = HashMap()
    val pendingRecipes: MutableSet<String> = mutableSetOf()

    val foodKey = key("food")

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


    private fun createItem(config: FoxConfig, path: String): ItemStack? {
        val itemStack = config.getString("$path.material")?.let { getMaterial(it, recipeName = path) } ?: return null

        val item = ItemBuilder.builder(itemStack).apply {

            pdc(foodKey, path)

            // attributes
            config.getStringList("$path.food.attributes").joinToString(";").takeIf { it.isNotBlank() }
                ?.let { pdc(key( "attributes"), it) }

            // name
            config.getString("$path.itemName")?.let { name -> itemName(Text(name).component) }

            // set amount
            config.getInt("$path.amount").takeIf { it != 1 }?.let { itemStack.amount = it }

            config.getString("$path.potionColor")?.let { potion(it) }

            // makes sure to get the id directly from the texture servers!
            config.getString("$path.textures")?.let { id -> headTexture(id) }

            // set custom model data
            config.getInt("$path.customModelData").takeIf { it != 0 }?.let { customModelData(it) }

            //set lore
            lore(config.getStringList("$path.lore").map { Text(it).component })

            // stackSize needs to be 1-99 or else the server will explode (real)
            config.getInt("$path.stackSize").takeIf { it in 1..99 }?.let { stackSize(it) }
        }.build()

        val resultItem = if (config.getKeys(path).contains("food")) {
            FoodBuilder.builder(item).apply {
                // i wonder why they split food into two components?
                hunger(
                    config.getInt("$path.food.hunger") ?: 1,
                    config.getDouble("$path.food.saturation")?.toFloat() ?: 2.0f,
                    config.getDouble("$path.food.eatTime")?.toFloat()
                )

                // always edible
                config.getBoolean("$path.food.isSnack")?.takeIf { it }?.let { isSnack() }

                // tp effect, similar to chorus fruit
                config.getDouble("$path.food.tp").takeIf { it != 0.0 }?.let { range -> tp(range.toFloat()) }

                // after eat sound (doesnt work for some reason)
                config.getString("$path.food.sound.burp")?.let { afterEatSound(it.lowercase()) }

                // sound while monchin and cronchin
                config.getString("$path.food.sound.eat")?.let { eatSound(it.lowercase()) }

                // should we show eat particles?
                config.getBoolean("$path.food.crumbs")?.takeIf { !it }?.let { noCrumbs() }

                // should it remove every effect?
                config.getBoolean("$path.food.isMilk")?.takeIf { it }?.let { clearEffects() }

                // add potion effects
                for (effectString in config.getStringList("$path.food.effects")) {
                    val parts = effectString.split("/")
                    // EFFECT/LEVEL/DURATION/CHANCE
                    val effectName = parts[0].lowercase()
                    val level = parts[1].toInt() - 1 // level 1 is actually level 2
                    val chance = parts.getOrNull(3)?.toFloatOrNull() ?: 1f

                    val effectType = Registry.POTION_EFFECT_TYPE[NamespacedKey.minecraft(effectName)] ?: continue

                    addEffect(effectType, parts[2], level, chance)
                }


                // eating animation (i love this)
                config.getString("$path.food.animation")?.let { animationName ->
                    val animation =
                        enumValues<ItemUseAnimation>().firstOrNull { it.name.equals(animationName, ignoreCase = true) }
                    animation?.let { animation(it) }
                }

            }.build()
        } else { item }

        resolvedResults[path.lowercase()] = item
        return resultItem
    }

    fun loadItems(config: FoxConfig) {
        for (key in config.getKeys()) {
            createItem(config, key)
        }
    }
}