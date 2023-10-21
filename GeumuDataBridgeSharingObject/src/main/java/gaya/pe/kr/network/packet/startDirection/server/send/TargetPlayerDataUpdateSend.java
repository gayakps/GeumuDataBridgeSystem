package gaya.pe.kr.network.packet.startDirection.server.send;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerDataUpdateRequest;
import lombok.Getter;

@Getter
public class TargetPlayerDataUpdateSend extends AbstractMinecraftPacket {

    TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest;

    public TargetPlayerDataUpdateSend(TargetPlayerDataUpdateRequest targetPlayerDataUpdateRequest) {
        super(PacketType.TARGET_PLAYER_DATA_UPDATE_SEND);
        this.targetPlayerDataUpdateRequest = targetPlayerDataUpdateRequest;
    }

}
