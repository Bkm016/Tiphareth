package ink.ptms.tiphareth;

import com.google.common.collect.Maps;
import ink.ptms.tiphareth.pack.*;
import io.izzel.taboolib.cronus.CronusUtils;
import io.izzel.taboolib.module.lite.SimpleIterator;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.item.inventory.MenuBuilder;
import io.izzel.taboolib.util.lite.Numbers;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2019-11-26 7:29
 */
public class TipharethAPI {

    public static final PackLoader LOADER = PackLoader.INSTANCE;
    public static final PackUploader UPLOADER = PackUploader.INSTANCE;
    public static final PackGenerator GENERATOR = PackGenerator.INSTANCE;
    public static final PackDispatcher DISPATCHER = PackDispatcher.INSTANCE;

    public static void openMenu(Player player, String search, int page) {
        Map<Integer, PackObject> objectsMap = Maps.newHashMap();
        List<PackObject> objects = LOADER.getItems().stream().filter(i -> !i.isHide() && (search == null || i.getPackName().contains(search))).collect(Collectors.toList());
        List<PackObject> objectsSorted = new SimpleIterator(objects).listIterator(page * 28, (page + 1) * 28);
        Inventory inventory = MenuBuilder.builder()
                .title("Tiphareth Items - " + (page + 1) + " (search: " + (search == null ? "*" : search) + ")")
                .rows(6)
                .event(e -> {
                    e.setCancelled(true);
                    if (e.getClickType() == ClickType.CLICK) {
                        if (e.getRawSlot() == 47 && e.castClick().getCurrentItem().getType() == Material.SPECTRAL_ARROW) {
                            openMenu(player, search, page - 1);
                        } else if (e.getRawSlot() == 51 && e.castClick().getCurrentItem().getType() == Material.SPECTRAL_ARROW) {
                            openMenu(player, search, page + 1);
                        } else if (objectsMap.containsKey(e.getRawSlot())) {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                            player.getInventory().addItem(objectsMap.get(e.getRawSlot()).buildItem());
                        }
                    }
                }).build();
        for (int i = 0; i < objectsSorted.size(); i++) {
            objectsMap.put(Items.INVENTORY_CENTER[i], objectsSorted.get(i));
            inventory.setItem(Items.INVENTORY_CENTER[i], objectsSorted.get(i).buildItem());
        }
        if (page > 0) {
            inventory.setItem(47, new ItemBuilder(Material.SPECTRAL_ARROW).name("§e上一页").build());
        } else {
            inventory.setItem(47, new ItemBuilder(Material.ARROW).name("§8上一页").build());
        }
        if (CronusUtils.next(page, objects.size(), 28)) {
            inventory.setItem(51, new ItemBuilder(Material.SPECTRAL_ARROW).name("§e下一页").build());
        } else {
            inventory.setItem(51, new ItemBuilder(Material.ARROW).name("§8下一页").build());
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 1f, 1f);
        player.openInventory(inventory);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (inventory.getViewers().isEmpty()) {
                    cancel();
                } else if (LOADER.getItems().isEmpty()) {
                    inventory.setItem(49, new ItemStack(Material.BARRIER));
                } else {
                    inventory.setItem(49, LOADER.getItems().get(Numbers.getRandom().nextInt(LOADER.getItems().size())).buildItem());
                }
            }
        }.runTaskTimer(Tiphareth.getPlugin(), 0, 10);
    }

}
