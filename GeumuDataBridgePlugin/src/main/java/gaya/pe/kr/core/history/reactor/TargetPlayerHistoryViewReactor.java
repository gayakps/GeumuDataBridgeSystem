package gaya.pe.kr.core.history.reactor;

import gaya.pe.kr.core.history.data.PlayerHistoryWithoutByteData;
import gaya.pe.kr.core.history.manager.HistoryServiceManager;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerHistoryRequest;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.*;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.ConsumerTwoObject;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.msg;

public class TargetPlayerHistoryViewReactor extends MinecraftListSlotInventoryReactor<PlayerHistoryWithoutByteData> {
    
    static final int ALL_HISTORY_VIEW_INDEX = 0;
    static final int EXPIRE_DAY_PREVIOUS_PAGE_INDEX = 1;
    static final int EXPIRE_DAY_AFTER_PAGE_INDEX = 8;
    
    static final int HISTORY_PREVIOUS_PAGE_INDEX = 48;
    static final int HISTORY_AFTER_PAGE_INDEX = 50;
    @Getter
    PlayerHistoryWithoutByteData[] targetPlayerAllHistories;
    String targetPlayerName;

    @Getter @Setter
    int nowExpireDayPage;

    @Getter @Setter
    int expireDayMaxPage;


    public TargetPlayerHistoryViewReactor(Player player, String targetPlayerName, int expireDay, int nowPage, PlayerHistoryWithoutByteData[] playerHistories) {
        super(player, PlayerHistoryWithoutByteData.class, 36, nowPage, true);
        this.targetPlayerAllHistories = playerHistories;
        this.targetPlayerName = targetPlayerName;
        this.nowExpireDayPage = 1;
        this.expireDayMaxPage = (expireDay/6) + 1;
    }

    public TargetPlayerHistoryViewReactor(Player player, String targetPlayerName, int expireDay, int nowExpireDayPage, int nowPage, PlayerHistoryWithoutByteData[] playerHistories) {
        super(player, PlayerHistoryWithoutByteData.class, 36, nowPage, true);
        this.targetPlayerAllHistories = playerHistories;
        this.targetPlayerName = targetPlayerName;
        this.nowExpireDayPage = nowExpireDayPage;
        this.expireDayMaxPage = (expireDay/6) + 1;


    }

    @Override
    protected List<PlayerHistoryWithoutByteData> setPageData() {
        return Arrays.stream(getTargetPlayerAllHistories()).collect(Collectors.toList());
    }

    @Override
    protected void setUpListDataInventory(Inventory nowInventory, HashMap<Integer, PlayerHistoryWithoutByteData> inventorySlotByData) {

        inventorySlotByData.forEach((integer, playerHistory) -> {

            Material material = Material.AIR;

            switch ( playerHistory.getLogType() ) {
                case AUTO_UPDATE: {
                    material = Material.LIGHT_BLUE_CONCRETE;
                    break;
                }
                case LOGIN: {
                    material = Material.LIME_CONCRETE;
                    break;
                }
                case LOGOUT: {
                    material = Material.RED_CONCRETE;
                    break;
                }
            }

            BukkitLocation bukkitLocation = playerHistory.getBukkitLocation();

            String timeStr = TimeUtil.getSimpleDateFormat().format(playerHistory.getDate());

            List<String> lore = new ArrayList<>();

            lore.add(String.format("§fID : %d", playerHistory.getHistoryID()));
            lore.add(String.format("§fTime : %s", timeStr));
            lore.add(String.format("§fCause : %s", playerHistory.getLogType().name()));

            lore.add("§f");
            lore.add(String.format("§fServer : %s", playerHistory.getServer()));
            lore.add(String.format("§fWorld : %s", bukkitLocation.getWorld()));
            lore.add(String.format("§fLocation : %d %d %d", bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ()));

            lore.add("§f");
            lore.add("§f좌클릭 - 인벤토리 및 엔더상자 데이터를 확인합니다");
            lore.add("§f우클릭 - 포켓몬 데이터를 가져옵니다");
            lore.add("§f휠 - 포켓몬, 인벤토리 및 엔더상자를 가져옵니다");

            nowInventory.setItem(integer, ItemCreator.createItemStack(material, timeStr, lore));


        });

    }

    @Override
    protected Inventory initInventoryData() {

        Inventory inventory1 = Bukkit.createInventory(null, 54, String.format("%s - %d§7/§2%d", targetPlayerName, page, totalPage));

        inventory1.setItem(ALL_HISTORY_VIEW_INDEX, ItemCreator.createItemStack(Material.CHEST_MINECART, "&f[ 전체 보기 &f]"));
        inventory1.setItem(EXPIRE_DAY_PREVIOUS_PAGE_INDEX, ItemModifier.setName(UtilMethod.getToLeft(), "&f[ &e이전 페이지 &f] 현재 : " + nowExpireDayPage));
        // 2 ~ 7 인덱스는 Expire Day

        Date startDate;

        if ( nowExpireDayPage > 1 ) {
            startDate = TimeUtil.getAfterDayTime( - ((nowExpireDayPage-1)*6) );
        } else {
            startDate = new Date();
        }

        List<Date> beforeDates = TimeUtil.getBeforeDates(startDate, 5);

        inventory1.setItem(2, ItemCreator.createItemStack(Material.CHEST, TimeUtil.getSimpleDateFormatYearMD().format(startDate), TimeUtil.getField(startDate, Calendar.DAY_OF_MONTH)));

        for (int i = 0; i < beforeDates.size(); i++) {
            Date date = beforeDates.get(i);
            inventory1.setItem(3+i, ItemCreator.createItemStack(Material.CHEST, TimeUtil.getSimpleDateFormatYearMD().format(date), TimeUtil.getField(date, Calendar.DAY_OF_MONTH)));
        }

        inventory1.setItem(EXPIRE_DAY_AFTER_PAGE_INDEX, ItemModifier.setName(UtilMethod.getToRight(), "&f[ &e다음 페이지 &f] 현재 : " + expireDayMaxPage));

        for ( int index = 45; index < 54; index++ ) {
            inventory1.setItem(index, ItemCreator.createItemStack(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        inventory1.setItem(HISTORY_PREVIOUS_PAGE_INDEX, ItemModifier.setName(UtilMethod.getToLeft(), "&f[ &e이전 페이지 &f]"));
        inventory1.setItem(HISTORY_AFTER_PAGE_INDEX, ItemModifier.setName(UtilMethod.getToRight(), "&f[ &e다음 페이지 &f]"));


        return inventory1;
    }

    @Override
    protected void clickInventory(InventoryClickEvent inventoryClickEvent, int clickedSlot) {


        switch ( clickedSlot ) {
            case ALL_HISTORY_VIEW_INDEX: {

                UUID targetPlayerUUID = PlayerUtil.getPlayerUUID(targetPlayerName);

                SchedulerUtil.runLaterTask( () -> {
                    TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(),1, HistoryServiceManager.getInstance().getPlayerHistoryWithoutByteData(targetPlayerUUID).toArray(new PlayerHistoryWithoutByteData[0]));
                    targetPlayerHistoryViewReactor.start();
                },1);

                break;
            }
            case EXPIRE_DAY_PREVIOUS_PAGE_INDEX: {

                nowExpireDayPage--;
                TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(), nowExpireDayPage, page, targetPlayerAllHistories);
                targetPlayerHistoryViewReactor.start();

                break;
            }
            case EXPIRE_DAY_AFTER_PAGE_INDEX: {

                nowExpireDayPage++;
                TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(), nowExpireDayPage, page, targetPlayerAllHistories);
                targetPlayerHistoryViewReactor.start();

                break;
            }
            case HISTORY_PREVIOUS_PAGE_INDEX: {

                page--;
                TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(), page, targetPlayerAllHistories);
                targetPlayerHistoryViewReactor.start();

                break;
            }
            case HISTORY_AFTER_PAGE_INDEX: {

                page++;
                TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(), page, targetPlayerAllHistories);
                targetPlayerHistoryViewReactor.start();

                break;
            }
            default: {

                if ( clickedSlot > 1 && clickedSlot < 8 ) {

                    // 날짜 데이트임

                    Date targetDate = getClickDate(clickedSlot);

                    List<PlayerHistoryWithoutByteData> result = new ArrayList<>();

                    for (PlayerHistoryWithoutByteData targetPlayerAllHistory : targetPlayerAllHistories) {
                        if ( TimeUtil.isSameDay(targetDate, targetPlayerAllHistory.getDate()) ) {
                            result.add(targetPlayerAllHistory);
                        }
                    }

                    if ( !result.isEmpty() ) {
                        TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(), page, result.toArray(PlayerHistoryWithoutByteData[]::new));
                        targetPlayerHistoryViewReactor.start();
                    } else {
                        msg(player, "&c해당 날짜의 기록은 존재하지 않거나, 이미 특정 날짜를 지정했습니다 전체 보기를 사용해주세요");
                    }

                } else {

                    PlayerHistoryWithoutByteData playerHistoryWithoutByteData = inventoryIndexByObject.get(clickedSlot);

                    if ( playerHistoryWithoutByteData != null ) {


                        TargetHistoryRequest targetHistoryRequest = new TargetHistoryRequest(playerHistoryWithoutByteData.getHistoryID());

                        NetworkManager.getInstance().sendPacketGetResult(player, (player1, playerHistories) -> {

                            if ( playerHistories != null ) {

                                PlayerHistory playerHistory = playerHistories[0];
                                PlayerDataForHistory playerDataForHistory = playerHistory.getPlayerDataForHistory();

                                switch ( inventoryClickEvent.getClick() ) {
                                    case LEFT: {
                                        TargetPlayerHistoryInventoryViewReactor targetPlayerHistoryInventoryViewReactor = new TargetPlayerHistoryInventoryViewReactor(TargetPlayerHistoryInventoryViewReactor.InventoryType.CRAFT_INVENTORY,
                                                HistoryServiceManager.getInstance().getExpireDay(), player, targetPlayerName
                                                , targetPlayerAllHistories, playerHistory);
                                        targetPlayerHistoryInventoryViewReactor.start();
                                        break;
                                    }
                                    case RIGHT: {

                                        PlayerPersistentDataManager.getInstance().overridePlayerData(player, playerDataForHistory, TargetPlayerDataUpdateRequest.UpdateType.POKETMON);

                                        player.closeInventory();
                                        player.sendTitle("§e★", "포켓몬 정보 로딩 완료!", 0, 40, 20);

                                        break;
                                    }
                                    case MIDDLE: {

                                        PlayerPersistentDataManager.getInstance().overridePlayerData(player, playerDataForHistory, TargetPlayerDataUpdateRequest.UpdateType.ALL);
                                        player.closeInventory();
                                        player.sendTitle("§e★", "전체 로딩 완료!", 0, 40, 20);

                                        break;
                                    }
                                }
                            } else {
                                msg(player, "&c해당 플레이어의 기록이 없습니다");
                            }

                        }, targetHistoryRequest, PlayerHistory[].class);



                    }

                }

                break;
            }
        }



    }

    public Date getClickDate(int clickedSlot) {
        return TimeUtil.getAfterDayTime( - ((clickedSlot-2) + (nowExpireDayPage-1)*6) );
    }

}
