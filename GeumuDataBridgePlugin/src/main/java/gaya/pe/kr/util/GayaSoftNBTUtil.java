package gaya.pe.kr.util;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class GayaSoftNBTUtil {

    public static NBTTagCompound byteToNBTTags(byte[] nbtData) {

        if (nbtData != null) {
            try {
                NBTTagCompound nbt = new NBTTagCompound();
                ByteArrayInputStream in = new ByteArrayInputStream(nbtData);
                InputStream is = new ObjectInputStream(in);

                return NBTCompressedStreamTools.a(is);
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        return null;

    }

    public static void setNBT(NBTTagCompound nbtTagCompound, NBTTagCompound newNbtCompound, String attribute) {
        NBTBase attributes = newNbtCompound.get(attribute);

        if ( attributes != null ) {
            nbtTagCompound.set(attribute, attributes);
        }
    }


    public static NBTTagCompound getNowNBTTagCompound(Player targetPlayer) {

        CraftPlayer craftPlayer = (CraftPlayer) targetPlayer;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();

        return entityPlayer.save(nbtTagCompound);

    }

    public static void loadInventoryAndEnderChestNBTTagCompound(Player player, NBTTagCompound nbtTagCompound) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        entityPlayer.loadData(nbtTagCompound);
    }


}
