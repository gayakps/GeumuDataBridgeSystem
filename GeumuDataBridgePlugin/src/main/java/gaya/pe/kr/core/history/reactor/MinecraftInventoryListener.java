package gaya.pe.kr.core.history.reactor;

import com.google.common.base.Preconditions;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.EventUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryLectern;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.msg;
import static gaya.pe.kr.util.ContainerUtil.openInventory;

public abstract class MinecraftInventoryListener implements Listener {

    Inventory inventory;
    Player player;

    public MinecraftInventoryListener(Player player) {
        this.player = player;
    }

    public void open() {

//        getPlayer().closeInventory();
        inventory = initInventoryData();

        EventUtil.register(this);

        SchedulerUtil.runLaterTask( ()-> {
            EntityPlayer entityPlayer = (EntityPlayer) ((CraftPlayer) player).getHandle();
            openInventory(entityPlayer, inventory);
        },1);

    }


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

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

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

}
