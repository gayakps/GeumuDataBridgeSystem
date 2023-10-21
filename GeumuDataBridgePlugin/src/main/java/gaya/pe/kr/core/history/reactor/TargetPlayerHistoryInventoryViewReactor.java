package gaya.pe.kr.core.history.reactor;

import gaya.pe.kr.core.history.data.PlayerHistoryWithoutByteData;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.util.*;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.ConsumerTwoObject;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;
import jdk.jshell.execution.Util;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.msg;

@Getter
public class TargetPlayerHistoryInventoryViewReactor extends MinecraftInventoryListener  {

    InventoryType inventoryType;
    String targetPlayerName;
    PlayerHistoryWithoutByteData[] playerAllHistories;
    int expireDay;
    PlayerHistory targetPlayerHistory;

    public TargetPlayerHistoryInventoryViewReactor(InventoryType inventoryType, int expireDay, Player player, String targetPlayerName, PlayerHistoryWithoutByteData[] playerAllHistories, PlayerHistory targetPlayerHistory) {
        super(player);
        this.inventoryType = inventoryType;
        this.targetPlayerName = targetPlayerName;
        this.playerAllHistories = playerAllHistories;
        this.expireDay = expireDay;
        this.targetPlayerHistory = targetPlayerHistory;
    }

    @Override
    protected Inventory initInventoryData() {

        int inventorySize = inventoryType.equals(InventoryType.CRAFT_INVENTORY) ? 54 : 36;

        Inventory inventory1 = Bukkit.createInventory(null, inventorySize, String.format("%s - %s - %d", targetPlayerName, inventorySize == 54 ? "인벤토리" : "엔더 상자", targetPlayerHistory.getHistoryID()));

        ItemStack blackStainedGlassPane = ItemCreator.createItemStack(Material.BLACK_STAINED_GLASS_PANE, "");

        BukkitLocation bukkitLocation = targetPlayerHistory.getBukkitLocation();

        String timeStr = TimeUtil.getSimpleDateFormat().format(targetPlayerHistory.getDate());

        List<String> lore = new ArrayList<>();

        lore.add(String.format("§fID : %d", targetPlayerHistory.getHistoryID()));
        lore.add(String.format("§fTime : %s", timeStr));
        lore.add(String.format("§fCause : %s", targetPlayerHistory.getLogType().name()));

        lore.add("§f");
        lore.add(String.format("§fServer : %s", targetPlayerHistory.getServer()));
        lore.add(String.format("§fWorld : %s", bukkitLocation.getWorld()));
        lore.add(String.format("§fLocation : %d %d %d", bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ()));

        PlayerDataForHistory playerDataForHistory = targetPlayerHistory.getPlayerDataForHistory();

        if ( inventoryType.equals(InventoryType.CRAFT_INVENTORY) ) {

            ItemStack[] contents = (ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getInventoryContents());

            ItemStack[] armorContents = (ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getArmorContents());

            ItemStack offHand = (ItemStack) ObjectConverter.getObject(playerDataForHistory.getOffHand());

            ItemStack[] inventoryStorageContents = IntStream.range(9, 36).boxed().map(integer -> contents[integer]).map(itemStack -> {
                if ( !Filter.isNullOrAirItem(itemStack) ) {
                    return itemStack.clone();
                }
                return itemStack;
            }).toArray(ItemStack[]::new);

            ItemStack[] hotBarContents = IntStream.range(0, 9).boxed().map(integer -> contents[integer]).map(itemStack -> {
                if ( !Filter.isNullOrAirItem(itemStack) ) {
                    return itemStack.clone();
                }
                return itemStack;
            }).toArray(ItemStack[]::new);

            for (ItemStack armorContent : armorContents) {

                EquipmentSlot equipmentSlot = UtilMethod.getEquipmentType(armorContent);

                if ( !Filter.isNullOrAirItem(armorContent)) {

                    if ( equipmentSlot != null ) {
                        switch ( equipmentSlot ) {
                            case HEAD: {
                                inventory1.setItem(0, armorContent.clone());
                                break;
                            }
                            case CHEST: {
                                inventory1.setItem(1, armorContent.clone());
                                break;
                            }
                            case LEGS: {
                                inventory1.setItem(2, armorContent.clone());
                                break;
                            }
                            case FEET: {
                                inventory1.setItem(3, armorContent.clone());
                                break;
                            }
                        }

                    }
                }
            }

            if ( offHand != null ) {
                inventory1.setItem(4, offHand);
            }

            inventory1.setItem(5, blackStainedGlassPane);
            inventory1.setItem(6, blackStainedGlassPane);
            inventory1.setItem(7, blackStainedGlassPane);
            inventory1.setItem(8, blackStainedGlassPane);

            int inventoryIndex = 9;

            for (ItemStack inventoryContent : inventoryStorageContents) {

                if ( inventoryIndex > 35 ) break;

                if ( !Filter.isNullOrAirItem(inventoryContent) ) {
                    inventory1.setItem(inventoryIndex, inventoryContent);
                    inventoryIndex++;
                }
            }

            inventoryIndex = 36;

            for (ItemStack hotBarContent : hotBarContents) {

                if ( inventoryIndex > 44 ) break;

                if ( !Filter.isNullOrAirItem(hotBarContent) ) {
                    inventory1.setItem(inventoryIndex, hotBarContent);
                    inventoryIndex++;
                }
            }


            inventory1.setItem(45, ItemModifier.setName(UtilMethod.getEject(), "&f[ &e메인 창으로 돌아가기 &f]"));
            inventory1.setItem(46, ItemCreator.createItemStack(Material.ENDER_CHEST, "&f[ &e엔더 상자 아이템 확인하기 ]"));
            inventory1.setItem(47, blackStainedGlassPane);
            inventory1.setItem(48, ItemModifier.setName(UtilMethod.getToLeft(), "&f[ &e이전 인벤토리 &f]", lore));
            inventory1.setItem(49, ItemCreator.createItemStack(Material.MAP, "&f[ &e현재 상태 &f]", lore));
            inventory1.setItem(50, ItemModifier.setName(UtilMethod.getToRight(), "&f[ &e다음 인벤토리 &f]", lore));
            inventory1.setItem(51, blackStainedGlassPane);
            inventory1.setItem(52, ItemModifier.setName(UtilMethod.getToUp(), "&f[ 내 인벤토리를 해당 유저의 인벤토리로 저장하기 ]"));
            inventory1.setItem(53, ItemModifier.setName(UtilMethod.getToDown(), "&f[ 해당 인벤토리를 내 인벤토리로 가져오기 ]"));


        }
        else {
            // 엔더상자일 경우

            ItemStack[] enderChest = (ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getEnderChest());

            int inventoryIndex = 0;

            for (ItemStack itemStack : enderChest) {

                if ( inventoryIndex > 26 ) break;

                if ( !Filter.isNullOrAirItem(itemStack) ) {
                    inventory1.setItem(inventoryIndex, itemStack);
                    inventoryIndex++;
                }

            }

            inventory1.setItem(27, ItemModifier.setName(UtilMethod.getEject(), "&f[ &e인벤토리 아이템 확인하기 &f]"));
            inventory1.setItem(28, blackStainedGlassPane);
            inventory1.setItem(29, blackStainedGlassPane);
            inventory1.setItem(30, ItemModifier.setName(UtilMethod.getToLeft(), "&f[ &e이전 엔더 상자 보기 &f]", lore));
            inventory1.setItem(31, ItemCreator.createItemStack(Material.MAP, "&f[ &e현재 상태 &f]", lore));
            inventory1.setItem(32, ItemModifier.setName(UtilMethod.getToRight(), "&f[ &e다음 엔더 상자 보기 &f]", lore));

            inventory1.setItem(33, blackStainedGlassPane);
            inventory1.setItem(34, ItemModifier.setName(UtilMethod.getToUp(), "&f[ 내 엔더 상자를 해당 유저의 엔더 상자로 저장하기 ]"));
            inventory1.setItem(35, ItemModifier.setName(UtilMethod.getToDown(), "&f[ 해당 엔더 상자를 내 엔더 상자로 가져오기 ]"));


        }


        return inventory1;
    }

    @Override
    protected void clickInventory(InventoryClickEvent inventoryClickEvent, int clickedSlot) {


        if ( inventoryType.equals(InventoryType.CRAFT_INVENTORY) ) {
            switch ( clickedSlot ) {
                case 45: {
                    // 메인 창으로 돌아가기
                    TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, expireDay, 1, playerAllHistories);
                    targetPlayerHistoryViewReactor.start();
                    break;
                }
                case 46: {
                    // 엔더 상자 아이템 확인하기
                    this.inventoryType = InventoryType.ENDER_CHEST_INVENTORY;
                    start();

                    break;

                }
                case 48: {
                    //이전 인벤토리

                    PlayerHistoryWithoutByteData playerHistory = getPlayerHistory(false);

                    if ( playerHistory != null ) {

                        TargetHistoryRequest targetHistoryRequest = new TargetHistoryRequest(playerHistory.getHistoryID());
                        ConsumerTwoObject<Player, PlayerHistory[]> playerConsumerTwoObject = (player12, playerHistories) -> {

                            if ( playerHistories != null ) {
                                new TargetPlayerHistoryInventoryViewReactor(InventoryType.CRAFT_INVENTORY, expireDay, player, targetPlayerName, playerAllHistories, playerHistories[0]).start();
                            } else {
                                msg(player, "&c해당 플레이어의 기록이 없습니다");
                            }
                        };
                        NetworkManager.getInstance().sendPacketGetResult(player, playerConsumerTwoObject, targetHistoryRequest, PlayerHistory[].class);

                    } else {
                        msg(player, "&c존재하지 않는 기록입니다");
                    }

                    break;
                }
                case 50: {
                    //이후 인벤토리

                    PlayerHistoryWithoutByteData playerHistory = getPlayerHistory(true);

                    if ( playerHistory != null ) {

                        TargetHistoryRequest targetHistoryRequest = new TargetHistoryRequest(playerHistory.getHistoryID());
                        ConsumerTwoObject<Player, PlayerHistory[]> playerConsumerTwoObject = (player12, playerHistories) -> {

                            if ( playerHistories != null ) {
                                new TargetPlayerHistoryInventoryViewReactor(InventoryType.CRAFT_INVENTORY, expireDay, player, targetPlayerName, playerAllHistories, playerHistories[0]).start();
                            } else {
                                msg(player, "&c해당 플레이어의 기록이 없습니다");
                            }
                        };
                        NetworkManager.getInstance().sendPacketGetResult(player, playerConsumerTwoObject, targetHistoryRequest, PlayerHistory[].class);

                    } else {
                        msg(player, "&c존재하지 않는 기록입니다");
                    }

                    break;

                }
                case 52: {
                    //내 인벤 -> 해당 유저의 인벤

                    TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest = new TargetPlayerDataUpdateRequest(PlayerUtil.getPlayerUUID(targetPlayerName)
                            , ObjectConverter.getObjectAsBytes(player.getInventory().getContents())
                            ,  ObjectConverter.getObjectAsBytes(player.getInventory().getArmorContents())
                            ,  ObjectConverter.getObjectAsBytes(player.getEnderChest().getContents())
                            , PoketmonUtil.serializePCToByte(player.getUniqueId())
                            , PoketmonUtil.serializePartyToByte(player.getUniqueId())
                            , ObjectConverter.getObjectAsBytes(player.getInventory().getItemInOffHand())
                            , TargetPlayerDataUpdateRequest.UpdateType.INVENTORY);

                    NetworkManager.getInstance().sendPacketGetResult(player, (player1, o) -> {

                    }, targetPlayerDataUpdateRequest, Boolean.class);

                    break;
                }
                case 53: {
                    //해당 유저 인벤 -> 내 인벤

                    PlayerPersistentDataManager.getInstance().overridePlayerData(player, targetPlayerHistory.getPlayerDataForHistory(), TargetPlayerDataUpdateRequest.UpdateType.INVENTORY);

                    player.closeInventory();

                    msg(player, String.format("성공적으로 %s 유저의 인벤토리를 내 인벤토리로 업데이트 합니다", targetPlayerName));

                    break;
                }
            }
        } else {
            switch ( clickedSlot ) {
                case 27: {
                    // 인벤토리 아이템 확인하기
                    this.inventoryType = InventoryType.CRAFT_INVENTORY;
                    start();
                    break;
                }
                case 30: {
                    //이전 엔더상자

                    PlayerHistoryWithoutByteData playerHistory = getPlayerHistory(false);

                    if ( playerHistory != null ) {
                        TargetHistoryRequest targetHistoryRequest = new TargetHistoryRequest(playerHistory.getHistoryID());
                        ConsumerTwoObject<Player, PlayerHistory[]> playerConsumerTwoObject = (player12, playerHistories) -> {

                            if ( playerHistories != null ) {
                                new TargetPlayerHistoryInventoryViewReactor(InventoryType.ENDER_CHEST_INVENTORY, expireDay, player, targetPlayerName, playerAllHistories, playerHistories[0]).start();
                            } else {
                                msg(player, "&c해당 플레이어의 기록이 없습니다");
                            }
                        };
                        NetworkManager.getInstance().sendPacketGetResult(player, playerConsumerTwoObject, targetHistoryRequest, PlayerHistory[].class);
                    } else {
                        msg(player, "&c존재하지 않는 기록입니다");
                    }

                    break;
                }
                case 32: {
                    //이후 엔더상자

                    PlayerHistoryWithoutByteData playerHistory = getPlayerHistory(true);

                    if ( playerHistory != null ) {
                        TargetHistoryRequest targetHistoryRequest = new TargetHistoryRequest(playerHistory.getHistoryID());
                        ConsumerTwoObject<Player, PlayerHistory[]> playerConsumerTwoObject = (player12, playerHistories) -> {

                            if ( playerHistories != null ) {
                                new TargetPlayerHistoryInventoryViewReactor(InventoryType.ENDER_CHEST_INVENTORY, expireDay, player, targetPlayerName, playerAllHistories, playerHistories[0]).start();
                            } else {
                                msg(player, "&c해당 플레이어의 기록이 없습니다");
                            }
                        };
                        NetworkManager.getInstance().sendPacketGetResult(player, playerConsumerTwoObject, targetHistoryRequest, PlayerHistory[].class);


                    } else {
                        msg(player, "&c존재하지 않는 기록입니다");
                    }

                    break;

                }
                case 34: {
                    //내 인벤 -> 해당 유저의 인벤
                    //    public TargetPlayerDataUpdateRequest(UUID targetPlayerUUID, byte[] inventory,
                    //    byte[] armorContents, byte[] enderChest, byte[] pc, byte[] poketmon, byte[] offHand, UpdateType updateType) {

                    PlayerDataForHistory playerDataForHistory =PlayerPersistentDataManager.getInstance().getPlayerDataForHistory(player);

                    TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest = new TargetPlayerDataUpdateRequest(PlayerUtil.getPlayerUUID(targetPlayerName)
                            , playerDataForHistory, TargetPlayerDataUpdateRequest.UpdateType.ENDER_CHEST);
                    NetworkManager.getInstance().sendPacketGetResult(player, (player1, o) -> {

                    }, targetPlayerDataUpdateRequest, Boolean.class);

                    break;
                }
                case 35: {
                    //해당 유저 엔더상자 -> 내 엔더 상자

                    PlayerPersistentDataManager.getInstance().overridePlayerData(player, targetPlayerHistory.getPlayerDataForHistory(), TargetPlayerDataUpdateRequest.UpdateType.ENDER_CHEST);
                    player.closeInventory();

                    msg(player, String.format("성공적으로 %s 유저의 엔더 상자를 내 엔더 상자로 업데이트 합니다", targetPlayerName));

                    break;
                }
            }
        }

    }

    @Nullable
    private PlayerHistoryWithoutByteData getPlayerHistory(boolean next) {
        int targetPlayerHistoryID = targetPlayerHistory.getHistoryID();
        PlayerHistoryWithoutByteData closestHistory = null;

        for (PlayerHistoryWithoutByteData playerHistory : playerAllHistories) {
            int historyID = playerHistory.getHistoryID();

            // 다음 history를 찾는 경우
            if (next && historyID > targetPlayerHistoryID) {
                if (closestHistory == null || historyID < closestHistory.getHistoryID()) {
                    closestHistory = playerHistory;
                }
            }
            // 이전 history를 찾는 경우
            else if (!next && historyID < targetPlayerHistoryID) {
                if (closestHistory == null || historyID > closestHistory.getHistoryID()) {
                    closestHistory = playerHistory;
                }
            }
        }

        return closestHistory;
    }


    public enum InventoryType {

        CRAFT_INVENTORY,
        ENDER_CHEST_INVENTORY

    }

}