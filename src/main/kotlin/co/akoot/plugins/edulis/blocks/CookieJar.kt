package co.akoot.plugins.edulis.blocks

import co.akoot.plugins.bluefox.extensions.getMeta
import co.akoot.plugins.bluefox.extensions.setMeta
import co.akoot.plugins.bluefox.extensions.withAmount
import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.plushies.Plushies.Companion.key
import co.akoot.plugins.plushies.api.Interactable
import co.akoot.plugins.plushies.api.Storage
import co.akoot.plugins.plushies.api.openMenu
import co.akoot.plugins.plushies.util.Recipes.tag
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.joml.Vector3f

object CookieJar : Interactable {

    override val key = key("cookie_jar")
    override fun translation() = Vector3f(0f, 0.5f, 0f)
    override val pushable = true
    override val rotatable = true
    override val removable = true
    override fun width() = 0.5f
    override fun height() = 0.57f
    override val placeSound = "block.glass.place"
    override val breakSound = "block.glass.break"
    override fun item() = ItemBuilder.builder(Material.POISONOUS_POTATO)
        .itemName("Cookie Jar".text)
        .stackSize(16)
        .itemModel("edulis:cookie_jar")
        .unsetData(DataComponentTypes.CONSUMABLE)
        .unsetData(DataComponentTypes.FOOD)
        .pdc(key)
        .build()

    init {
        tag("planks")?.let {
            CraftRecipe.builder("cookie_jar", item().withAmount(3))
                .ingredient('a', it)
                .ingredient('b', Material.GLASS)
                .shape("a", "b")
                .shaped("edulis")
        }
    }

    override fun interact(entity: Entity, player: Player) {
        player.openMenu(CookieJarGUI.get(entity))
    }

    override fun place(event: PlayerInteractEvent): Boolean {
        if (event.blockFace != BlockFace.UP || event.clickedBlock?.getRelative(BlockFace.UP)?.type != Material.AIR) return false
        return super.place(event)
    }

    override fun remove(entity: Entity, damager: Entity?): Boolean {
        CookieJarGUI.get(entity).remove()

        val display = entity.vehicle as? ItemDisplay
        display?.setItemStack(item())

        return super.remove(entity, damager)
    }
}

private class CookieJarGUI(override val entity: Entity) :
    Storage(Component.text("Cookie Jar"), KEY,StorageConfig(InventoryType.HOPPER)) {

    override val openSound = StorageSound("block.decorated_pot.insert")
    override val closeSound = StorageSound("block.decorated_pot.insert_fail")


    override fun onClose(event: InventoryCloseEvent) {
        val filledSlots = inventory.contents.take(5).count { it != null }.toFloat()
        val display = entity.vehicle as? ItemDisplay
            ?: return super.onClose(event)

        display.setItemStack(display.itemStack.apply {
            setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addFloat(filledSlots).build()
            )
        })

        super.onClose(event)
    }

    companion object {
        private const val KEY = "cookie.jar.inv"

        fun get(entity: Entity): CookieJarGUI {
            return entity.getMeta<CookieJarGUI>(KEY)
                ?: CookieJarGUI(entity).also {
                    entity.setMeta(KEY, it)
                    it.loadContents()
                }
        }
    }
}