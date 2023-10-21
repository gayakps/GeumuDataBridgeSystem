package com.velocitypowered.proxy.gaya.history.thread;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.gaya.databse.DBConnection;
import com.velocitypowered.proxy.gaya.history.manager.HistoryManager;
import com.velocitypowered.proxy.gaya.network.GayaSoftNetworkManager;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.TimeUtil;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

public class HistorySaveScheduler extends TimerTask {

    HistoryManager historyManager = HistoryManager.getInstance();
    GayaSoftNetworkManager gayaSoftNetworkManager = GayaSoftNetworkManager.getInstance();

    VelocityServer velocityServer;

    public HistorySaveScheduler(VelocityServer velocityServer) {
        this.velocityServer = velocityServer;
    }

    @Override
    public void run() {

        if ( historyManager.isEmptyData() ) return;

        List<PlayerConnectionHistorySaveRequest> playerConnectionHistorySaveRequestList = HistoryManager.getInstance().getPlayerConnectionHistorySaveRequests();

        if ( playerConnectionHistorySaveRequestList.isEmpty() ) return;

        try (Connection connection = DBConnection.getConnection()) {

            int amount = 0;

            String insertSql = "INSERT INTO player_history (name, uuid, server_name, inventory, armor_contents, ender_chest, pc, party, off_hand, location, type, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertSql);
            connection.setAutoCommit(false); // 배치 작업을 위해 자동 커밋을 끕니다.

            for (PlayerConnectionHistorySaveRequest playerConnectionHistorySaveRequest : playerConnectionHistorySaveRequestList) {

                String serverName = "NONE";

                String ip = playerConnectionHistorySaveRequest.getIp();
                int port = playerConnectionHistorySaveRequest.getPort();

                for (RegisteredServer allServer : velocityServer.getAllServers()) {
                    ServerInfo serverInfo = allServer.getServerInfo();
                    String serverIp = serverInfo.getAddress().getHostString();
                    int serverPort = serverInfo.getAddress().getPort();

                    if ( ip.equals(serverIp) && port == serverPort ) {
                        serverName = serverInfo.getName();
                        break;
                    }

                }

                String playerName = playerConnectionHistorySaveRequest.getPlayerName();
                UUID uuid = playerConnectionHistorySaveRequest.getUuid();


                pstmt.setString(1, playerName);
                pstmt.setString(2, uuid.toString());
                pstmt.setString(3, serverName);
                PlayerDataForHistory playerDataForHistory = playerConnectionHistorySaveRequest.getPlayerDataForHistory();
                pstmt.setBytes(4, playerDataForHistory.getInventoryContents());
                pstmt.setBytes(5, playerDataForHistory.getArmorContents());
                pstmt.setBytes(6, playerDataForHistory.getEnderChest());
                pstmt.setBytes(7, playerDataForHistory.getPc());
                pstmt.setBytes(8, playerDataForHistory.getPoketmon());
                pstmt.setBytes(9, playerDataForHistory.getOffHand());
                BukkitLocation bukkitLocation = playerConnectionHistorySaveRequest.getBukkitLocation();
                pstmt.setString(10, String.format("%s,%d,%d,%d", bukkitLocation.getWorld(), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ()));
                PlayerConnectionLogType playerConnectionLogType = playerConnectionHistorySaveRequest.getLogType();
                pstmt.setInt(11, playerConnectionLogType.getNumber());
                pstmt.setString(12, TimeUtil.getSimpleDateFormat().format(playerConnectionHistorySaveRequest.getDate()));

                pstmt.addBatch();

                if (amount % 100 == 0) { // 100개마다 배치 쿼리 실행
                    pstmt.executeBatch();
                }

                amount++;

            }

            pstmt.executeBatch(); // 남아있는 배치 작업들 실행
            connection.commit();  // 데이터베이스에 커밋

        } catch ( Exception e) {
            e.printStackTrace();
        }


    }
}
