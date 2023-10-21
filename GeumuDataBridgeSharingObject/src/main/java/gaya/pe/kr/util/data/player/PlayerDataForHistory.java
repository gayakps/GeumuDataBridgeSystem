package gaya.pe.kr.util.data.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;


@Getter
@AllArgsConstructor
public class PlayerDataForHistory implements Serializable {

    byte[] inventoryContents;

    byte[] armorContents;
    byte[] enderChest;

    byte[] pc;
    byte[] poketmon;

    byte[] offHand; //


}
