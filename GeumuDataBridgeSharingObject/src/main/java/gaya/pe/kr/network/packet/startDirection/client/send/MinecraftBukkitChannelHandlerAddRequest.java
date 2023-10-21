package gaya.pe.kr.network.packet.startDirection.client.send;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import lombok.Getter;

@Getter
public class MinecraftBukkitChannelHandlerAddRequest extends AbstractMinecraftPacket {

    String ip;
    int port;

    public MinecraftBukkitChannelHandlerAddRequest(String ip, int port) {
        super(PacketType.MINECRAFT_BUKKIT_CHANNEL_HANDLER_ADD_REQUEST);
        this.ip = ip;
        this.port = port;
    }
}
