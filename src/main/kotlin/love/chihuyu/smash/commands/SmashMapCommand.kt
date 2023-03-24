package love.chihuyu.smash.commands

import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import love.chihuyu.smash.SmashPlugin.Companion.mapsFile
import love.chihuyu.smash.SmashPlugin.Companion.prefix
import love.chihuyu.smash.game.SchematicRepair
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
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください (${e.cause})"
                    }
                )
            }
            "create" -> {
                sender.sendMessage(
                    try {
                        section.createSection(args[1])
                        "$prefix マップを追加しました: \"${args[1]}\""
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}マップの追加に失敗しました (${e.cause})"
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
                        "$prefix ${ChatColor.RED}マップの削除に失敗しました (${e.cause})"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "addspawn" -> {
                sender.sendMessage(
                    try {
                        val map = section.getConfigurationSection(args[1])
                        val list = (map.getList("spawns") as? List<Vector> ?: mutableListOf()).toMutableList()
                        map.set("spawns", list.plus(sender.location.toVector()))
                        "$prefix スポーン地点をマップに追加しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください (${e.cause})"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "removespawn" -> {
                sender.sendMessage(
                    try {
                        val map = section.getConfigurationSection(args[1])
                        val list = (map.getList("spawns") as? List<Vector> ?: mutableListOf()).toMutableList()
                        list.removeAt(Integer.parseInt(args[2]))
                        map.set("spawns", list)
                        "$prefix スポーン地点をマップから削除しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください (${e.cause})"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "setcenter" -> {
                sender.sendMessage(
                    try {
                        val map = section.getConfigurationSection(args[1])
                        map.set("center", sender.location.toVector())
                        "$prefix マップの中心を設定しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください (${e.cause})"
                    }
                )
                mapsConfig.save(mapsFile)
            }
            "tp" -> {
                sender.sendMessage(
                    try {
                        val map = section.getConfigurationSection(args[1])
                        sender.teleport(map.getVector("center").toLocation(sender.world) ?: (map.getList("spawns") as List<Vector>).map { it.toLocation(sender.world) }[0])
                        "$prefix ${args[1]}にテレポートしました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}正しくコマンドを入力してください (${e.cause})"
                    }
                )
            }
            "recovery" -> {
                sender.sendMessage(
                    try {
                        SchematicRepair.recovery(args[1])
                        "$prefix マップを修復しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}マップの修復に失敗しました"
                    }
                )
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        val section = mapsConfig.getConfigurationSection("maps") ?: mapsConfig.createSection("maps")
        return when (args.size) {
            1 -> listOf("list", "spawns", "create", "remove", "addspawn", "removespawn", "setcenter", "tp", "recovery")
            2 -> when (args[0]) {
                "spawns", "remove", "addspawn", "removespawn", "setcenter", "tp", "recovery" -> section.getKeys(false).toList()
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
