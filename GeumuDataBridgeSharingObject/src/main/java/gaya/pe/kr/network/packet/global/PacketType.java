package gaya.pe.kr.network.packet.global;

import gaya.pe.kr.network.packet.startDirection.client.send.MinecraftBukkitChannelHandlerAddRequest;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.bukkit.PlayerDataSaveRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerHistoryRequest;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataClientLoadResponse;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataResponse;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataClientLoadRequest;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataRequest;
import gaya.pe.kr.network.packet.startDirection.server.response.PlayerDataSaveResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetPlayerDataUpdateResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetHistoryResponse;
import gaya.pe.kr.network.packet.startDirection.server.send.JDBCDataSend;
import gaya.pe.kr.network.packet.startDirection.server.send.TargetPlayerDataUpdateSend;
import lombok.Getter;

import java.util.HashMap;

@Getter
public enum PacketType {

    MINECRAFT_BUKKIT_CHANNEL_HANDLER_ADD_REQUEST(0x00, PacketStartDirection.CLIENT, MinecraftBukkitChannelHandlerAddRequest.class),
    PLAYER_DATA_SAVE_REQUEST(0x01, PacketStartDirection.SERVER, PlayerDataSaveRequest.class),
    PLAYER_DATA_CLIENT_LOAD_RESPONSE(0x02, PacketStartDirection.SERVER, PlayerDataClientLoadResponse.class),
    PLAYER_DATA_RESPONSE(0x03, PacketStartDirection.SERVER, PlayerDataResponse.class),
    PLAYER_DATA_CLIENT_LOAD_REQUEST(0x04, PacketStartDirection.CLIENT, PlayerDataClientLoadRequest.class),
    PLAYER_DATA_REQUEST(0x05, PacketStartDirection.CLIENT, PlayerDataRequest.class),
    PLAYER_DATA_SAVE_RESPONSE(0x06, PacketStartDirection.CLIENT, PlayerDataSaveResponse.class),
    PLAYER_CONNECTION_HISTORY_SAVE_REQUEST(0x07, PacketStartDirection.CLIENT, PlayerConnectionHistorySaveRequest.class),
    TARGET_PLAYER_DATA_UPDATE_REQUEST(0x08, PacketStartDirection.CLIENT, TargetPlayerDataUpdateRequest.class),
    TARGET_PLAYER_HISTORY_REQUEST(0x09, PacketStartDirection.CLIENT, TargetPlayerHistoryRequest.class),
    TARGET_HISTORY_RESPONSE(0x10, PacketStartDirection.CLIENT, TargetHistoryResponse.class),
    TARGET_HISTORY_REQUEST(0x11, PacketStartDirection.CLIENT, TargetHistoryRequest.class),

    TARGET_PLAYER_DATA_UPDATE_SEND(0x12, PacketStartDirection.SERVER, TargetPlayerDataUpdateSend.class),
    TARGET_PLAYER_DATA_UPDATE_RESPONSE(0x13, PacketStartDirection.SERVER, TargetPlayerDataUpdateResponse.class),

    JDBC_DATA_SEND(0x14, PacketStartDirection.SERVER, JDBCDataSend.class);


    private final byte id;
    private final PacketStartDirection packetStartDirection;
    private final Class<? extends AbstractMinecraftPacket> clazz;
    private static HashMap<Byte, PacketType> packetTypeHashMap = new HashMap<>();
    private static HashMap<Class<?>, PacketType> classTypeAsPacketTypeHashMap = new HashMap<>();

    static {
        for (PacketType value : PacketType.values()) {
            packetTypeHashMap.put(value.getId(), value);
            classTypeAsPacketTypeHashMap.put(value.getClazz(), value);
        }
    }

    PacketType(int id, PacketStartDirection packetStartDirection, Class<? extends AbstractMinecraftPacket> clazz) {
        this.id = (byte) id;
        this.packetStartDirection = packetStartDirection;
        this.clazz = clazz;
    }

    public static PacketType fromId(byte id) {

        PacketType packetType = packetTypeHashMap.get(id);

        if ( packetType == null ) throw new IllegalArgumentException("알 수 없는 패킷 ID 입니다 : " + id);

        return packetType;

    }

    public static PacketType fromClass(Class<?> clazz) {

        PacketType packetType = classTypeAsPacketTypeHashMap.get(clazz);

        if ( packetType == null ) throw new IllegalArgumentException("알 수 없는 패킷 Class 입니다 : " + clazz.getSimpleName());

        return packetType;

    }

}
