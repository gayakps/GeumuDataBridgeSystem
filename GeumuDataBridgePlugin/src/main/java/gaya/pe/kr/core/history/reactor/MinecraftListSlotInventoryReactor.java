package gaya.pe.kr.core.history.reactor;

import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.EventUtil;
import gaya.pe.kr.util.ItemCreator;
import gaya.pe.kr.util.ItemModifier;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.msg;
import static gaya.pe.kr.util.ContainerUtil.openInventory;


public abstract class MinecraftListSlotInventoryReactor<T> implements Listener {

    List<T> pageData;
    int page = 1;
    int totalPage;
    Inventory inventory;
    Player player;

    int dataMaxSize;
    Class<T> clazz;

    HashMap<Integer, T> inventoryIndexByObject = new HashMap<>();

    boolean cancel;

    public MinecraftListSlotInventoryReactor(Player player, Class<T> clazz, int size, int nowPage, boolean cancel) {
        this.player = player;
        this.clazz = clazz;
        this.dataMaxSize = size;
        this.page = nowPage;
        this.cancel = cancel;
    }

    public void open() {

        pageData = setPageData();

        totalPage = (pageData.size() / dataMaxSize) + 1;

        inventory = initInventoryData();

        inventoryIndexByObject.clear();

        int startIndex = (page - 1) * dataMaxSize;
        int lastIndex = (page * dataMaxSize);

        int size = pageData.size();

        if (startIndex > lastIndex || page < 1 || size < startIndex) {
            msg(getPlayer(), "&f[&c&l!&f] 접근할 수 없는 페이지 입니다");
            return;
        }

        EventUtil.register(this);


        int inventoryIndex = 9;
        for (int index = startIndex; index < lastIndex; index++) {
            if (index < size) {

                if ( inventoryIndex > 53 ) {
                    break;
                }

                T data = pageData.get(index);
//                inventory.setItem(inventoryIndex, itemStack);
                inventoryIndexByObject.put(inventoryIndex, data);
                inventoryIndex++;


//                if ( (inventoryIndex+1) % 9 == 0 ) {
//                    inventoryIndex++;
//                }
//
//                if ( inventoryIndex % 9 == 0 ) {
//                    inventoryIndex++;
//                }

            } else {
                break;
            }
        }

        setUpListDataInventory(inventory, inventoryIndexByObject);

        SchedulerUtil.runLaterTask( ()-> {
            EntityPlayer entityPlayer = (EntityPlayer) ((CraftPlayer) player).getHandle();
            openInventory(entityPlayer, inventory);
        },1);


    }

    protected abstract List<T> setPageData();

    protected abstract void setUpListDataInventory(Inventory nowInventory, HashMap<Integer, T> inventorySlotByData);
    protected abstract Inventory initInventoryData();

    public void start() {
        open();
    }

    @EventHandler
    public final void clickInventoryEvent(InventoryClickEvent event) {

        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) return;

        Player clickedPlayer = (Player) event.getWhoClicked();

        if (!clickedPlayer.getUniqueId().equals(player.getUniqueId())) return;

        if (!clickedInventory.equals(getInventory())) return;

        if ( cancel ) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }

        int clickedSlot = event.getSlot();

        clickInventory(event, clickedSlot);

    }

    @EventHandler
    public final void closeInventoryEvent(InventoryCloseEvent event) {

        Inventory nowInventory = event.getInventory();

        if (!nowInventory.equals(getInventory())) return;

        close();

    }

    protected abstract void clickInventory(InventoryClickEvent inventoryClickEvent, int clickedSlot);


    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    protected void close() {
        HandlerList.unregisterAll(this);
    }

    protected void closeAll() {
        player.closeInventory();
        HandlerList.unregisterAll(this);
    }


    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    protected void addPage() {
        page++;
    }

    protected void subtractPage() {
        page--;
    }


}
    