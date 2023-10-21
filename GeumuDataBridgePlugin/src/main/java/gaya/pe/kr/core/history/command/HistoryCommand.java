package gaya.pe.kr.core.history.command;

import gaya.pe.kr.core.history.data.PlayerHistoryWithoutByteData;
import gaya.pe.kr.core.history.manager.HistoryServiceManager;
import gaya.pe.kr.core.history.reactor.TargetPlayerHistoryInventoryViewReactor;
import gaya.pe.kr.core.history.reactor.TargetPlayerHistoryViewReactor;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerHistoryRequest;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.ItemCreator;
import gaya.pe.kr.util.PlayerUtil;
import gaya.pe.kr.util.PoketmonUtil;
import gaya.pe.kr.util.UtilMethod;
import gaya.pe.kr.util.data.ConsumerTwoObject;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.msg;

public class HistoryCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if ( commandSender instanceof Player) {

            Player player = ((Player) commandSender).getPlayer();

            if ( player == null ) return false;

            if ( !player.hasPermission("db.admin") ) {
                return false;
            }

            NetworkManager networkManager = NetworkManager.getInstance();

            if ( args.length > 0 ) {

                String category = args[0];

                switch ( category ) {
                    case "load" : {
                        //type ("all" ,"i", "p", "e", "ie");
                        //history id

                        try {
                            String type = args[1];
                            int historyID = Integer.parseInt(args[2]);
                            TargetHistoryRequest targetHistoryRequest = new TargetHistoryRequest(historyID);
                            ConsumerTwoObject<Player, PlayerHistory[]> playerConsumerTwoObject = (player12, playerHistories) -> {

                                if ( playerHistories != null ) {
                                    PlayerPersistentDataManager.getInstance().overridePlayerData(player12, playerHistories[0].getPlayerDataForHistory(), TargetPlayerDataUpdateRequest.UpdateType.valueOf(type));
                                } else {
                                    msg(player, "&c해당 플레이어의 기록이 없습니다");
                                }
                            };
                            networkManager.sendPacketGetResult(player, playerConsumerTwoObject, targetHistoryRequest, PlayerHistory[].class);

                        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e ) {
                            msg(player, "&c숫자를 정확하게 입력해주세요");
                        }


                        break;
                    }
                    case "save" : {
                        // 내 정보 -> 특정 플레이어 정보

                        try {

                            String updateTypeStr = args[1];
                            TargetPlayerDataUpdateRequest.UpdateType updateType = TargetPlayerDataUpdateRequest.UpdateType.valueOf(updateTypeStr);


                            String targetPlayerName = args[2];
                            UUID targetPlayerUUID = PlayerUtil.getPlayerUUID(targetPlayerName);

                            if (targetPlayerUUID == null ) {
                                msg(player, "&c존재하지 않는 플레이어입니다");
                            }

                            PlayerDataForHistory playerDataForHistory =PlayerPersistentDataManager.getInstance().getPlayerDataForHistory(player);

                            TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest = new TargetPlayerDataUpdateRequest(targetPlayerUUID
                                    , playerDataForHistory
                                    , updateType);


                            NetworkManager.getInstance().sendPacketGetResult(player, (player1, o) -> {

                                if ( o ) {
                                    msg(player, String.format("&e성공적으로 %s을(를) 전송합니다", updateTypeStr));
                                } else {
                                    msg(player, "&c작업에 실패했습니다");
                                }
                            }, targetPlayerDataUpdateRequest, Boolean.class);

                        } catch ( ArrayIndexOutOfBoundsException e ) {
                            msg(player, "&c플레이어 및 저장 타입을 입력해주세요!");
                        }

                        break;
                    }
                    case "history" : {
                        // 플레이어 이름만

                        try {
                            String targetPlayerName = args[1];

                            UUID targetPlayerUUID = PlayerUtil.getPlayerUUID(targetPlayerName);

                            if (targetPlayerUUID == null ) {
                                msg(player, "&c존재하지 않는 플레이어입니다");
                                return false;
                            }

                                SchedulerUtil.runTaskAsync( ()-> {
                                    TargetPlayerHistoryViewReactor targetPlayerHistoryViewReactor = new TargetPlayerHistoryViewReactor(player, targetPlayerName, HistoryServiceManager.getInstance().getExpireDay(),1, HistoryServiceManager.getInstance().getPlayerHistoryWithoutByteData(targetPlayerUUID).toArray(new PlayerHistoryWithoutByteData[0]));
                                    targetPlayerHistoryViewReactor.start();
                                });



                        } catch ( ArrayIndexOutOfBoundsException e ) {
                            msg(player, "&c플레이어 이름을 입력해주세요!");
                        }

                        break;
                    }
                }

            } else {

            }


        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if ( commandSender instanceof Player) {

            Player player = ((Player) commandSender).getPlayer();

            if ( player == null ) return null;

            if (!player.hasPermission("db.admin")) {
                return null;
            }

            if (args.length == 1) {
                return Arrays.asList("load","save","history");
            }

            if ( args.length == 2 ) {
                String category = args[0];

                switch ( category ) {
                    case "load" :
                    case "save": {
                        return Arrays.stream(TargetPlayerDataUpdateRequest.UpdateType.values()).map(Enum::name).collect(Collectors.toList());
                    }
                    case "history": {
                        return List.of("닉네임을 입력해주세요");
                    }
                    default: {
                        return List.of("load,save,history 셋 중 하나의 명령어를 입력해주세요");
                    }
                }

            }

            if ( args.length == 3 ) {
                String category = args[0];

                if (category.equals("history") ) {
                    return List.of("입력하실 명령어가 없습니다");
                } else if ( category.equals("load")) {
                    return List.of("로그 번호를 입력해주세요");
                } else if ( category.equals("save") ) {
                    return List.of("닉네임을 입력해주세요");
                }

            }

        }

        return null;
    }
}
