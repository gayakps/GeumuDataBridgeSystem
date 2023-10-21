package gaya.pe.kr.util.converter;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ObjectConverter {

    public static byte[] getByteFromObject(Object object) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(io);
            os.writeObject(object);
            os.flush();
            return io.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ByteBuf getByteBufFromObject(Object object) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(io);
            os.writeObject(object);
            os.flush();
            byte[] serializedData = io.toByteArray();

            ByteBuf compressed = Unpooled.buffer();
            ByteBufOutputStream out = new ByteBufOutputStream(compressed);
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(serializedData);
            gzip.finish();
            gzip.close();
            out.close();

            return compressed;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Object getObject(byte[] serializedObject) {
        // byte[] -> object
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        } catch ( Exception e) {
            return null;
        }
    }

    public static <T extends AbstractMinecraftPacket> T getMinecraftPacket(ByteBuf byteBuf, Class<T> packetClass) {
        try {
            // 압축 해제
            byte[] compressedData = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(compressedData);
            ByteArrayInputStream compressedInputStream = new ByteArrayInputStream(compressedData);
            GZIPInputStream gzipInputStream = new GZIPInputStream(compressedInputStream);
            ByteArrayOutputStream decompressedOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipInputStream.read(buffer)) > 0) {
                decompressedOutputStream.write(buffer, 0, length);
            }
            gzipInputStream.close();
            decompressedOutputStream.close();
            byte[] decompressedData = decompressedOutputStream.toByteArray();

            // 직렬화된 데이터 복원
            ByteArrayInputStream serializedInputStream = new ByteArrayInputStream(decompressedData);
            ObjectInputStream objectInputStream = new ObjectInputStream(serializedInputStream);
            Object object = objectInputStream.readObject();
            byteBuf.release();

            return packetClass.cast(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }





    public static String getObjectAsString(byte[] serializedObject) {
        // byte[] -> object
        try {
            return Base64.getEncoder().encodeToString(serializedObject);
        } catch ( Exception e) {
            return null;
        }
    }


    public static byte[] getStringAsByte(String serializedObject) {
        // byte[] -> object
        try {
            return Base64.getDecoder().decode(serializedObject);
        } catch ( Exception e) {
            return null;
        }
    }




}