package gaya.pe.kr.core.history.manager;

import gaya.pe.kr.core.GeumuDataBridgePlugin;
import gaya.pe.kr.core.history.command.HistoryCommand;
import gaya.pe.kr.core.history.data.PlayerHistoryWithoutByteData;
import gaya.pe.kr.core.history.db.DBConnection;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.TimeUtil;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HistoryServiceManager {

    private static class SingleTon {
        private static final HistoryServiceManager HISTORY_SERVICE_MANAGER = new HistoryServiceManager();
    }

    public static HistoryServiceManager getInstance() {
        return SingleTon.HISTORY_SERVICE_MANAGER;
    }

    @Setter
    @Getter
    int expireDay;

    public void init() {

        HistoryCommand historyCommand = new HistoryCommand();
        GeumuDataBridgePlugin.registerCommand("db", historyCommand);
        GeumuDataBridgePlugin.registerTabCommand("db", historyCommand);

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

    public List<PlayerHistoryWithoutByteData> getPlayerHistoryWithoutByteData(UUID uuid) {

        List<PlayerHistoryWithoutByteData> histories = new ArrayList<>();

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

                String[] locationData = rs.getString("location").split(",");
                BukkitLocation bukkitLocation = new BukkitLocation(locationData[0], Integer.parseInt(locationData[1]), Integer.parseInt(locationData[2]), Integer.parseInt(locationData[3]));

                PlayerConnectionLogType logType = PlayerConnectionLogType.getPlayerConnectionLogType(rs.getInt("type"));

                Date date = TimeUtil.getSimpleDateFormat().parse(rs.getString("date"));

                histories.add(new PlayerHistoryWithoutByteData(name, playerUUID, historyID, server, bukkitLocation, logType, date));
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return histories;

    }

}
