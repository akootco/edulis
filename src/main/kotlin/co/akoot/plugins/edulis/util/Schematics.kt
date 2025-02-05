package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.log
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import java.io.File

object Schematics {

    fun registerSchematics(plugin: FoxPlugin) {

        val directory = File(plugin.dataFolder, "schematics")

        if (!directory.exists()) {
            directory.mkdirs()
            log.info("schematics folder created")
        }

        // loop through each file in the folder and register the structure
        directory.listFiles()?.forEach { file ->
            if (file.extension == ".nbt") {

                val manager = Bukkit.getStructureManager()
                val key = NamespacedKey("edulis", file.nameWithoutExtension)

                manager.apply {
                    unregisterStructure(key)
                    registerStructure(key, manager.loadStructure(file))
                }
            }
        }
    }
}