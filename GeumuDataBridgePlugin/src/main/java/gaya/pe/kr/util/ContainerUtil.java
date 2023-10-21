package gaya.pe.kr.util;

import com.google.common.base.Preconditions;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryLectern;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class ContainerUtil {


    public static InventoryView openInventory(EntityPlayer player, Inventory inventory) {
        Container formerContainer = player.activeContainer;
        ITileInventory iinventory = null;
        if (inventory instanceof CraftInventoryDoubleChest) {
            iinventory = ((CraftInventoryDoubleChest) inventory).tile;
        } else if (inventory instanceof CraftInventoryLectern) {
            iinventory = ((CraftInventoryLectern) inventory).tile;
        } else if (inventory instanceof CraftInventory) {
            CraftInventory craft = (CraftInventory) inventory;
            if (craft.getInventory() instanceof ITileInventory) {
                iinventory = (ITileInventory) craft.getInventory();
            }
        }

        if (iinventory instanceof ITileInventory && iinventory instanceof TileEntity) {
            TileEntity te = (TileEntity) iinventory;
            if (!te.hasWorld()) {
                te.setLocation(player.world, player.getChunkCoordinates());
            }
        }

        Containers<?> container = CraftContainer.getNotchInventoryType(inventory);
        if (iinventory instanceof ITileInventory) {
            openContainer(player, iinventory);
        } else {
            openCustomInventory(inventory, player, container);
        }

        if ( player.activeContainer == formerContainer) {
            return null;
        } else {
            player.activeContainer.checkReachable = false;
            return player.activeContainer.getBukkitView();
        }
    }

    public static OptionalInt openContainer(EntityPlayer entityPlayer, @Nullable ITileInventory itileinventory) {
        if (itileinventory == null) {
            return OptionalInt.empty();
        } else {
            if ( entityPlayer.activeContainer != entityPlayer.defaultContainer) {
                entityPlayer.closeInventory();
            }

            entityPlayer.nextContainerCounter();
            int containerCounter = 3;
            Container container = itileinventory.createMenu( containerCounter, entityPlayer.inventory, entityPlayer);

            if (container != null) {
                container.setTitle(itileinventory.getScoreboardDisplayName());
                boolean cancelled = false;
                container = CraftEventFactory.callInventoryOpenEvent(entityPlayer, container, cancelled);
                if (container == null && !cancelled) {
                    if (itileinventory instanceof IInventory) {
                        ((IInventory)itileinventory).closeContainer(entityPlayer);
                    } else if (itileinventory instanceof BlockChest.DoubleInventory) {
                        ((BlockChest.DoubleInventory)itileinventory).inventorylargechest.closeContainer(entityPlayer);
                    }

                    return OptionalInt.empty();
                }
            }

            if (container == null) {
                if (entityPlayer.isSpectator()) {
                    entityPlayer.a((IChatBaseComponent)(new ChatMessage("container.spectatorCantOpen")).a(EnumChatFormat.RED), true);
                }
                return OptionalInt.empty();
            } else {
                entityPlayer.activeContainer = container;
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, container.getType(), container.getTitle()));
                container.addSlotListener(entityPlayer);
                return OptionalInt.of( containerCounter);
            }
        }
    }

    public static void openCustomInventory(Inventory inventory, EntityPlayer player, Containers<?> windowType) {
        if (player.playerConnection != null) {
            Preconditions.checkArgument(windowType != null, "Unknown windowType");
            Container container = new CraftContainer(inventory, player, player.nextContainerCounter());
            CraftEventFactory.callInventoryOpenEvent(player, container);
            String title = container.getBukkitView().getTitle();
            player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, windowType, CraftChatMessage.fromString(title)[0]));
            player.activeContainer = container;
            player.activeContainer.addSlotListener(player);
        }
    }

}
