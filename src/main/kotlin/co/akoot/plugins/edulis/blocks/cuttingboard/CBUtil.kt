package co.akoot.plugins.edulis.blocks.cuttingboard

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.util.Util.foodid
import co.akoot.plugins.plushies.util.Items.getItem
import co.akoot.plugins.plushies.util.Items.registerItem
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

val cbkey = Edulis.key("cutting_board")

val ItemStack.isTool: Boolean
    get() = Tag.ITEMS_BREAKS_DECORATED_POTS.isTagged(this.type)

fun createCuttingBoards() {
    Tag.PRESSURE_PLATES.values.forEach { material ->
        val type = material.name.lowercase().removeSuffix("_pressure_plate") + "_cutting_board"

        val model = type
            .replace("light_weighted", "gold")
            .replace("heavy_weighted", "iron")

        val name = model.split("_")
            .joinToString(" ") {
                it.replaceFirstChar(Char::uppercase)
            }.text

        registerItem(
            type,
            ItemBuilder.builder(Material.GLOW_ITEM_FRAME)
                .itemName(name)
                .pdc(cbkey, type)
                .pdc(foodKey, type)
                .customModelData(model)
                .build()
        )
    }
}

fun getDisplayTransform(item: ItemStack): Transformation {
    val asItem = Transformation(
        Vector3f(0f, 0.06f, -0.465f),
        AxisAngle4f(),
        Vector3f(0.501f),
        AxisAngle4f()
    )
    val asBlock = Transformation(
        Vector3f(0f, 0f, -0.36f),
        AxisAngle4f(),
        Vector3f(0.501f),
        AxisAngle4f()
    )
    val asTool = Transformation(
        Vector3f(-.2f, 0f, -0.15f),
        AxisAngle4f(Math.toRadians(-90.0).toFloat(), 1f, 0f, -.2f),
        Vector3f(.8f),
        AxisAngle4f()
    )

    return when {
        item.isTool -> asTool
        item.type.isBlock -> asBlock
        else -> asItem
    }
}

fun cutItem(player: Player, location: Location, item: ItemStack, tool: ItemStack, board: GlowItemFrame): Boolean {
    val fixedLoc = location.add(0.0,.5,0.0)
    val recipe = cuttingBoardRecipes.firstOrNull { r ->
        r.inputs.size == 1 && r.inputs[0].test(item) && (r.tool?.test(tool) ?: true) //lmao
    }

    if (recipe != null) {
        recipe.results.forEach {
            fixedLoc.world.dropItemNaturally(location, it.clone()) }

        if (recipe.tool != null) {
            tool.damage(1, player)
        }

        return clearBoard(board)
    }

    if (tool.foodid == "butcher_knife") {
        val sliced = giveSlice(item, location)
        if (sliced) tool.damage(1, player)
        return sliced && clearBoard(board)
    }
    return false
}

fun clearBoard(board: GlowItemFrame) : Boolean{
    board.setItem(null)
    board.passengers.firstOrNull()?.remove()
    board.location.world.playSound(board.location, Sound.ITEM_SPEAR_WOOD_USE, 0.8f, 1.5f)
    return true
}

fun giveSlice(item: ItemStack, location: Location): Boolean {
    val cakeId = item.itemMeta?.getPDC<String>(foodKey)
        ?: when (item.type) {
            Material.CAKE, Material.PUMPKIN_PIE -> item.type.name.lowercase()
            else -> return false
        }

    val cakeSlice = getItem("${cakeId}_slice") ?: return false

    cakeSlice.amount = when {
        cakeId == "pizza" -> 12
        item.type == Material.CAKE -> 8
        else -> 4
    }

    location.world.dropItemNaturally(location.toCenterLocation(), cakeSlice)

    return true
}