package co.akoot.plugins.edulis.listeners.handlers

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import java.util.concurrent.ThreadLocalRandom

object BlockDrops {

    fun getBlockPDC(location: Location): NamespacedKey {
        val key = "${location.world.name}.${location.blockX}.${location.blockY}.${location.blockZ}"
        return NamespacedKey("edulis", key)
    }

    fun dropItems(block: Block, amount: Int = 1, removePDC: Boolean = true): Boolean {
        if (block.type == Material.CAKE) return false

        // get the id of the item from the block pdc
        val id = block.chunk.getPDC<String>(getBlockPDC(block.location))
            ?: return false

        val loc = block.location

        // does the item exist?
        val item = resolvedResults[id] ?: return false

        item.amount = amount

        // for crops set amount to 3 if fully grown or else 1
        if (block.type in Tag.CROPS.values) {
            val ageable = block.blockData as? Ageable ?: return false
            item.amount = if (ageable.age != ageable.maximumAge) 1 else 3
        }

        // for sweet berry bush set amount depending on age
        else if (block.type == Material.SWEET_BERRY_BUSH) {
            val ageable = block.blockData as? Ageable ?: return false
            item.amount = if (ageable.age == 2) 2 else if (ageable.age == 3) 3 else 1
        }

        loc.world.dropItemNaturally(loc.add(0.5, 0.5, 0.5), item)

        // only remove if asked, since this is used for harvesting as well as block break
        if (removePDC) block.chunk.removePDC(getBlockPDC(loc))

        return true
    }

    fun isLeaf(block: Block): Boolean {
        return block.type.name.endsWith("_LEAVES")
    }

    fun leafDrops(block: Block) {
        if (block.type == Material.CHERRY_LEAVES && ThreadLocalRandom.current().nextDouble() < 0.05) {
            resolvedResults["cherries"]?.let { block.world.dropItemNaturally(block.location, it) }
        }
    }
}