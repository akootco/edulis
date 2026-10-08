package co.akoot.plugins.edulis.blocks

import co.akoot.plugins.bluefox.extensions.getMeta
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setMeta
import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.plushies.Plushies.Companion.key
import co.akoot.plugins.plushies.api.Interactable
import co.akoot.plugins.plushies.api.Storage
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.BoundingBox
import org.joml.Vector3f

object CookingPot : Interactable {

    private val heatSources = setOf(Material.CAMPFIRE, Material.SOUL_CAMPFIRE)
    override val key = key("cooking_pot")
    override fun translation() = Vector3f(0f, 0.5f, 0f)
    override val cancelPlacement = true
    override val useInteractionPoint = true
    override val removable = true
    override fun width() = 0.75f
    override fun height() = 0.5f
    override val placeSound = "block.decorated_pot.place"
    override val breakSound = "block.decorated_pot.break"
    override fun item() = ItemBuilder.builder(Material.POISONOUS_POTATO)
        .itemName("Cooking Pot".text)
        .stackSize(1)
        .customModelData("cooking_pot")
        .unsetData(DataComponentTypes.CONSUMABLE)
        .unsetData(DataComponentTypes.FOOD)
        .pdc(key)
        .build()

    init {
        CraftRecipe.builder("cooking_pot", item())
            .ingredient(Material.CAULDRON)
            .shapeless()
    }

    override fun interact(entity: Entity, player: Player) {
        player.openInventory(CookingPotGUI.get(entity).inventory)
    }

    override fun place(event: PlayerInteractEvent): Boolean {
        val clicked = event.clickedBlock ?: return false

        if (clicked.type !in heatSources || event.blockFace != BlockFace.UP) return false
        if (clicked.world.getNearbyEntities(BoundingBox.of(clicked)).any { it.hasPDC(key) }) return false
        return super.place(event)
    }

    override fun remove(entity: Entity, damager: Entity?): Boolean {
        CookingPotGUI.get(entity).remove()
        return super.remove(entity, damager)
    }
}

private class CookingPotGUI(override val entity: Entity) : Storage(
    Component.text("Cooking Pot"),
    KEY,
    3,
    setOf(1, 2, 3, 10, 11, 12, 23, 25)
) {
    companion object {
        private const val KEY = "cook.pot.inv"

        fun get(entity: Entity): CookingPotGUI {
            return entity.getMeta<CookingPotGUI>(KEY)
                ?: CookingPotGUI(entity).also {
                    entity.setMeta(KEY, it)
                    it.loadContents()
                }
        }
    }
}