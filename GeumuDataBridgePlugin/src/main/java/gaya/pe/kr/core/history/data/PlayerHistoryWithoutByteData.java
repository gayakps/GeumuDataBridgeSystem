package gaya.pe.kr.core.history.data;

import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.TimeUtil;
import gaya.pe.kr.util.data.BukkitLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlayerHistoryWithoutByteData {

    String name ;
    UUID playerUUID ;
    int historyID ;
    String server;
    BukkitLocation bukkitLocation;
    PlayerConnectionLogType logType;

    Date date;

}
