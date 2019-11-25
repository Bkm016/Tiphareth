package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackGenerator
import ink.ptms.tiphareth.pack.PackDispatcher
import ink.ptms.tiphareth.pack.PackUploader
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.SubCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @Author sky
 * @Since 2019-11-25 14:55
 */
@BaseCommand(name = "tiphareth", aliases = ["th"], permission = "tiphareth.admin")
class TipharethCommand : BaseMainCommand() {

    @SubCommand(priority = 0.0, description = "获取资源包")
    fun refresh(sender: CommandSender, args: Array<String>) {
        if (sender is Player) {
            PackDispatcher.send(sender)
        }
    }

    @SubCommand(priority = 0.0, description = "生成资源包")
    fun generate(sender: CommandSender, args: Array<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(Tiphareth.getPlugin(), Runnable {
            sender.sendMessage("§c[Tiphareth] §7正在生成...")
            PackGenerator.generate(Tiphareth.PACK_ITEMS)
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
}