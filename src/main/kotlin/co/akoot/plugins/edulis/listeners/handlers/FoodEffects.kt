package co.akoot.plugins.edulis.listeners.handlers

import co.akoot.plugins.bluefox.extensions.getPDC
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object FoodEffects {

    fun setAttributes(item: ItemStack, player: Player) {
        val attributes = item.itemMeta.getPDC<String>(NamespacedKey("edulis", "attributes")) ?: return
        val attributeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE)

        // name/value;name/value;name/value
        attributes.split(";").forEach {
            val (name, value) = it.split("/")

            // make sure the attribute exists
            val attribute = attributeRegistry[NamespacedKey.minecraft(name.lowercase())] ?: return
            val att = player.getAttribute(attribute) ?: return // return if the player doesn't have the attribute

            // set the value
            att.baseValue += value.toDouble()
        }
    }
}