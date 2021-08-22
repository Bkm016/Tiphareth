package ink.ptms.tiphareth

import ink.ptms.tiphareth.pack.PackDispatcher
import ink.ptms.tiphareth.pack.PackGenerator
import ink.ptms.tiphareth.pack.PackLoader
import ink.ptms.tiphareth.pack.PackUploader
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.library.xseries.parseToMaterial
import taboolib.platform.util.giveItem

@CommandHeader(name = "tiphareth", aliases = ["th"], permission = "tiphareth.admin")
object TipharethCommand {

    @CommandBody
    val get = subCommand {
        dynamic {
            suggestion<Player> { _, _ ->
                PackLoader.items.map { it.getPackName() }.toList()
            }
            execute<Player> { sender, _, argument ->
                val pack = PackLoader.getByName(argument)
                if (pack != null) {
                    sender.giveItem(pack.buildItem())
                }
            }
        }
    }

    @CommandBody
    val list = subCommand {
        dynamic(optional = true) {
            execute<Player> { sender, _, argument ->
                TipharethItemList.openMenu(sender, Coerce.toInteger(argument))
            }
        }
        execute<Player> { sender, _, _ ->
            TipharethItemList.openMenu(sender, 0)
        }
    }

    @CommandBody
    val sublist = subCommand {
        // texture
        dynamic {
            suggestion<Player> { _, _ ->
                Material.values().map { it.name }
            }
            execute<Player> { sender, _, argument ->
                TipharethItemList.openMenu(sender, argument.parseToMaterial(), 0)
            }
            // page
            dynamic(optional = true) {
                execute<Player> { sender, context, argument ->
                    TipharethItemList.openMenu(sender, context.argument(-1)!!.parseToMaterial(), Coerce.toInteger(argument))
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                sender.sendMessage("§c[Tiphareth] §7正在重载...")
                Tiphareth.reloadPack()
                sender.sendMessage("§c[Tiphareth] §7重载成功.")
                sender.sendMessage("§c[Tiphareth] §7结束.")
            }
        }
    }

    @CommandBody
    val refresh = subCommand {
        execute<Player> { sender, _, _ ->
            PackDispatcher.send(sender)
        }
    }

    @CommandBody
    val refreshAll = subCommand {
        execute<CommandSender> { sender, _, _ ->
            Bukkit.getOnlinePlayers().forEach {
                PackDispatcher.send(it)
            }
        }
    }

    @CommandBody
    val generate = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
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
            }
        }
    }

    @CommandBody
    val generateItem = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                sender.sendMessage("§c[Tiphareth] §7正在生成...")
                PackGenerator.generateNoModels()
                sender.sendMessage("§c[Tiphareth] §7生成成功.")
                sender.sendMessage("§c[Tiphareth] §7结束.")
            }
        }
    }
}