package co.akoot.plugins.edulis.blocks.cookingpot

import co.akoot.plugins.bluefox.extensions.getMeta
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setMeta
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.plushies.Plushies.Companion.key
import co.akoot.plugins.plushies.api.Interactable
import co.akoot.plugins.plushies.api.Menu
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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

private class CookingPotGUI(private val entity: Entity) : Menu {

    private val inventory = Bukkit.createInventory(this, 27,
        Component.text("Cooking Pot")
    )

    companion object {
        private val openSlots = setOf(1, 2, 3, 10, 11, 12, 23, 25)
        private const val KEY = "cook.pot.inv"

        private val filler = ItemBuilder.builder(ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE))
            .itemName(Component.empty())
            .itemModel("slot")
            .hideTooltip()
            .build()

        fun get(entity: Entity): CookingPotGUI {
            return entity.getMeta<CookingPotGUI>(KEY)
                ?: CookingPotGUI(entity).also {
                    entity.setMeta(KEY, it)
                    it.load()
                }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = event.rawSlot !in openSlots
    }

    override fun onClose(event: InventoryCloseEvent) {
        if (inventory.viewers.isNotEmpty()) return
        entity.setPDC(key(KEY), ItemStack.serializeItemsAsBytes(inventory.contents))
    }

    private fun load() {
        entity.getPDC<ByteArray>(key(KEY))?.let {
            inventory.contents = ItemStack.deserializeItemsFromBytes(it)
        }

        for (slot in 0 until inventory.size) {
            if (slot !in openSlots) {
                inventory.setItem(slot, filler)
            }
        }
    }

    fun remove() {
        inventory.viewers.forEach { it.closeInventory() }

        for (item in inventory.contents.filterNotNull()) {
            if (item.isSimilar(filler)) continue
            entity.world.dropItemNaturally(entity.location, item)
        }
    }

    override fun getInventory(): Inventory = inventory
}