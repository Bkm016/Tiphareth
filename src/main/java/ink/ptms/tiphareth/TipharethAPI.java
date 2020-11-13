package ink.ptms.tiphareth;

import com.google.common.collect.Maps;
import ink.ptms.tiphareth.pack.*;
import io.izzel.taboolib.cronus.CronusUtils;
import io.izzel.taboolib.kotlin.Indexed;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
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

    public static void openMenu(Player player, int page) {
        Map<Integer, Material> objectsMap = Maps.newHashMap();
        Map<Material, List<PackObject>> objects = LOADER.getItems().stream().collect(Collectors.groupingBy(i -> i.getItem().getType()));
        List<Map.Entry<Material, List<PackObject>>> objectsSorted = Indexed.INSTANCE.subMap(objects, page * 28, (page + 1) * 28 - 1);
        MenuBuilder.builder()
                .title("Tiphareth Items - " + (page + 1))
                .rows(6)
                .event(e -> {
                    e.setCancelled(true);
                    if (e.getClickType() == ClickType.CLICK) {
                        if (e.getRawSlot() == 47 && e.castClick().getCurrentItem().getType() == Material.SPECTRAL_ARROW) {
                            openMenu(player, page - 1);
                        } else if (e.getRawSlot() == 51 && e.castClick().getCurrentItem().getType() == Material.SPECTRAL_ARROW) {
                            openMenu(player, page + 1);
                        } else if (objectsMap.containsKey(e.getRawSlot())) {
                            openMenu(player, objectsMap.get(e.getRawSlot()), 0);
                        }
                    }
                })
                .build(inventory -> {
                    for (int i = 0; i < objectsSorted.size(); i++) {
                        objectsMap.put(Items.INVENTORY_CENTER[i], objectsSorted.get(i).getKey());
                        inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(objectsSorted.get(i).getKey())
                                .name(Tiphareth.CONF.getString("group-name." + objectsSorted.get(i).getKey().name()))
                                .flags(ItemFlag.values())
                                .build());
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
                }).open(player);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 2f);
    }

    public static void openMenu(Player player, Material search, int page) {
        Map<Integer, PackObject> objectsMap = Maps.newHashMap();
        List<PackObject> objects = LOADER.getItems().stream()
                .filter(i -> search == null || i.getItem().getType().equals(search))
                .sorted(Comparator.comparing(a -> a.getItem().getType().name()))
                .collect(Collectors.toList());
        List<PackObject> objectsSorted = Indexed.INSTANCE.subList(objects, page * 28, (page + 1) * 28 - 1);
        MenuBuilder.builder()
                .title("Tiphareth Items - " + (page + 1) + " (" + (search == null ? "*" : search) + ")")
                .rows(6)
                .event(e -> {
                    e.setCancelled(true);
                    if (e.getClickType() == ClickType.CLICK) {
                        if (e.getRawSlot() == 49) {
                            openMenu(player, 0);
                        } else if (e.getRawSlot() == 47 && e.castClick().getCurrentItem().getType() == Material.SPECTRAL_ARROW) {
                            openMenu(player, search, page - 1);
                        } else if (e.getRawSlot() == 51 && e.castClick().getCurrentItem().getType() == Material.SPECTRAL_ARROW) {
                            openMenu(player, search, page + 1);
                        } else if (objectsMap.containsKey(e.getRawSlot())) {
                            player.getInventory().addItem(objectsMap.get(e.getRawSlot()).buildItem());
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        }
                    }
                })
                .build(inventory -> {
                    for (int i = 0; i < objectsSorted.size(); i++) {
                        objectsMap.put(Items.INVENTORY_CENTER[i], objectsSorted.get(i));
                        if (objectsSorted.get(i).isHide()) {
                            inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.BARRIER).name("§c隐藏 [§m" + objectsSorted.get(i).getPackName() + "§c]").build());
                        } else {
                            inventory.setItem(Items.INVENTORY_CENTER[i], objectsSorted.get(i).buildItem());
                        }
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
                    inventory.setItem(49, new ItemBuilder(Material.REDSTONE).name("§c返回").build());
                }).open(player);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 2f);
    }
}
