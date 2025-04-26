package co.akoot.plugins.edulis.listeners.handlers

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.Edulis.Companion.leafConfig
import co.akoot.plugins.edulis.util.Materials.getMaterial
import co.akoot.plugins.edulis.util.Materials.matches
import co.akoot.plugins.plushies.util.Items.customItems
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
        return key(key)
    }

    fun dropItems(block: Block, amount: Int = 1, removePDC: Boolean = false, setAge: Boolean = false): Boolean {
        if (block.type.matches(Material.CAKE)) return false

        // get the id of the item from the block pdc
        val id = block.chunk.getPDC<String>(getBlockPDC(block.location))
            ?: return false

        val loc = block.location

        // does the item exist?
        val item = customItems[id] ?: return false

        item.amount = amount

        // for crops set amount to 3 if fully grown or else 1
        if (block.type in Tag.CROPS.values) {
            val ageable = block.blockData as? Ageable ?: return false
            item.amount = if (ageable.age != ageable.maximumAge) 1 else 3
        }

        // for sweet berry bush set amount depending on age
        else if (block.type.matches(Material.SWEET_BERRY_BUSH)) {
            val ageable = block.blockData as Ageable
            item.amount = if (ageable.age == 2) 2 else if (ageable.age == 3) 3 else 1
        }

        if (setAge) { // set crop age to 1 if asked
            val ageable = block.blockData as Ageable
            ageable.age = 1
            block.blockData = ageable
        }

        loc.world.dropItemNaturally(loc.add(0.5, 0.8, 0.5), item)

        // only remove if asked, since this is used for harvesting as well as block break
        if (removePDC) block.chunk.removePDC(getBlockPDC(loc))

        return true
    }

    fun isLeaf(block: Block): Boolean {
        return Tag.LEAVES.isTagged(block.type)
    }

    fun leafDrops(block: Block) {
        val key = block.type.name
        if (leafConfig.getKeys().contains(key)) {

            for (ingredient in leafConfig.getStringList(key)) {
                val parts = ingredient.split("/")
                val chance = parts.getOrNull(1)?.toFloat() ?: 0.05f
                val material = getMaterial(parts[0]) ?: continue // skip if input is invalid

                // nice!, add item to drop
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    runLater(1) { block.world.dropItemNaturally(block.location.add(0.5,0.0,0.5), material) }
                }
            }
        }
    }
}