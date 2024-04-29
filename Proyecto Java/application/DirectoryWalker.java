/*    */ package application;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.nio.file.FileVisitResult;
/*    */ import java.nio.file.FileVisitor;
/*    */ import java.nio.file.Files;
/*    */ import java.nio.file.Path;
/*    */ import java.nio.file.attribute.BasicFileAttributes;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class DirectoryWalker
/*    */   implements FileVisitor<Path> {
/* 14 */   private List<Path> pathes = new ArrayList<>();
/* 15 */   long totalSize = 0L;
/*    */   
/*    */   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
/* 18 */     return FileVisitResult.CONTINUE;
/*    */   }
/*    */ 
/*    */   
/*    */   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
/* 23 */     if (Files.isRegularFile(file, new java.nio.file.LinkOption[0]) && file.getFileName().toString().endsWith("mp4")) {
/* 24 */       this.pathes.add(file);
/* 25 */       this.totalSize += attrs.size();
/*    */     } 
/* 27 */     return FileVisitResult.CONTINUE;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
/* 33 */     return FileVisitResult.CONTINUE;
/*    */   }
/*    */ 
/*    */   
/*    */   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
/* 38 */     return FileVisitResult.CONTINUE;
/*    */   }
/*    */   
/*    */   public List<Path> getPathes() {
/* 42 */     return this.pathes;
/*    */   }
/*    */ 
/*    */   
/*    */   public long getTotalSize() {
/* 47 */     return this.totalSize;
/*    */   }
/*    */ }


/* Location:              C:\Users\mmula\Downloads\TL_Dec\TrafficLight_Decipher.jar!\application\DirectoryWalker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */