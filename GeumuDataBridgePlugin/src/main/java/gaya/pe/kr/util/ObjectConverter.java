package gaya.pe.kr.util;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ObjectConverter {

    public static String getObjectAsString(Object object) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(object);
            os.flush();
            byte[] serializedObject = io.toByteArray();
            return Base64.getEncoder().encodeToString(serializedObject);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getObjectAsBytes(Object object) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(object);
            os.flush();
            return io.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Object getObject(String encodedObject) {
        try {
            byte[] serializedObject = Base64.getDecoder().decode(encodedObject);
            ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);
            return is.readObject();
        } catch ( Exception e) {
            return null;
        }
    }


    public static Object getObject(byte[] bytes) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);
            return is.readObject();
        } catch ( Exception e) {
            return null;
        }
    }



}
