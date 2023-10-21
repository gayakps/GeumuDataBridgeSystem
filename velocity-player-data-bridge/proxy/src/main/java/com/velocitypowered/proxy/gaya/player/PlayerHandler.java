package com.velocitypowered.proxy.gaya.player;

import com.velocitypowered.proxy.gaya.databse.DBConnection;
import gaya.pe.kr.network.packet.startDirection.client.request.bukkit.PlayerDataSaveRequest;
import gaya.pe.kr.network.packet.startDirection.server.response.PlayerDataSaveResponse;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.converter.ObjectConverter;
import gaya.pe.kr.util.data.player.GameMode;
import gaya.pe.kr.util.data.player.PlayerPersistentData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerHandler {

    public static HashMap<UUID, PlayerPersistentData> playerPersistentDataHashMap = new HashMap<>();

    public static boolean savePlayerData(PlayerPersistentData saveTarget, boolean removeData) {
        String sql = "insert into player_data SET " +
                "player_uuid = ?, nbt_data = ?, game_mode = ?, effects = ?, poketmon_data = ?, pc_data = ?" +
                " ON DUPLICATE KEY UPDATE nbt_data = ?, game_mode = ?, effects = ?, poketmon_data = ?, pc_data = ?";

        try (Connection connection = DBConnection.getConnection() ) {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            byte[] nbtDataBytes = saveTarget.getPlayerNBTData();

            String nbtDataStr = null;

            if ( nbtDataBytes != null ) {
                nbtDataStr = ObjectConverter.getObjectAsString(nbtDataBytes);
            }

            GameMode gameMode =  saveTarget.getGameMode();

            preparedStatement.setString(1, saveTarget.getPlayerUUID().toString());
            preparedStatement.setString(2, nbtDataStr);
            preparedStatement.setString(3, gameMode.name());


            byte[] effectByte = saveTarget.getEffectList();
            String effectStr = null;

            if ( effectByte != null ) {
                effectStr = ObjectConverter.getObjectAsString(effectByte);
            }

            preparedStatement.setString(4, effectStr);

            byte[] poketmonData = saveTarget.getPoketmonData();

            preparedStatement.setBytes(5, poketmonData);

            byte[] pcData = saveTarget.getPcData();

            preparedStatement.setBytes(6, pcData);

            preparedStatement.setString(7, nbtDataStr);
            preparedStatement.setString(8, gameMode.name());
            preparedStatement.setString(9, effectStr);
            preparedStatement.setBytes(10, poketmonData);
            preparedStatement.setBytes(11, pcData);

            preparedStatement.executeUpdate();
            System.out.println(saveTarget.getPlayerUUID().toString() + " 데이터 저장성공");

            if ( removeData ) {
                PlayerHandler.playerPersistentDataHashMap.remove(saveTarget.getPlayerUUID());
            }

            return true;

        } catch ( SQLException e ) {
            e.printStackTrace();
            System.out.println("데이터 저장 중 오류 발생, 추후 입장할 때 해당 데이터로 로드를 진행합니다");
        }

        return false;
    }


}
