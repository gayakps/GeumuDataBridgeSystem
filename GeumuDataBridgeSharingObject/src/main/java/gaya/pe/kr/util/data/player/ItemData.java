package gaya.pe.kr.util.data.player;

import java.io.Serializable;

public class ItemData implements Serializable {

    byte[] playerItemData;
    byte[] enderChestItemData;

//    byte[] armorWorkShopData;

//    public byte[] getArmorWorkShopData() {
//        return armorWorkShopData;
//    }

//    public void setArmorWorkShopData(byte[] armorWorkShopData) {
//        this.armorWorkShopData = armorWorkShopData;
//    }

    public byte[] getPlayerItemData() {
        return playerItemData;
    }

    public void setPlayerItemData(byte[] playerItemData) {
        this.playerItemData = playerItemData;
    }

    public byte[] getEnderChestItemData() {
        return enderChestItemData;
    }

    public void setEnderChestItemData(byte[] enderChestItemData) {
        this.enderChestItemData = enderChestItemData;
    }
}
