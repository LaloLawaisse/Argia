/*     */ package application;
/*     */ 
/*     */ 
          import com.google.crypto.tink.CleartextKeysetHandle;
/*     */ import com.google.crypto.tink.JsonKeysetReader;
/*     */ import com.google.crypto.tink.KeysetHandle;
/*     */ import com.google.crypto.tink.KeysetReader;
/*     */ import com.google.crypto.tink.StreamingAead;
/*     */ import com.google.crypto.tink.streamingaead.StreamingAeadConfig;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.channels.ReadableByteChannel;
/*     */ import java.nio.channels.WritableByteChannel;
/*     */ import java.security.GeneralSecurityException;
/*     */ 
/*     */ 
/*     */ public class Encryptor
/*     */ {
/*  24 */   private final byte[] aad = new byte[] { 2, 50, 34, 17, 36, 67, 121, 21, 69, 35, 2, 86, 68, 19, 85, 85 };
/*     */   private File outfile;
/*     */   private File infile;
/*     */   private final KeysetHandle keysetHandle;
/*     */   private final StreamingAead streamingAead;
/*     */   
/*     */   public Encryptor() throws GeneralSecurityException, IOException {
/*  31 */     StreamingAeadConfig.register();
/*  32 */     this.keysetHandle = CleartextKeysetHandle.read((KeysetReader)JsonKeysetReader.withString(getKey()));
/*  33 */     this.streamingAead = (StreamingAead)this.keysetHandle.getPrimitive(StreamingAead.class);
/*     */   }
/*     */ 
/*     */   
/*     */   public Encryptor(String inputfile) throws IOException, GeneralSecurityException {
/*  38 */     StreamingAeadConfig.register();
/*  39 */     this.keysetHandle = CleartextKeysetHandle.read((KeysetReader)JsonKeysetReader.withString(getKey()));
/*  40 */     this.streamingAead = (StreamingAead)this.keysetHandle.getPrimitive(StreamingAead.class);
/*     */     
/*  42 */     this.infile = new File(inputfile);
/*  43 */     this.outfile = new File(String.valueOf(inputfile) + "aux");
/*  44 */     if (!this.outfile.exists())
/*  45 */       this.outfile.createNewFile(); 
/*     */   }
/*     */   
/*     */   public Encryptor(String inputfile, String outputfile) throws IOException, GeneralSecurityException {
/*  49 */     StreamingAeadConfig.register();
/*  50 */     this.keysetHandle = CleartextKeysetHandle.read((KeysetReader)JsonKeysetReader.withString(getKey()));
/*  51 */     this.streamingAead = (StreamingAead)this.keysetHandle.getPrimitive(StreamingAead.class);
/*     */     
/*  53 */     this.infile = new File(inputfile);
/*  54 */     this.outfile = new File(outputfile);
/*  55 */     if (!this.outfile.exists())
/*  56 */       this.outfile.createNewFile(); 
/*     */   }
/*     */   
/*     */   public void setInputFile(File inputFile) {
/*  60 */     this.infile = inputFile;
/*     */   }
/*     */   
/*     */   public void setOutputFile(File outputFile) {
/*  64 */     this.outfile = outputFile;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void encrypt() throws IOException, GeneralSecurityException {
/*  70 */     FileOutputStream fos = new FileOutputStream(this.outfile);
/*  71 */     FileChannel ciphertextDestination = fos.getChannel();
/*  72 */     WritableByteChannel encryptingChannel = this.streamingAead.newEncryptingChannel(ciphertextDestination, this.aad);
/*  73 */     InputStream is = new FileInputStream(this.infile.getAbsolutePath());
/*  74 */     ByteBuffer buffer = ByteBuffer.allocate((int)this.infile.length());
/*  75 */     while (is.read(buffer.array()) != -1) {
/*  76 */       encryptingChannel.write(buffer);
/*     */     }
/*     */ 
/*     */     
/*  80 */     encryptingChannel.close();
/*  81 */     fos.close();
/*  82 */     is.close();
/*  83 */     this.infile.delete();
/*  84 */     this.outfile.renameTo(this.infile);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void decrypt() throws GeneralSecurityException, IOException {
/*  90 */     FileInputStream fileInputStream = new FileInputStream(this.infile);
/*  91 */     FileChannel cipherTextSource = fileInputStream.getChannel();
/*  92 */     ReadableByteChannel decryptingChannel = this.streamingAead.newDecryptingChannel(cipherTextSource, this.aad);
/*  93 */     ByteBuffer buffer = ByteBuffer.allocate((int)this.infile.length());
/*  94 */     OutputStream out = new FileOutputStream(this.outfile);
/*  95 */     while (decryptingChannel.read(buffer) != -1) {
/*  96 */       out.write(buffer.array());
/*     */     }
/*  98 */     decryptingChannel.close();
/*  99 */     out.close();
/* 100 */     fileInputStream.close();
/* 101 */     this.infile.delete();
/* 102 */     this.outfile.renameTo(this.infile);
/*     */   }
/*     */   
/*     */   public void decrypt(File inputFile) throws GeneralSecurityException, IOException {
/* 106 */     File outputFile = new File(inputFile + "aux");
/* 107 */     if (!outputFile.exists()) {
/* 108 */       outputFile.createNewFile();
/*     */     }
/* 110 */     FileInputStream fileInputStream = new FileInputStream(inputFile);
/* 111 */     FileChannel cipherTextSource = fileInputStream.getChannel();
/* 112 */     ReadableByteChannel decryptingChannel = this.streamingAead.newDecryptingChannel(cipherTextSource, this.aad);
/* 113 */     ByteBuffer buffer = ByteBuffer.allocate((int)inputFile.length());
/* 114 */     OutputStream out = new FileOutputStream(outputFile);
/* 115 */     while (decryptingChannel.read(buffer) != -1) {
/* 116 */       out.write(buffer.array());
/*     */     }
/* 118 */     decryptingChannel.close();
/* 119 */     out.close();
/* 120 */     fileInputStream.close();
/* 121 */     inputFile.delete();
/* 122 */     outputFile.renameTo(inputFile);
/*     */   }
/*     */   
/*     */   public void decrypt(File inputFile, File outputFile) throws GeneralSecurityException, IOException {
/* 126 */     FileInputStream fileInputStream = new FileInputStream(inputFile);
/* 127 */     FileChannel cipherTextSource = fileInputStream.getChannel();
/* 128 */     ReadableByteChannel decryptingChannel = this.streamingAead.newDecryptingChannel(cipherTextSource, this.aad);
/* 129 */     ByteBuffer buffer = ByteBuffer.allocate((int)inputFile.length());
/* 130 */     OutputStream out = new FileOutputStream(outputFile);
/* 131 */     while (decryptingChannel.read(buffer) != -1) {
/* 132 */       out.write(buffer.array());
/*     */     }
/* 134 */     decryptingChannel.close();
/* 135 */     out.close();
/* 136 */     fileInputStream.close();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private final String getKey() {
/* 142 */     return "{\n    \"primaryKeyId\": 1658370469,\n    \"key\": [{\n        \"keyData\": {\n            \"typeUrl\": \"type.googleapis.com/google.crypto.tink.AesCtrHmacStreamingKey\",\n            \"keyMaterialType\": \"SYMMETRIC\",\n            \"value\": \"Eg0IgCAQEBgDIgQIAxAgGhCLR6mbaYWa6ZoNGF3/qpZr\"\n        },\n        \"outputPrefixType\": \"RAW\",\n        \"keyId\": 1658370469,\n        \"status\": \"ENABLED\"\n    }]\n}";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public File getInfile() {
/* 158 */     return this.infile;
/*     */   }
/*     */ }


/* Location:              C:\Users\mmula\Downloads\TL_Dec\TrafficLight_Decipher.jar!\application\Encryptor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */