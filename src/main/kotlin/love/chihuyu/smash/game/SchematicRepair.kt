package love.chihuyu.smash.game

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.session.ClipboardHolder
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import love.chihuyu.smash.SmashPlugin.Companion.prefix
import org.bukkit.ChatColor
import java.io.File
import java.io.FileInputStream

object SchematicRepair {

    fun recovery(map: String) {
        val file = File(SmashPlugin.dataFolder, "../WorldEdit/schematics/$map.schematic")
        val format = ClipboardFormat.findByFile(file)
        val world = SmashPlugin.server.worlds[0]
        val clipboard = try {
            format!!.getReader(FileInputStream(file)).read(BukkitWorld(SmashPlugin.server.worlds[0]).worldData)
        } catch (e: Throwable) {
            SmashPlugin.logger.info("$prefix ${ChatColor.RED}スキーマファイルの読み込みに失敗しました")
            return
        }.apply {
            val vector = mapsConfig.getVector("maps.$map.center")
            origin = Vector(vector.x, vector.y, vector.z)
        }
        val worldedit = SmashPlugin.server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin
        ClipboardHolder(clipboard, BukkitWorld(world).worldData)
            .createPaste(
                worldedit.createEditSession(
                    SmashPlugin.server.operators.toList()[0].player
                ),
                BukkitWorld(world).worldData
            ).apply {
                ignoreAirBlocks(true)
            }
            .build()
    }
}
