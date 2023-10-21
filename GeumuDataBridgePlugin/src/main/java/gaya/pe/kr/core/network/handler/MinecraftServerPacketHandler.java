package gaya.pe.kr.core.network.handler;

import gaya.pe.kr.core.history.db.DBConnection;
import gaya.pe.kr.core.history.manager.HistoryServiceManager;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataClientLoadResponse;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataResponse;
import gaya.pe.kr.network.packet.startDirection.client.send.MinecraftBukkitChannelHandlerAddRequest;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataClientLoadRequest;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataRequest;
import gaya.pe.kr.network.packet.startDirection.server.response.PlayerDataSaveResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetHistoryResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetPlayerDataUpdateResponse;
import gaya.pe.kr.network.packet.startDirection.server.send.JDBCDataSend;
import gaya.pe.kr.network.packet.startDirection.server.send.TargetPlayerDataUpdateSend;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.PoketmonUtil;
import gaya.pe.kr.util.data.WaitingTicket;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.log;


/**
 * 서버로 부터 전송된 패킷을 처리 하는 곳
 */
public class MinecraftServerPacketHandler extends SimpleChannelInboundHandler<AbstractMinecraftPacket> {

    PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();

    HashMap<Long, WaitingTicket<?>> packetWaitingResponseAsObjectHashMap = new HashMap<>();


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 예외 처리
        cause.printStackTrace();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log(String.format("%s Server Join\n", ctx.channel().toString()));
        Channel channel = ctx.channel();

        SchedulerUtil.runLaterTask( ()-> {

            MinecraftBukkitChannelHandlerAddRequest minecraftBukkitChannelHandlerAddRequest = new MinecraftBukkitChannelHandlerAddRequest(NetworkManager.getInstance().getConfiguration().getString("now_ip"), Bukkit.getServer().getPort());
            ChannelFuture channelFuture = channel.writeAndFlush(minecraftBukkitChannelHandlerAddRequest);
            try {
                channelFuture.get();
                log( String.format("데이터를 보내줌 %s:%d\n", minecraftBukkitChannelHandlerAddRequest.getIp(), minecraftBukkitChannelHandlerAddRequest.getPort()));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

        }, 5);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractMinecraftPacket minecraftPacket) throws Exception {
        // 패킷 타입에 따라 분기 처리

        System.out.printf("RECEIVED PACKET [FROM SERVER] : %s\n", minecraftPacket.getType().name());

        long packetId = minecraftPacket.getPacketID();

        Channel channel = channelHandlerContext.channel();

        switch (minecraftPacket.getType()) {

            case PLAYER_DATA_SAVE_RESPONSE: {

                // 서버로부터 데이터가 정상적으로 저장되었다면 Quit 으로부터 제거
                // 서버로부터 데이터가 정상적으로 제거되지 않았다면 Quit 에서 DB 직접 접근을 통해 저장
                PlayerDataSaveResponse playerDataSaveResponse = (PlayerDataSaveResponse) minecraftPacket;
                ResponseType responseType = playerDataSaveResponse.getResponseType();

                if (isWaitingTicket(playerDataSaveResponse.getRequestPacketId())) {
                    getWaitingTicket(playerDataSaveResponse.getRequestPacketId()).setResult(responseType.equals(ResponseType.SUCCESS));
                    System.out.printf("UUID : %s RESULT : %s\n", playerDataSaveResponse.getTargetPlayerUUID(), responseType.name());
                }

                break;
            }

            case PLAYER_DATA_CLIENT_LOAD_REQUEST: {
                // Velocity 로부터 특정 플레이어를 로드해달란 요청을 받았다.

                PlayerDataClientLoadRequest playerDataClientLoadRequest = (PlayerDataClientLoadRequest) minecraftPacket;
                PlayerPersistentData playerPersistentData = playerDataClientLoadRequest.getPlayerPersistentData();
                playerPersistentDataManager.addLoadWaitingPlayer(playerPersistentData);
                playerDataClientLoadRequest.handleResponse(() -> new PlayerDataClientLoadResponse(packetId, ResponseType.SUCCESS), channel); // 응답 생성

                break;

            }

            case PLAYER_DATA_REQUEST: {

                PlayerDataRequest playerDataRequest = (PlayerDataRequest) minecraftPacket;
                PlayerPersistentData playerPersistentData = playerPersistentDataManager.getQuitPlayerPersistentData(playerDataRequest.getPlayerUUID(), false);

                if ( playerPersistentData == null ) {
                    System.out.println("퇴장 데이터에 없습니다");
                    System.out.println("현재 접속중인 데이터로 송출");
                    Player player = Bukkit.getPlayer(playerDataRequest.getPlayerUUID());
                    playerPersistentData = playerPersistentDataManager.getNowPlayerPersistentData(player);
                } else {
                    System.out.println("퇴장 데이터에 존재");
                }

                PlayerPersistentData finalPlayerPersistentData = playerPersistentData;
                playerDataRequest.handleResponse(() -> new PlayerDataResponse(packetId, finalPlayerPersistentData != null ? ResponseType.SUCCESS : ResponseType.FAIL, finalPlayerPersistentData), channel);

                break;
            }

            case TARGET_HISTORY_RESPONSE: {

                TargetHistoryResponse targetHistoryResponse = (TargetHistoryResponse) minecraftPacket;

                HistoryServiceManager historyServiceManager = HistoryServiceManager.getInstance();
                historyServiceManager.setExpireDay(targetHistoryResponse.getExpireDay());

                if (isWaitingTicket(targetHistoryResponse.getRequestPacketId())) {
                    System.out.println("TARGET : " + targetHistoryResponse.getExpireDay() + " REASON : " + targetHistoryResponse.getReason());
                    getWaitingTicket(targetHistoryResponse.getRequestPacketId()).setResult(targetHistoryResponse.getResponseObject());
                }

                break;
            }
            case TARGET_PLAYER_DATA_UPDATE_RESPONSE: {

                TargetPlayerDataUpdateResponse targetPlayerDataUpdateResponse = (TargetPlayerDataUpdateResponse) minecraftPacket;

                if (isWaitingTicket(targetPlayerDataUpdateResponse.getRequestPacketId())) {
                    System.out.println("TARGET SIZE : " + targetPlayerDataUpdateResponse.getReason());
                    getWaitingTicket(targetPlayerDataUpdateResponse.getRequestPacketId()).setResult(targetPlayerDataUpdateResponse.getResponseObject());
                }


                break;

            }
            case TARGET_PLAYER_DATA_UPDATE_SEND: {

                TargetPlayerDataUpdateSend targetPlayerDataUpdateSend = (TargetPlayerDataUpdateSend) minecraftPacket;

                TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest = targetPlayerDataUpdateSend.getTargetPlayerDataUpdateRequest();


                UUID uuid = targetPlayerDataUpdateRequest.getTargetPlayerUUID();


                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                    if ( onlinePlayer.getUniqueId().equals(uuid) ) {

                        TargetPlayerDataUpdateRequest.UpdateType updateType = targetPlayerDataUpdateRequest.getUpdateType();
                        PlayerDataForHistory playerDataForHistory = targetPlayerDataUpdateRequest.getPlayerDataForHistory();
                        playerPersistentDataManager.overridePlayerData(onlinePlayer, playerDataForHistory, updateType);

                        return;
                    }

                }

                break;

            }

            case JDBC_DATA_SEND: {
                JDBCDataSend jdbcDataSend = (JDBCDataSend) minecraftPacket;
//                DBConnection.init(jdbcDataSend);
                break;
            }

            default:
                // 알 수 없는 패킷 처리
                break;
        }
    }

    public boolean isWaitingTicket(long requestPacketId) {
        return packetWaitingResponseAsObjectHashMap.containsKey(requestPacketId);
    }


    @SuppressWarnings("unchecked")
    public <T> WaitingTicket<T> getWaitingTicket(long requestPacketId) {

        WaitingTicket<T> waitingTicket = (WaitingTicket<T>) packetWaitingResponseAsObjectHashMap.get(requestPacketId);

        if ( waitingTicket != null ) {
            packetWaitingResponseAsObjectHashMap.remove(requestPacketId);
            return waitingTicket;
        }

        return null;
    }

    public void removeWaitingTicket(long requestPacketId) {
        packetWaitingResponseAsObjectHashMap.remove(requestPacketId);
    }

    public void addWaitingTicket(AbstractMinecraftPacket abstractMinecraftPacket, WaitingTicket<?> waitingTicket ) {
        packetWaitingResponseAsObjectHashMap.put(abstractMinecraftPacket.getPacketID(), waitingTicket);
    }

    private <T> void handleWaitingTicket(long requestPacketId, T tObject) throws RuntimeException {
        WaitingTicket<T> waitingTicket = getWaitingTicket(requestPacketId);
        waitingTicket.setResult(tObject);
        removeWaitingTicket(requestPacketId);
    }



}
