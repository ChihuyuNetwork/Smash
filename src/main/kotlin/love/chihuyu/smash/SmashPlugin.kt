package love.chihuyu.smash

import love.chihuyu.smash.listener.EventCanceller
import org.bukkit.plugin.java.JavaPlugin

class SmashPlugin: JavaPlugin() {
    companion object {
        lateinit var SmashPlugin: JavaPlugin
    }

    init {
        SmashPlugin = this
    }

    override fun onEnable() {
        super.onEnable()

        listOf(
            EventCanceller
        ).forEach { server.pluginManager.registerEvents(it, this) }
    }
}