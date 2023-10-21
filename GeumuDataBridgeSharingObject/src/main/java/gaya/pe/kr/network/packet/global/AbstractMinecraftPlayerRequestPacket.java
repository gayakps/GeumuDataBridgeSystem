package gaya.pe.kr.network.packet.global;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public abstract class AbstractMinecraftPlayerRequestPacket extends AbstractMinecraftPacket {

    String playerName;
    UUID playerUUID;

    public AbstractMinecraftPlayerRequestPacket(PacketType type, String playerName, UUID playerUUID) {
        super(type);
        this.playerName = playerName;
        this.playerUUID = playerUUID;
    }

    public AbstractMinecraftPlayerRequestPacket(PacketType type, Player player) {
        super(type);
        this.playerName = player.getName();
        this.playerUUID = player.getUniqueId();
    }


}
