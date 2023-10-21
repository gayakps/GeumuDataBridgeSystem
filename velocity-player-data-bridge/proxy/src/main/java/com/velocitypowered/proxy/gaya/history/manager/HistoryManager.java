package com.velocitypowered.proxy.gaya.history.manager;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.gaya.databse.DBConnection;
import com.velocitypowered.proxy.gaya.history.thread.HistorySaveScheduler;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.TimeUtil;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class HistoryManager {

    private static class SingleTon {
        private static final HistoryManager HISTORY_MANAGER = new HistoryManager();
    }

    public static HistoryManager getInstance() {
        return SingleTon.HISTORY_MANAGER;
    }

    List<PlayerConnectionHistorySaveRequest> playerConnectionHistorySaveRequests = new ArrayList<>();

    int expireDay = 7;

    Timer timer = new Timer();

    public void init(VelocityServer velocityServer) {
        timer.scheduleAtFixedRate(new HistorySaveScheduler(velocityServer), 0, 40);
    }

    public void setExpireDay(int expireDay) {
        this.expireDay = expireDay;
    }

    public List<PlayerConnectionHistorySaveRequest> getPlayerConnectionHistorySaveRequests() {
        List<PlayerConnectionHistorySaveRequest> playerConnectionHistorySaveRequests1 = new ArrayList<>(playerConnectionHistorySaveRequests);
        playerConnectionHistorySaveRequests.clear();
        return playerConnectionHistorySaveRequests1;
    }

    public void addLog(PlayerConnectionHistorySaveRequest playerConnectionHistorySaveRequest) {
        this.playerConnectionHistorySaveRequests.add(playerConnectionHistorySaveRequest);
        System.out.printf("ADD REQUEST : %s [%,d]\n", playerConnectionHistorySaveRequest.getLogType().name(), this.playerConnectionHistorySaveRequests.size());
    }


    @Nullable
    public PlayerHistory getPlayerHistory(int requestHistoryID) {
        String sql = "SELECT * FROM player_history where history_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, requestHistoryID);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                int historyID = rs.getInt("history_id");
                String server = rs.getString("server_name");

                byte[] inventoryContents = rs.getBytes("inventory");
                byte[] armorContents = rs.getBytes("armor_contents");
                byte[] enderChest = rs.getBytes("ender_chest");
                byte[] pc = rs.getBytes("pc");
                byte[] pocketmon = rs.getBytes("party");
                byte[] offHand = rs.getBytes("off_hand");
                PlayerDataForHistory playerDataForHistory = new PlayerDataForHistory(inventoryContents, armorContents, enderChest, pc, pocketmon, offHand);

                String[] locationData = rs.getString("location").split(",");
                BukkitLocation bukkitLocation = new BukkitLocation(locationData[0], Integer.parseInt(locationData[1]), Integer.parseInt(locationData[2]), Integer.parseInt(locationData[3]));

                PlayerConnectionLogType logType = PlayerConnectionLogType.getPlayerConnectionLogType(rs.getInt("type"));

                Date date = TimeUtil.getSimpleDateFormat().parse(rs.getString("date"));

                return new PlayerHistory(name, playerUUID, historyID, server, playerDataForHistory, bukkitLocation, logType, date);

            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    public List<PlayerHistory> getPlayerHistory(UUID uuid) {

        List<PlayerHistory> histories = new ArrayList<>();

        String sql = "SELECT * FROM player_history where uuid = ?";

        try (Connection connection = DBConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, uuid.toString());

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                int historyID = rs.getInt("history_id");
                String server = rs.getString("server_name");

                byte[] inventoryContents = rs.getBytes("inventory");
                byte[] armorContents = rs.getBytes("armor_contents");
                byte[] enderChest = rs.getBytes("ender_chest");
                byte[] pc = rs.getBytes("pc");
                byte[] pocketmon = rs.getBytes("party");
                byte[] offHand = rs.getBytes("off_hand");
                PlayerDataForHistory playerDataForHistory = new PlayerDataForHistory(inventoryContents, armorContents, enderChest, pc, pocketmon, offHand);

                String[] locationData = rs.getString("location").split(",");
                BukkitLocation bukkitLocation = new BukkitLocation(locationData[0], Integer.parseInt(locationData[1]), Integer.parseInt(locationData[2]), Integer.parseInt(locationData[3]));

                PlayerConnectionLogType logType = PlayerConnectionLogType.getPlayerConnectionLogType(rs.getInt("type"));

                Date date = TimeUtil.getSimpleDateFormat().parse(rs.getString("date"));

                histories.add(new PlayerHistory(name, playerUUID, historyID, server, playerDataForHistory, bukkitLocation, logType, date));
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return histories;

    }

    public boolean isEmptyData() {
        return playerConnectionHistorySaveRequests.size() == 0;
    }

    public int getExpireDay() {
        return expireDay;
    }
}
