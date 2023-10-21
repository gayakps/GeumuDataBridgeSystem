package gaya.pe.kr.network.packet.type;

public enum PlayerConnectionLogType {

    NONE(-1),
    LOGOUT(1),
    LOGIN(2),
    AUTO_UPDATE(3);

    final int number;

    PlayerConnectionLogType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static PlayerConnectionLogType getPlayerConnectionLogType(int number) {

        switch ( number ) {
            case 1 : {
                return LOGOUT;
            }
            case 2 : {
                return LOGIN;
            }
            case 3 : {
                return AUTO_UPDATE;
            }
        }

        return NONE;

    }

}
