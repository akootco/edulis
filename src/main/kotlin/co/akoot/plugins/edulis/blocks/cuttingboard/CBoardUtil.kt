package co.akoot.plugins.edulis.blocks.cuttingboard

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.util.Materials
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.entity.GlowItemFrame
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

val cbkey = Edulis.key("cutting_board")

data class CBoardRecipes(
    val tool: Material,
    val input: String,
    val results: MutableSet<ItemStack>
)

val cuttingBoardRecipes = mutableListOf<CBoardRecipes>()

val ItemStack.isTool: Boolean
    get() = Tag.ITEMS_BREAKS_DECORATED_POTS.isTagged(this.type)

fun createCuttingBoards() {
    Tag.PLANKS.values.forEach { material ->
        val woodType = material.name.lowercase().removeSuffix("_planks")
        val id = woodType + "_cutting_board"
        val displayName = woodType.split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

        customItems[id] = ItemBuilder.builder(Material.GLOW_ITEM_FRAME)
            .itemName(Text("$displayName Cutting Board").component)
            .pdc(cbkey, woodType)
            .pdc(foodKey, id)
            .customModelData(id)
            .build()
    }
}

fun getDisplayTransform(item: ItemStack): Transformation {
    val asItem = Transformation(
        Vector3f(0f, 0f, -0.46f),
        AxisAngle4f(),
        Vector3f(0.501f, 0.501f, 0.501f),
        AxisAngle4f()
    )
    val asTool = Transformation(
        Vector3f(-.2f, 0f, -0.15f),
        AxisAngle4f(Math.toRadians(-90.0).toFloat(), 1f, 0f, -.2f),
        Vector3f(.8f, .8f, .8f),
        AxisAngle4f()
    )
    return if (item.isTool) asTool else asItem
}

fun cutItem(location: Location, item: ItemStack, tool: ItemStack, board: GlowItemFrame): Boolean {
    val recipe = cuttingBoardRecipes.firstOrNull { r ->
        Materials.getInput(r.input, item.type.name)?.test(item) == true &&
                r.tool == tool.type // wtf. aint no way
    }

    if (recipe != null) {
        location.world.playSound(location, Sound.ITEM_SPEAR_WOOD_ATTACK, 0.5f, 2.0f)
        recipe.results.forEach { location.world.dropItemNaturally(location, it.clone()) }
        return clearBoard(board)
    }

    return giveSlice(item, location) && clearBoard(board)
}

fun clearBoard(board: GlowItemFrame) : Boolean{
    board.setItem(null)
    board.passengers.firstOrNull()?.remove()
    return true
}

fun giveSlice(item: ItemStack, location: Location): Boolean {
    val cakeId = item.itemMeta?.getPDC<String>(foodKey)
        ?: when (item.type) {
            Material.CAKE, Material.PUMPKIN_PIE -> item.type.name.lowercase()
            else -> return false
        }

    val cakeSlice = customItems["${cakeId}_slice"] ?: return false

    cakeSlice.amount = when {
        cakeId.endsWith("cake") -> 8
        cakeId == "pizza" -> 12
        else -> 4
    }

    location.world.playSound(location, Sound.ITEM_SPEAR_WOOD_ATTACK, 0.5f, 2.0f)
    location.world.dropItemNaturally(location.toCenterLocation(), cakeSlice)

    return true
}