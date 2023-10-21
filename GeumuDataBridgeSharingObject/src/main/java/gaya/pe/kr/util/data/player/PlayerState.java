package gaya.pe.kr.util.data.player;

import gaya.pe.kr.util.converter.ObjectConverter;

import java.io.Serializable;

public class PlayerState implements Serializable {

    byte[] effectList;
    int totalExp;
    int level;
    double exp;
    GameMode gameMode;
    double maxHealth;
    double nowHealth;

    private PlayerState(PlayerStateBuilder playerStateBuilder) {
        totalExp = playerStateBuilder.totalExp;
        level = playerStateBuilder.level;
        exp = playerStateBuilder.exp;
        effectList = playerStateBuilder.effectList;
        gameMode = playerStateBuilder.gameMode;
        nowHealth = playerStateBuilder.nowHealth;
        maxHealth = playerStateBuilder.maxHealth;
    }

    public byte[] getEffectList() {
        return effectList;
    }

    public void setEffectList(byte[] effectList) {
        this.effectList = effectList;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(int totalExp) {
        this.totalExp = totalExp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getNowHealth() {
        return nowHealth;
    }

    public void setNowHealth(double nowHealth) {
        this.nowHealth = nowHealth;
    }

    public static class PlayerStateBuilder {

        byte[] effectList = null;
        int totalExp = 0;
        int level = 0;
        double exp = 0;
        GameMode gameMode = GameMode.SURVIVAL;
        double maxHealth = 20;
        double nowHealth = 20;

        public PlayerStateBuilder setEffectList(byte[] effectList) {
            this.effectList = effectList;
            return this;
        }

        public PlayerStateBuilder setEffectList(String effectList) {
            if ( effectList != null ) this.effectList = ObjectConverter.getStringAsByte(effectList);
            return this;
        }

        public PlayerStateBuilder setTotalExp(int totalExp) {
            this.totalExp = totalExp;
            return this;
        }

        public PlayerStateBuilder setLevel(int level) {
            this.level = level;
            return this;
        }

        public PlayerStateBuilder setExp(double exp) {
            this.exp = exp;
            return this;
        }

        public PlayerStateBuilder setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public PlayerStateBuilder setMaxHealth(double maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        public PlayerStateBuilder setNowHealth(double nowHealth) {
            this.nowHealth = nowHealth;
            return this;
        }

        public PlayerState build() {
            return new PlayerState(this);
        }

    }


}
