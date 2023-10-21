package gaya.pe.kr.network.packet.startDirection.server.response;

import gaya.pe.kr.network.packet.global.AbstractResponsePacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.client.request.bukkit.PlayerDataSaveRequest;
import gaya.pe.kr.network.packet.type.ResponseType;
import lombok.Getter;

import java.util.UUID;

/**
 * 플레이어 데이터가 서버측에 잘 저장되었습니다 ~ 할 떄
 * @see PlayerDataSaveRequest
 */
@Getter
public class PlayerDataSaveResponse extends AbstractResponsePacket<Boolean> {

    UUID targetPlayerUUID;
    public PlayerDataSaveResponse(UUID targetPlayerUUID, long requestPacketId, ResponseType responseType) {
        super(PacketType.PLAYER_DATA_SAVE_RESPONSE, requestPacketId, responseType, responseType.equals(ResponseType.SUCCESS));
        this.targetPlayerUUID = targetPlayerUUID;
    }

}
