package love.chihuyu.smash.commands

import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.prefix
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object SmashConfigCommand: Command("smashconfig") {
    override fun onCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player || !sender.hasPermission("smash.command.smashconfig") || args.isEmpty()) return

        when (args[0]) {
            "lobby-spawn" -> {
                sender.sendMessage(
                    try {
                        SmashPlugin.config.set("lobby-spawn", sender.location.toVector())
                        "$prefix ロビーのスポーン地点を${sender.location.toVector()}に設定しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}設定の変更に失敗しました"
                    }
                )
                SmashPlugin.saveConfig()
            }
            "nodelay" -> {
                if (args.size < 2) {
                    sender.sendMessage("$prefix NoDelayの値は${SmashPlugin.config.getBoolean("nodelay")}です")
                } else {
                    sender.sendMessage(
                        try {
                            SmashPlugin.config.set("nodelay", args[1].toBooleanStrict())
                            "$prefix NoDelayを${args[1]}に設定しました"
                        } catch (e: Throwable) {
                            "$prefix ${ChatColor.RED}設定の変更に失敗しました"
                        }
                    )
                    SmashPlugin.saveConfig()
                }
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String>? {
        return when (args.size) {
            1 -> {
                listOf("lobby-spawn", "nodelay")
            }
            else -> emptyList()
        }
    }
}