package gaya.pe.kr.network.packet.startDirection.server.send;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import lombok.Getter;

@Getter
public class JDBCDataSend extends AbstractMinecraftPacket {

    String host;
    int port;
    String db;
    String userName;
    String password;

    public JDBCDataSend(String host, int port, String db, String userName, String password) {
        super(PacketType.JDBC_DATA_SEND);
        this.host = host;
        this.port = port;
        this.db = db;
        this.userName = userName;
        this.password = password;
    }
}
