package gaya.pe.kr.util.data.player;

import java.io.Serializable;
import java.util.UUID;

public class PlayerPersistentData implements Serializable {

    UUID playerUUID;

    byte[] playerNBTData;

    byte[] effectList;

    byte[] pcData;
    byte[] poketmonData;
    GameMode gameMode;

//    double balance;

    private PlayerPersistentData(PlayerPersistentDataBuilder playerPersistentDataBuilder) {
        this.playerUUID = playerPersistentDataBuilder.playerUUID;
        this.playerNBTData = playerPersistentDataBuilder.playerNBTData;
        this.gameMode = playerPersistentDataBuilder.gameMode;
        this.effectList = playerPersistentDataBuilder.effectList;
        this.pcData = playerPersistentDataBuilder.pcData;
        this.poketmonData = playerPersistentDataBuilder.poketmonData;
//        this.balance = playerPersistentDataBuilder.balance;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setPlayerNBTData(byte[] playerNBTData) {
        this.playerNBTData = playerNBTData;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public byte[] getPlayerNBTData() {
        return playerNBTData;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public byte[] getEffectList() {
        return effectList;
    }

    public void setEffectList(byte[] effectList) {
        this.effectList = effectList;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public byte[] getPcData() {
        return pcData;
    }

    public void setPcData(byte[] pcData) {
        this.pcData = pcData;
    }

    public byte[] getPoketmonData() {
        return poketmonData;
    }

    public void setPoketmonData(byte[] poketmonData) {
        this.poketmonData = poketmonData;
    }

    //    public double getBalance() {
//        return balance;
//    }
//
//    public void setBalance(double balance) {
//        this.balance = balance;
//    }

    public static class PlayerPersistentDataBuilder {

        // 필수
        UUID playerUUID;
        byte[] playerNBTData;
        GameMode gameMode = GameMode.SURVIVAL;

        byte[] effectList;

        byte[] pcData;
        byte[] poketmonData;

//        double balance = -1;

        public PlayerPersistentDataBuilder(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        // 옵션
        public PlayerPersistentDataBuilder setEffects(byte[] effects) {
            effectList = effects;
            return this;
        }

        public PlayerPersistentDataBuilder setPlayerData(byte[] nbtData) {
            playerNBTData = nbtData;
            return this;
        }

        public PlayerPersistentDataBuilder setPcData(byte[] pcData) {
            this.pcData = pcData;
            return this;
        }

        public PlayerPersistentDataBuilder setPoketmonData(byte[] poketmonData) {
            this.poketmonData = poketmonData;
            return this;
        }

        public PlayerPersistentDataBuilder setPlayerGameMode(GameMode mode) {
            this.gameMode = mode;
            return this;
        }

//        public PlayerPersistentDataBuilder setBalance(double balance) {
//            this.balance = balance;
//            return this;
//        }

        public PlayerPersistentData build() {
            return new PlayerPersistentData(this);
        }



    }



}
