package gaya.pe.kr.nbt;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.CompoundNBT;

public class CompressedStreamTools {
   public static CompoundNBT readCompressed(File file) throws IOException {
      CompoundNBT compoundnbt;
      try (InputStream inputstream = new FileInputStream(file)) {
         compoundnbt = readCompressed(inputstream);
      }

      return compoundnbt;
   }

   /**
    * Load the gzipped compound from the inputstream.
    */
   public static CompoundNBT readCompressed(InputStream is) throws IOException {
      CompoundNBT compoundnbt;
      try (DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)))) {
         compoundnbt = read(datainputstream, NBTSizeTracker.INFINITE);
      }

      return compoundnbt;
   }


   /**
    * Write the compound, gzipped, to the outputstream.
    */


   @Nullable
   public static CompoundNBT read(File fileIn) throws IOException {
      if (!fileIn.exists()) {
         return null;
      } else {
         CompoundNBT compoundnbt;
         try (
            FileInputStream fileinputstream = new FileInputStream(fileIn);
            DataInputStream datainputstream = new DataInputStream(fileinputstream);
         ) {
            compoundnbt = read(datainputstream, NBTSizeTracker.INFINITE);
         }

         return compoundnbt;
      }
   }

   /**
    * Reads from a CompressedStream.
    */
   public static CompoundNBT read(DataInput inputStream) throws IOException {
      return read(inputStream, NBTSizeTracker.INFINITE);
   }

   /**
    * Reads the given DataInput, constructs, and returns an NBTTagCompound with the data from the DataInput
    */
   public static CompoundNBT read(DataInput input, NBTSizeTracker accounter) throws IOException {
      INBT inbt = read(input, 0, accounter);
      if (inbt instanceof CompoundNBT) {
         return (CompoundNBT)inbt;
      }
      else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }


   private static void writeTag(INBT tag, DataOutput output) throws IOException {
      output.writeByte(tag.getId());
      if (tag.getId() != 0) {
         output.writeUTF("");
         tag.write(output);
      }
   }

   private static INBT read(DataInput input, int depth, NBTSizeTracker accounter) throws IOException {
      byte b0 = input.readByte();
      accounter.read(8); // Forge: Count everything!
      if (b0 == 0) {
         return EndNBT.INSTANCE;
      } else {
         accounter.readUTF(input.readUTF()); //Forge: Count this string.
         accounter.read(32); //Forge: 4 extra bytes for the object allocation.
         try {
            return NBTTypes.getGetTypeByID(b0).readNBT(input, depth, accounter);
         } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addDetail("Tag type", b0);
            throw new ReportedException(crashreport);
         }
      }
   }
}
