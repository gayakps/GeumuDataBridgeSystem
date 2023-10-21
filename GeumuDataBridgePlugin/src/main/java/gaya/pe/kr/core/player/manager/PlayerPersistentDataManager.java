package gaya.pe.kr.core.player.manager;

import gaya.pe.kr.GayaSoftMain;
import gaya.pe.kr.core.GeumuDataBridgePlugin;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.command.PlayerDataSaveCommand;
import gaya.pe.kr.core.player.listener.PlayerConnectionListener;
import gaya.pe.kr.core.player.scheduler.PlayerPersistentDataAutoSave;
import gaya.pe.kr.core.player.scheduler.PlayerPersistentLoadingScheduler;
import gaya.pe.kr.network.packet.startDirection.client.request.bukkit.PlayerDataSaveRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.GayaSoftNBTUtil;
import gaya.pe.kr.util.ObjectConverter;
import gaya.pe.kr.util.PoketmonUtil;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.GameMode;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.msg;
import static gaya.pe.kr.util.GayaSoftNBTUtil.setNBT;


public class PlayerPersistentDataManager {

    /**
     * 해당 클래스 Manager 에서는 Channel Manager 와 더불어
     * 메인 서버로 데이터를 송/수신 하는 방법을 택한다.
     */


    private static class SingleTon {
        private static final PlayerPersistentDataManager PLAYER_PERSISTENT_MANAGER = new PlayerPersistentDataManager();
    }

    public static synchronized PlayerPersistentDataManager getInstance() {
        return SingleTon.PLAYER_PERSISTENT_MANAGER;
    }

    HashMap<UUID, PlayerPersistentData> playerLoadWaitingData = new HashMap<>(); // 이걸 루핑 돌려서 해당 데이터가 있으면 overriding 해줌
    HashMap<UUID, PlayerPersistentData> playerQuitPersistentData = new HashMap<>(); // 나가면 무조건 여기에 저장

    HashSet<UUID> successLoadingPlayerUUIDHashSet = new HashSet<>();

    @Getter
    PlayerConnectionListener playerConnectionListener;

    List<String> playerList = new ArrayList<>();

    List<String> attributeList = List.of(
            "Attributes", "abilities", "ForgeCaps"
            , "Health", "Bukkit.updateLevel", "foodSaturationLevel"
            , "Score", "XpP", "EnderItems"
            , "foodLevel", "foodExhaustionLevel", "Inventory", "ForgeData" , "XpLevel"
    );

    //

    public void init() {
        playerConnectionListener = new PlayerConnectionListener();
        GeumuDataBridgePlugin.registerEvent(playerConnectionListener);
        PlayerPersistentLoadingScheduler playerPersistentLoadingScheduler = new PlayerPersistentLoadingScheduler();
        SchedulerUtil.scheduleRepeatingTask(playerPersistentLoadingScheduler, 0, 20);
        PlayerPersistentDataAutoSave playerPersistentDataAutoSave = new PlayerPersistentDataAutoSave();
        GeumuDataBridgePlugin.registerCommand("dsave", new PlayerDataSaveCommand());
        SchedulerUtil.scheduleRepeatingTask(playerPersistentDataAutoSave, 0, (20*60)*15);
    }

    public void sendSaveTicket(PlayerPersistentData playerPersistentData, boolean forcedSave) {
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.sendPacket(new PlayerDataSaveRequest(playerPersistentData, forcedSave), Boolean.class);
    }

    public void addQuitPlayerData(PlayerPersistentData playerPersistentData) {
        playerQuitPersistentData.put(playerPersistentData.getPlayerUUID(), playerPersistentData);
    }

    public void addLoadWaitingPlayer(PlayerPersistentData playerPersistentData) {
        playerLoadWaitingData.put(playerPersistentData.getPlayerUUID(), playerPersistentData);
    }

    public HashMap<UUID, PlayerPersistentData> getPlayerLoadWaitingData() {
        return playerLoadWaitingData;
    }

    @Nullable
    public PlayerPersistentData getQuitPlayerPersistentData(UUID targetPlayerUUID, boolean delete) {
        PlayerPersistentData playerPersistentData = playerQuitPersistentData.get(targetPlayerUUID);
        if ( playerQuitPersistentData != null ) {
            if ( delete ) {
                playerQuitPersistentData.remove(targetPlayerUUID);
            }
        }
        return playerPersistentData;
    }

    public PlayerPersistentData getNowPlayerPersistentData(Player targetPlayer) {

        PlayerPersistentData.PlayerPersistentDataBuilder playerPersistentDataBuilder = new PlayerPersistentData.PlayerPersistentDataBuilder(targetPlayer.getUniqueId());

        byte[] poketmonData = PoketmonUtil.serializePartyToByte(targetPlayer.getUniqueId());
        byte[] pcData = PoketmonUtil.serializePCToByte(targetPlayer.getUniqueId());

        CraftPlayer craftPlayer = (CraftPlayer) targetPlayer;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        NBTTagCompound saveCompound = entityPlayer.save(nbtTagCompound);

        ByteArrayOutputStream io = new ByteArrayOutputStream();
        try {
            OutputStream os = new ObjectOutputStream(io);
            NBTCompressedStreamTools.a(saveCompound, os);
////
            for (String key : saveCompound.getKeys()) {
//                System.out.printf("%s : %s\n", key, saveCompound.get(key) != null ? saveCompound.get(key) : "NONE");
            }

            byte[] nbtData = io.toByteArray();

            playerPersistentDataBuilder
                    .setPlayerData(nbtData)
                    .setPlayerGameMode(GameMode.valueOf(targetPlayer.getGameMode().name().toUpperCase(Locale.ROOT)));

            if ( pcData != null ) {
                playerPersistentDataBuilder.setPcData(pcData);
            }

            if ( poketmonData != null ) {
                playerPersistentDataBuilder.setPoketmonData(poketmonData);
            }

            List<PotionEffect> potionEffectList = new ArrayList<>(targetPlayer.getActivePotionEffects());

            if (!potionEffectList.isEmpty()) {
                playerPersistentDataBuilder.setEffects(ObjectConverter.getObjectAsBytes(potionEffectList));
            }

            return playerPersistentDataBuilder.build();
        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;

    }

    public void allPlayerDataSave(CommandSender commandSender, boolean kickPlayer) {

        List<Player> collection = new ArrayList<>(Bukkit.getOnlinePlayers());

        commandSender.sendMessage(String.format("서버 데이터 저장 시작... [ %d명 ]", collection.size()));

        PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();

        for (Player targetPlayer : collection) {
            try {
                commandSender.sendMessage(String.format("%s 플레이어 데이터 저장 요청", targetPlayer.getName()));
                PlayerPersistentData playerPersistentData = playerPersistentDataManager.getNowPlayerPersistentData(targetPlayer);
                playerPersistentDataManager.sendSaveTicket(playerPersistentData, true);
                commandSender.sendMessage(String.format("%s 플레이어 데이터 저장 완료", targetPlayer.getName()));
                if ( kickPlayer ) {
                    targetPlayer.kickPlayer("서버 종료");
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public void overridePlayerData(Player targetPlayer, PlayerDataForHistory playerDataForHistory, TargetPlayerDataUpdateRequest.UpdateType updateType) {

        PlayerInventory playerInventory = targetPlayer.getInventory();
        Inventory enderChest = targetPlayer.getEnderChest();

        UUID uuid = targetPlayer.getUniqueId();

        switch ( updateType ) {
            case ALL: {

                PoketmonUtil.loadPC(uuid, playerDataForHistory.getPc());
                PoketmonUtil.loadParty(uuid, playerDataForHistory.getPoketmon());
                playerInventory.setContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getInventoryContents()));
                targetPlayer.getEnderChest().setContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getEnderChest()));
                playerInventory.setArmorContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getArmorContents()));
                playerInventory.setItemInOffHand((org.bukkit.inventory.ItemStack) ObjectConverter.getObject(playerDataForHistory.getOffHand()));

                msg(targetPlayer, "&6성공적으로 포켓몬, 인벤토리, 엔더상자를 업데이트 했습니다");

                break;
            }
            case ENDER_CHEST: {
                enderChest.setContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getEnderChest()));
                msg(targetPlayer, "&6성공적으로 엔더상자를 업데이트 했습니다");
                break;
            }
            case INVENTORY: {
                playerInventory.setContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getInventoryContents()));
                playerInventory.setArmorContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getArmorContents()));
                playerInventory.setItemInOffHand((org.bukkit.inventory.ItemStack) ObjectConverter.getObject(playerDataForHistory.getOffHand()));

                msg(targetPlayer, "&6성공적으로 인벤토리를 업데이트 했습니다");
                break;
            }
            case INVENTORY_ENDER_CHEST: {

                playerInventory.setContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getInventoryContents()));
                enderChest.setContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getEnderChest()));
                playerInventory.setArmorContents((org.bukkit.inventory.ItemStack[]) ObjectConverter.getObject(playerDataForHistory.getArmorContents()));
                playerInventory.setItemInOffHand((ItemStack) ObjectConverter.getObject(playerDataForHistory.getOffHand()));
                msg(targetPlayer, "&6성공적으로 엔더상자 및 인벤토리를 업데이트 했습니다");

                break;
            }
            case POKETMON:{
                PoketmonUtil.loadPC(uuid, playerDataForHistory.getPc());
                PoketmonUtil.loadParty(uuid, playerDataForHistory.getPoketmon());
                msg(targetPlayer, "&6성공적으로 포켓몬 데이터를 업데이트 했습니다");
            }
        }


    }



    public void overridePlayerData(Player player, PlayerPersistentData playerPersistentData) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        byte[] nbtData = playerPersistentData.getPlayerNBTData();


        if (nbtData != null) {

            entityPlayer.getMinecraftServer().processQueue.add( ()-> {

                    NBTTagCompound nbt = new NBTTagCompound();
                    NBTTagCompound nbtTagCompound = entityPlayer.save(nbt); // 현재 월드에 등록된 ㄷ이터 확보
                    NBTTagCompound newNbtCompound = GayaSoftNBTUtil.byteToNBTTags(nbtData);

                    for (String attribute : attributeList) {
                        try {
                            setNBT(nbtTagCompound, newNbtCompound, attribute);
                        } catch ( Exception e ) {
                            e.printStackTrace();
                        }
                    }

                    entityPlayer.load(nbtTagCompound);

                    SchedulerUtil.runLaterTask(()-> {
                        int heldSlot = newNbtCompound.getInt("SelectedItemSlot");
                        player.getInventory().setHeldItemSlot(heldSlot);

                        float xp = newNbtCompound.getFloat("XpP");
                        player.setExp(xp);

                        int level = newNbtCompound.getInt("XpLevel");
                        player.setLevel(level);

                    }, 40);

            });

        }

        SchedulerUtil.runLaterTask(() -> player.setGameMode(org.bukkit.GameMode.valueOf(playerPersistentData.getGameMode().name().toUpperCase(Locale.ROOT))), 1);
        SchedulerUtil.runLaterTask(() -> player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType())), 1);

        byte[] effects = playerPersistentData.getEffectList();

        if (effects != null) {
            List<PotionEffect> potionEffectList = (List<PotionEffect>) ObjectConverter.getObject(effects);
            if (!potionEffectList.isEmpty()) {
                for (PotionEffect potionEffect : potionEffectList) {
                    SchedulerUtil.runLaterTask(() -> player.addPotionEffect(potionEffect), 1);
                }
            }
        }

        byte[] poketmonData = playerPersistentData.getPoketmonData();

        byte[] pcData = playerPersistentData.getPcData();

        if ( pcData != null ) {
            PoketmonUtil.loadPC(player.getUniqueId(), pcData);
        }

        if ( poketmonData != null ) {
            PoketmonUtil.loadParty(player.getUniqueId(), poketmonData);
        }

        playerPersistentData.setPlayerUUID(player.getUniqueId());
        addSuccessLoading(player.getUniqueId());

        player.sendTitle("§e★", "로딩 완료!", 20, 140, 10);

        SchedulerUtil.runLaterTask( ()-> {
            NetworkManager networkManager = NetworkManager.getInstance();
            Location location = player.getLocation();
            BukkitLocation bukkitLocation = new BukkitLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            PlayerConnectionHistorySaveRequest playerConnectionHistorySaveRequest = new PlayerConnectionHistorySaveRequest("127.0.0.1", Bukkit.getPort(), player.getName(), player.getUniqueId(), getPlayerDataForHistory(player), bukkitLocation, PlayerConnectionLogType.LOGIN);
            networkManager.sendPacket(playerConnectionHistorySaveRequest);
        }, 10);

//        player.sendMessage(ConfigData.SYNC_COMPLETE.getMessage());

    }


    public PlayerDataForHistory getPlayerDataForHistory(Player player) {

        PlayerInventory playerInventory = player.getInventory();

        byte[] inventory = ObjectConverter.getObjectAsBytes(playerInventory.getContents());
        byte[] armor = ObjectConverter.getObjectAsBytes(playerInventory.getArmorContents());
        byte[] ender = ObjectConverter.getObjectAsBytes(player.getEnderChest().getContents());
        byte[] pc = PoketmonUtil.serializePCToByte(player.getUniqueId());
        byte[] poketmon = PoketmonUtil.serializePartyToByte(player.getUniqueId());
        byte[] offhand = ObjectConverter.getObjectAsBytes(playerInventory.getItemInOffHand());

        return new PlayerDataForHistory(inventory, armor, ender, pc, poketmon, offhand);

    }


    public void addSuccessLoading(UUID uuid) {
        successLoadingPlayerUUIDHashSet.add(uuid);
    }

    public void removeSuccessLoadingPlayer(UUID uuid) {
        successLoadingPlayerUUIDHashSet.remove(uuid);
    }

    public boolean isSuccessLoadingPlayer(UUID uuid) {
        return successLoadingPlayerUUIDHashSet.contains(uuid);
    }


}
