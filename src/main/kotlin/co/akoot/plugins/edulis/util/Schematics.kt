package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.log
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import java.io.File
import java.util.*

object Schematics {

    fun registerSchematics(plugin: FoxPlugin) {

        val directory = File(plugin.dataFolder, "schematics")

        if (!directory.exists()) {
            directory.mkdirs()
            log.info("schematics folder created")
        }

        // loop through each file in the folder and register the structure
        directory.listFiles()?.forEach { file ->

            val manager = Bukkit.getStructureManager()
            val key = NamespacedKey("edulis", file.nameWithoutExtension)

            manager.apply {
                registerStructure(key, manager.loadStructure(file))
            }
        }
    }

    fun paste(value: String, location: Location): Boolean {
        val structureManager = Bukkit.getStructureManager()
        val key = NamespacedKey("edulis", value)

        val structure = structureManager.getStructure(key) ?: return false

        val size = structure.size

        // center structure based on size
        val center = location.clone().add(-(size.x / 2.0), 0.0, -(size.z / 2.0))

        structure.place(
            center.add(0.5, 0.0, 0.5),
            true,
            StructureRotation.NONE,
            Mirror.NONE,
            0,
            1.0f,
            Random()
        )

        return true
    }
}