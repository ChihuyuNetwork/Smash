package love.chihuyu.smash.commands

import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import love.chihuyu.smash.SmashPlugin.Companion.mapsFile
import love.chihuyu.smash.SmashPlugin.Companion.prefix
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object SmashMapCommand : Command("smashmap") {
    override fun onCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player || !sender.hasPermission("smash.command.smashconfig") || args.isEmpty()) return
        val section = mapsConfig.getConfigurationSection("maps") ?: mapsConfig.createSection("maps")

        when (args[0]) {
            "list" -> {
                sender.sendMessage("$prefix マップ一覧:\n${section.getKeys(false).joinToString("\n")}")
            }
            "spawns" -> {
                sender.sendMessage(
                    try {
                        val list = (section.getConfigurationSection(args[1]).getList("spawns") as List<Vector>)
                        "$prefix ${args[1]}のスポーン地点一覧:\n${list.map { "${list.indexOf(it)}: $it\n" }}"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください"
                    }
                )
            }
            "create" -> {
                sender.sendMessage(
                    try {
                        section.createSection(args[1])
                        "$prefix マップを追加しました: \"${args[1]}\""
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}マップの追加に失敗しました"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "remove" -> {
                sender.sendMessage(
                    try {
                        section.set(args[1], null)
                        "$prefix マップを削除しました: \"${args[1]}\""
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}マップの削除に失敗しました"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "addspawn" -> {
                sender.sendMessage(
                    try {
                        val map = section.getConfigurationSection(args[1])
                        val list = (map.getList("spawns") as List<Vector>)
                        map.set("spawns", list.plus(sender.location.toVector()))
                        "$prefix スポーン地点をマップに追加しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "removespawn" -> {
                sender.sendMessage(
                    try {
                        val map = section.getConfigurationSection(args[1])
                        val list = (map.getList("spawns") as List<Vector>).toMutableList()
                        list.removeAt(Integer.parseInt(args[2]))
                        map.set("spawns", list)
                        "$prefix スポーン地点をマップから削除しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください"
                    }
                )
                mapsConfig.save(mapsFile)
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        val section = mapsConfig.getConfigurationSection("maps") ?: mapsConfig.createSection("maps")
        return when (args.size) {
            1 -> listOf("list", "spawns", "create", "remove", "addspawn", "removespawn")
            2 -> when (args[0]) {
                "spawns", "remove", "addspawn", "removespawn" -> section.getKeys(false).toList()
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
