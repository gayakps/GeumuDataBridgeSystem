package com.velocitypowered.proxy.gaya.network;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.config.VelocityConfiguration;
import com.velocitypowered.proxy.gaya.databse.DBConnection;
import com.velocitypowered.proxy.gaya.history.manager.HistoryManager;
import com.velocitypowered.proxy.gaya.player.PlayerHandler;
import com.velocitypowered.proxy.scheduler.VelocityScheduler;
import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.client.request.bukkit.PlayerDataSaveRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataClientLoadResponse;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataResponse;
import gaya.pe.kr.network.packet.startDirection.client.send.MinecraftBukkitChannelHandlerAddRequest;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.startDirection.server.response.PlayerDataSaveResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetHistoryResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetPlayerDataUpdateResponse;
import gaya.pe.kr.network.packet.startDirection.server.send.JDBCDataSend;
import gaya.pe.kr.network.packet.startDirection.server.send.TargetPlayerDataUpdateSend;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.converter.ObjectConverter;
import gaya.pe.kr.util.data.WaitingTicket;
import gaya.pe.kr.util.data.player.GameMode;
import gaya.pe.kr.util.data.player.PlayerHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GayaSoftBackendHandler extends SimpleChannelInboundHandler<AbstractMinecraftPacket> {

    public static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    GayaSoftNetworkManager gayaSoftNetworkManager = GayaSoftNetworkManager.getInstance();
    VelocityServer velocityServer;

    public GayaSoftBackendHandler(VelocityServer velocityServer) {
        System.out.println("Gaya soft Backend Handler Add ( Ready to receive client message )\n");
        this.velocityServer = velocityServer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 예외 처리
        cause.printStackTrace();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String ip = channel.remoteAddress().toString();
        try {
            channelGroup.remove(channel);
            System.out.printf("Channel : %s [IP : %s] Inactive", channel.toString(), ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.add(channel);

        VelocityConfiguration.DataBase base = velocityServer.getConfiguration().getDataBase();

        channel.writeAndFlush(new JDBCDataSend(base.getHost(), base.getPort(), base.getDatabase(), base.getUsername(), base.getPassword()));

        System.out.println(channel.toString() + " :: ADD STARTING Network Send DB Data");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractMinecraftPacket minecraftPacket) throws Exception {
        // 패킷 타입에 따라 분기 처리
        System.out.printf("RECEIVED PACKET [FROM CLIENT] : %s\n", minecraftPacket.getType().name());

        Channel channel = channelHandlerContext.channel();
        PacketType packetType = minecraftPacket.getType();
        long packetId = minecraftPacket.getPacketID();

        switch ( packetType ) {

            case MINECRAFT_BUKKIT_CHANNEL_HANDLER_ADD_REQUEST: {

                MinecraftBukkitChannelHandlerAddRequest minecraftBukkitChannelHandlerAddRequest = (MinecraftBukkitChannelHandlerAddRequest) minecraftPacket;
                GayaSoftNetworkManager.getInstance().addGayaSoftChannel(minecraftBukkitChannelHandlerAddRequest, channel);

                break;
            }

            case PLAYER_DATA_SAVE_REQUEST: {

                PlayerDataSaveRequest playerDataSaveRequest = (PlayerDataSaveRequest) minecraftPacket;
                PlayerPersistentData saveTarget = playerDataSaveRequest.getPlayerPersistentData();

                if ( playerDataSaveRequest.isForcedSave() ) {
                    //TODO 무조건 저장
                    if ( PlayerHandler.savePlayerData(saveTarget, false) ) {
                        playerDataSaveRequest.handleResponse(()-> new PlayerDataSaveResponse(saveTarget.getPlayerUUID(), playerDataSaveRequest.getPacketID(), ResponseType.FAIL), channel);
                    } else {
                        playerDataSaveRequest.handleResponse(()-> new PlayerDataSaveResponse(saveTarget.getPlayerUUID(), playerDataSaveRequest.getPacketID(), ResponseType.SUCCESS), channel);
                    }
                }

                if ( PlayerHandler.playerPersistentDataHashMap.containsKey(saveTarget.getPlayerUUID()) ) {
                    PlayerHandler.playerPersistentDataHashMap.put(saveTarget.getPlayerUUID(), saveTarget);
                    System.out.println("PlayerDataSaveRequest [기존 데이터가 있는 상태에서 덮어 쓰기 합니다]- " + saveTarget.getPlayerUUID());
                    playerDataSaveRequest.handleResponse(()-> new PlayerDataSaveResponse(saveTarget.getPlayerUUID(), playerDataSaveRequest.getPacketID(), ResponseType.SUCCESS), channel);

                } else {

                    boolean existPlayer = false;

                    for (Player allPlayer : velocityServer.getAllPlayers()) {
                        if ( allPlayer.getUniqueId().equals(saveTarget.getPlayerUUID()) ) {
                            // 유저가 서버안에 있으면 놔둬야함
                            existPlayer = true;
                        }
                    }

                    PlayerHandler.playerPersistentDataHashMap.put(saveTarget.getPlayerUUID(), saveTarget);

                    if ( existPlayer ) {
                        System.out.println("PlayerDataSaveRequest 정상저장 - " + saveTarget.getPlayerUUID());
                        playerDataSaveRequest.handleResponse(()-> new PlayerDataSaveResponse(saveTarget.getPlayerUUID(), playerDataSaveRequest.getPacketID(), ResponseType.SUCCESS), channel);
                    } else {
                        // 유저가 서버안에 없으면 저장해야함

                        System.out.printf("%s 님은 서버 내에서 나가셨기 때문에, 데이터를 저장합니다\n", saveTarget.getPlayerUUID());

                        if (PlayerHandler.savePlayerData(saveTarget, true)) {
                            playerDataSaveRequest.handleResponse(() -> new PlayerDataSaveResponse(saveTarget.getPlayerUUID(), playerDataSaveRequest.getPacketID(), ResponseType.FAIL), channel);
                        } else {
                            playerDataSaveRequest.handleResponse(() -> new PlayerDataSaveResponse(saveTarget.getPlayerUUID(), playerDataSaveRequest.getPacketID(), ResponseType.SUCCESS), channel);
                        }

                    }

                }

                break;
            }
            case PLAYER_DATA_CLIENT_LOAD_RESPONSE: {

                PlayerDataClientLoadResponse playerDataClientLoadResponse = (PlayerDataClientLoadResponse) minecraftPacket;
                long requestPacketId = playerDataClientLoadResponse.getRequestPacketId();

                System.out.println("서버로부터 플레이어의 로딩을 할 준비가 되었다고 전달되었습니다 REQYEST PACKET : " + requestPacketId);

                gayaSoftNetworkManager.getWaitingTicket(requestPacketId).setResult(playerDataClientLoadResponse.getResponseType().equals(ResponseType.SUCCESS));

                break;
            }
            case PLAYER_DATA_RESPONSE: {

                PlayerDataResponse playerDataResponse = (PlayerDataResponse) minecraftPacket;
                long requestPacketId = playerDataResponse.getRequestPacketId();

                System.out.println("서버로부터 플레이어 데이터가 전달되었습니다 REQUEST PACKET : " + requestPacketId);

                gayaSoftNetworkManager.getWaitingTicket(requestPacketId).setResult(playerDataResponse.getResponseObject());

                break;
            }
            case TARGET_HISTORY_REQUEST: {
                // 특정 History 요청
                TargetHistoryRequest targetHistoryRequest = (TargetHistoryRequest) minecraftPacket;

                Thread thread = new Thread( ()-> {
                    HistoryManager historyManager = HistoryManager.getInstance();
                    PlayerHistory playerHistory = historyManager.getPlayerHistory(targetHistoryRequest.getHistoryNumber());

                    TargetHistoryResponse targetHistoryResponse;

                    if ( playerHistory != null ) {
                        targetHistoryResponse = new TargetHistoryResponse(packetId, ResponseType.SUCCESS, new PlayerHistory[]{playerHistory}, historyManager.getExpireDay());
                    } else {
                        targetHistoryResponse = new TargetHistoryResponse(packetId, ResponseType.FAIL,  "데이터 없음", null, historyManager.getExpireDay());
                    }

                    targetHistoryRequest.handleResponse( ()-> targetHistoryResponse, channel);

                });

                thread.start();

                break;
            }
            case TARGET_PLAYER_DATA_UPDATE_REQUEST: {

                TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest = (TargetPlayerDataUpdateRequest) minecraftPacket;

                TargetPlayerDataUpdateSend targetPlayerDataUpdateSend = new TargetPlayerDataUpdateSend(targetPlayerDataUpdateRequest);

                for (Map.Entry<String, Channel> stringChannelEntry : GayaSoftNetworkManager.getInstance().getMinecraftServerInfoAsChannel().entrySet()) {
                    String serverName = stringChannelEntry.getKey();
                    Channel connectedChannel = stringChannelEntry.getValue();
                    try {
                        connectedChannel.writeAndFlush(targetPlayerDataUpdateSend);
                    } catch (Exception e) {
                        System.out.printf("[%s] 서버에 %s 플레이어 데이터를 업데이트 요청하는데 실패했습니다", serverName, targetPlayerDataUpdateRequest.getTargetPlayerUUID().toString());
                        e.printStackTrace();
                        targetPlayerDataUpdateRequest.handleResponse(()-> new TargetPlayerDataUpdateResponse(packetId, ResponseType.FAIL, e.getMessage(), null), channel);
                        return;
                    }
                }

                targetPlayerDataUpdateRequest.handleResponse(() -> new TargetPlayerDataUpdateResponse(packetId, ResponseType.SUCCESS, "성공", true), channel);

                break;
            }
            case TARGET_PLAYER_HISTORY_REQUEST: {


                TargetPlayerHistoryRequest targetPlayerHistoryRequest = (TargetPlayerHistoryRequest) minecraftPacket;

                Thread thread = new Thread( ()-> {
                    HistoryManager historyManager = HistoryManager.getInstance();
                    List<PlayerHistory> playerHistories = historyManager.getPlayerHistory(targetPlayerHistoryRequest.getTargetPlayerUUID());

                    TargetHistoryResponse targetHistoryResponse;

                    if ( !playerHistories.isEmpty() ) {
                        targetHistoryResponse = new TargetHistoryResponse(packetId, ResponseType.SUCCESS, playerHistories.toArray(new PlayerHistory[0]), historyManager.getExpireDay());
                    } else {
                        targetHistoryResponse = new TargetHistoryResponse(packetId, ResponseType.FAIL,  "데이터 없음", null, historyManager.getExpireDay());
                    }

                    System.out.printf("%s 로그 요청 결과 개수 : %,d\n", targetPlayerHistoryRequest.getTargetPlayerUUID(), playerHistories.size());

                    targetPlayerHistoryRequest.handleResponse( ()-> targetHistoryResponse, channel);

                });

                thread.start();

                break;
            }
            case PLAYER_CONNECTION_HISTORY_SAVE_REQUEST: {
                PlayerConnectionHistorySaveRequest playerConnectionHistorySaveRequest = (PlayerConnectionHistorySaveRequest) minecraftPacket;
                HistoryManager.getInstance().addLog(playerConnectionHistorySaveRequest);
                break;
            }
        }

    }




}

