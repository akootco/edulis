package co.akoot.plugins.edulis.listeners.tasks

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.scheduler.BukkitRunnable

class CropDisplay(private val block: Block) : BukkitRunnable() {
    override fun run() {
        val id = block.location.chunk.getPDC<String>(getBlockPDC(block.location, "edulis")) ?: return
        // Refresh the block
        val updatedState = block.state
        val updatedBlockData = updatedState.blockData as? Ageable ?: return
        // Get the new age
        val newAge = updatedBlockData.age
        // Retrieve the ID from PDC
        createDisplay(block.location, newAge, id)
    }
}