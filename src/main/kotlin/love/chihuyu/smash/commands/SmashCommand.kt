package love.chihuyu.smash.commands

import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import love.chihuyu.timerapi.timer.Timer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object SmashCommand : Command("smash") {
    override fun onCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player || !sender.hasPermission("smash.command.smash") || args.size > 2) return

        when (args[0]) {
            "start" -> {
                gameTimer = Timer("Smash-Game", SmashPlugin.config.getLong("gameDuration"), 20, 0)
            }
            "end" -> {}
        }
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("start", "end")
            2 -> mapsConfig.getConfigurationSection("maps").getKeys(false).toList()
            else -> emptyList()
        }
    }
}
