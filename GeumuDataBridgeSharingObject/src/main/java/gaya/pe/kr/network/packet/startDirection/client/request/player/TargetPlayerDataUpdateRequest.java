package gaya.pe.kr.network.packet.startDirection.client.request.player;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.AbstractMinecraftRequestPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetPlayerDataUpdateResponse;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 요청자의 인벤토리 및 데이터를 특정 플레이어에게 전달시켜 업데이트 시켜주는 방식임.
 */

@Getter
public class TargetPlayerDataUpdateRequest extends AbstractMinecraftRequestPacket<TargetPlayerDataUpdateResponse> {

    UUID targetPlayerUUID;

    PlayerDataForHistory playerDataForHistory;

    UpdateType updateType;

    public TargetPlayerDataUpdateRequest(UUID targetPlayerUUID, byte[] inventory,  byte[] armorContents, byte[] enderChest, byte[] pc, byte[] poketmon, byte[] offHand, UpdateType updateType) {
        super(PacketType.TARGET_PLAYER_DATA_UPDATE_REQUEST);
        this.targetPlayerUUID = targetPlayerUUID;
        this.playerDataForHistory = new PlayerDataForHistory(inventory, armorContents, enderChest, pc, poketmon, offHand);
        this.updateType = updateType;
    }

    public TargetPlayerDataUpdateRequest(UUID targetPlayerUUID, PlayerDataForHistory playerDataForHistory, UpdateType updateType) {
        super(PacketType.TARGET_PLAYER_DATA_UPDATE_REQUEST);
        this.targetPlayerUUID = targetPlayerUUID;
        this.playerDataForHistory = playerDataForHistory;
        this.updateType = updateType;
    }

    @Override
    public void handleResponse(Supplier<TargetPlayerDataUpdateResponse> supplier, Channel sendTargetChannel) throws RuntimeException {
        sendTargetChannel.writeAndFlush(supplier.get());
    }

    public enum UpdateType {

        INVENTORY,
        ENDER_CHEST,
        POKETMON,
        INVENTORY_ENDER_CHEST,
        ALL;

    }

}
