package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackGenerator
import ink.ptms.tiphareth.pack.PackDispatcher
import ink.ptms.tiphareth.pack.PackLoader
import ink.ptms.tiphareth.pack.PackUploader
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.module.command.base.*
import io.izzel.taboolib.module.lite.SimpleIterator
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @Author sky
 * @Since 2019-11-25 14:55
 */
@BaseCommand(name = "tiphareth", aliases = ["th"], permission = "tiphareth.admin")
class TipharethCommand : BaseMainCommand() {

    @SubCommand(priority = 0.0)
    val get = object : BaseSubCommand() {

        override fun getType(): CommandType = CommandType.PLAYER

        override fun getArguments(): Array<Argument> = arrayOf(Argument("物品") { PackLoader.items.map { it.getPackName() }.toList() })

        override fun getDescription(): String = "获取物品"

        override fun onCommand(sender: CommandSender?, p1: Command?, p2: String?, args: Array<out String>?) {
            val pack = PackLoader.getByName(args!![0]) ?: return
            CronusUtils.addItem(sender as Player, pack.buildItem())
        }
    }

    @SubCommand(priority = 0.001, description = "所有物品", arguments = ["过滤?"], type = CommandType.PLAYER)
    fun list(sender: CommandSender, args: Array<String>) {
        TipharethAPI.openMenu(sender as Player, args.getOrNull(0), 0)
    }

    @SubCommand(priority = 0.01, description = "重载配置")
    fun reload(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7正在重载...")
            Tiphareth.reloadPack()
            sender.sendMessage("§c[Tiphareth] §7生成成功.")
            sender.sendMessage("§c[Tiphareth] §7结束.")
        })
    }

    @SubCommand(priority = 0.1, description = "获取资源包")
    fun refresh(sender: CommandSender, args: Array<String>) {
        if (sender is Player) {
            PackDispatcher.send(sender)
        }
    }

    @SubCommand(priority = 0.2, description = "生成资源包")
    fun generate(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7正在生成...")
            PackGenerator.generate(PackLoader.items)
            sender.sendMessage("§c[Tiphareth] §7生成成功.")
            sender.sendMessage("§c[Tiphareth] §7生成上传...")
            if (PackUploader.upload(PackGenerator.file())) {
                sender.sendMessage("§c[Tiphareth] §7上传成功.")
            } else {
                sender.sendMessage("§c[Tiphareth] §7上传失败.")
            }
            sender.sendMessage("§c[Tiphareth] §7结束.")
        })
    }

    @SubCommand(priority = 0.3, description = "生成资源素材")
    fun generateItem(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7正在生成...")
            PackGenerator.generateNoModels()
            sender.sendMessage("§c[Tiphareth] §7生成成功.")
            sender.sendMessage("§c[Tiphareth] §7结束.")
        })
    }
}