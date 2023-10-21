package gaya.pe.kr.util.data;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class BukkitLocation implements Serializable {

    String world;
    int x;
    int y;
    int z;

    public BukkitLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     *
     * @param str ( ex) str : worldname,x,y,z return : BukkitLocation(worldName,x,y,z)
     * @return
     */
    public static BukkitLocation getBukkitLocation(String str) {

        String[] strings = str.split(",");

        return new BukkitLocation(strings[0], Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));

    }

}
