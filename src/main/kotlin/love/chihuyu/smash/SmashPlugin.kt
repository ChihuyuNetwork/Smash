package love.chihuyu.smash

import love.chihuyu.smash.commands.Command
import love.chihuyu.smash.commands.SmashCommand
import love.chihuyu.smash.commands.SmashConfigCommand
import love.chihuyu.smash.commands.SmashMapCommand
import love.chihuyu.smash.listener.EventCanceller
import love.chihuyu.timerapi.timer.Timer
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SmashPlugin : JavaPlugin() {
    companion object {
        lateinit var SmashPlugin: JavaPlugin
        val prefix = "${ChatColor.DARK_RED}${ChatColor.BOLD}[Smash]${ChatColor.RESET}"
        lateinit var mapsFile: File
        lateinit var mapsConfig: YamlConfiguration
        lateinit var gameTimer: Timer
    }

    init {
        SmashPlugin = this
        mapsFile = File(dataFolder, "maps.yml")
    }

    override fun onLoad() {
        super.onLoad()
        if (!mapsFile.exists()) saveResource("maps.yml", false)
        mapsConfig = YamlConfiguration().apply {
            this.load(mapsFile)
        }
    }

    override fun onEnable() {
        super.onEnable()

        saveDefaultConfig()

        listOf(
            EventCanceller
        ).forEach { server.pluginManager.registerEvents(it, this) }

        listOf(
            SmashConfigCommand,
            SmashMapCommand,
            SmashCommand
        ).forEach(Command::register)
    }
}
