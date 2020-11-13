package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackGenerator
import ink.ptms.tiphareth.pack.PackDispatcher
import ink.ptms.tiphareth.pack.PackLoader
import ink.ptms.tiphareth.pack.PackUploader
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.module.command.base.*
import io.izzel.taboolib.module.lite.SimpleIterator
import io.izzel.taboolib.util.ArrayUtil
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.lite.Materials
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.NumberConversions

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

        override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>) {
            val pack = PackLoader.getByName(ArrayUtil.arrayJoin(args, 0)) ?: return
            CronusUtils.addItem(sender as Player, pack.buildItem())
        }
    }

    @SubCommand(priority = 0.001, description = "所有物品", arguments = ["页面?"], type = CommandType.PLAYER)
    fun list(sender: CommandSender, args: Array<String>) {
        TipharethAPI.openMenu(sender as Player, NumberConversions.toInt(args.getOrElse(0) { "0" }))
    }

    @SubCommand(priority = 0.001, description = "所有物品", arguments = ["材质?", "页面?"], type = CommandType.PLAYER)
    fun sublist(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            TipharethAPI.openMenu(sender as Player, null, 0)
        } else {
            TipharethAPI.openMenu(sender as Player, Items.asMaterial(args[0]), NumberConversions.toInt(args.getOrElse(1) { "0" }))
        }
    }

    @SubCommand(priority = 0.01, description = "重载配置")
    fun reload(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7正在重载...")
            Tiphareth.reloadPack()
            sender.sendMessage("§c[Tiphareth] §7重载成功.")
            sender.sendMessage("§c[Tiphareth] §7结束.")
        })
    }

    @SubCommand(priority = 0.1, description = "更新资源包")
    fun refresh(sender: CommandSender, args: Array<String>) {
        if (sender is Player) {
            PackDispatcher.send(sender)
        }
    }

    @SubCommand(priority = 0.1, description = "更新所有玩家的资源包")
    fun refreshAll(sender: CommandSender, args: Array<String>) {
        Bukkit.getOnlinePlayers().forEach {
            PackDispatcher.send(it)
        }
    }

    @SubCommand(priority = 0.2, description = "上传资源包")
    fun upload(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7生成上传...")
            if (PackUploader.upload(PackGenerator.file())) {
                sender.sendMessage("§c[Tiphareth] §7上传成功.")
            } else {
                sender.sendMessage("§c[Tiphareth] §7上传失败.")
            }
            sender.sendMessage("§c[Tiphareth] §7结束.")
        })
    }

    @SubCommand(priority = 0.25, description = "生成资源包")
    fun generate(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7正在重载...")
            Tiphareth.reloadPack()
            sender.sendMessage("§c[Tiphareth] §7重载成功.")
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