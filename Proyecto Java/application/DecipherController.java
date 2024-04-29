package application;


/*     */ import java.io.File;
/*     */ import java.nio.file.FileAlreadyExistsException;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.Paths;
/*     */ import java.security.GeneralSecurityException;
/*     */ import java.util.List;
/*     */ import java.util.Optional;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import javafx.application.Platform;
/*     */ import javafx.beans.property.DoubleProperty;
/*     */ import javafx.beans.property.SimpleDoubleProperty;
/*     */ import javafx.concurrent.Task;
/*     */ import javafx.concurrent.WorkerStateEvent;
/*     */ import javafx.event.ActionEvent;
/*     */ import javafx.fxml.FXML;
/*     */ import javafx.scene.control.Alert;
/*     */ import javafx.scene.control.Button;
/*     */ import javafx.scene.control.ButtonType;
/*     */ import javafx.scene.control.CheckBox;
/*     */ import javafx.scene.control.ProgressBar;
/*     */ import javafx.scene.control.TextArea;
/*     */ import javafx.scene.control.TextField;
/*     */ import javafx.stage.DirectoryChooser;
/*     */ import javafx.stage.Stage;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class DecipherController
/*     */ {
/*     */   @FXML
/*     */   private Button originButton;
/*     */   @FXML
/*     */   private Button destinationButton;
/*     */   @FXML
/*     */   private Button exitButton;
/*     */   @FXML
/*     */   private Button decipherButton;
/*     */   @FXML
/*     */   private TextField originField;
/*     */   @FXML
/*     */   private TextField destinationField;
/*     */   @FXML
/*     */   private TextArea progressTextArea;
/*     */   @FXML
/*     */   private ProgressBar progressBar;
/*     */   @FXML
/*     */   private CheckBox replaceCheckbox;
/*     */   @FXML
/*     */   private CheckBox deleteOriginCheckbox;
/*  57 */   private DoubleProperty progress = new SimpleDoubleProperty();
/*     */   private boolean stopped = true;
/*  59 */   private final int NUM_THREADS = Runtime.getRuntime().availableProcessors() * 5;
/*     */   private List<Path> walk;
/*  61 */   private int decryptedAmount = 0;
/*     */   private int failedAmount;
/*     */   private int duplicateAmount;
/*  64 */   private final int MAX_TRIAL_VERSION_FILES = 50; static final boolean IS_TRIAL_VERSION = true;
/*     */   
/*     */   @FXML
/*     */   public void initialize() {
/*  68 */     this.progressBar.progressProperty().unbind();
/*  69 */     this.progressBar.progressProperty().bind(this.progress);
/*     */   }
/*     */   
/*     */   public void originButtonOnAction(ActionEvent event) {
/*  73 */     Stage stage = (Stage)this.originButton.getScene().getWindow();
/*  74 */     DirectoryChooser directoryChooser = new DirectoryChooser();
/*  75 */     File initialFile = new File(System.getProperty("user.home"));
/*  76 */     directoryChooser.setInitialDirectory(initialFile);
/*  77 */     directoryChooser.setTitle("Seleccion Directorio de Origen");
/*  78 */     File directoryChosed = directoryChooser.showDialog(stage);
/*  79 */     if (directoryChosed == null)
/*  80 */       return;  this.originField.setText(directoryChosed.getAbsolutePath());
/*     */   }
/*     */   
/*     */   public void destinationButtonOnAction(ActionEvent event) {
/*  84 */     Stage stage = (Stage)this.destinationButton.getScene().getWindow();
/*  85 */     DirectoryChooser directoryChooser = new DirectoryChooser();
/*  86 */     File initialFile = new File(System.getProperty("user.home"));
/*  87 */     directoryChooser.setInitialDirectory(initialFile);
/*  88 */     directoryChooser.setTitle("Seleccion Directorio de Destino");
/*  89 */     File directoryChosed = directoryChooser.showDialog(stage);
/*  90 */     if (directoryChosed == null)
/*  91 */       return;  this.destinationField.setText(directoryChosed.getAbsolutePath());
/*     */   }
/*     */ 
/*     */   
/*     */   public void exitButtonOnAction(ActionEvent event) {
/*  96 */     if (this.stopped) {
/*  97 */       System.exit(0);
/*     */     } else {
/*  99 */       Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType[] { ButtonType.OK, ButtonType.CANCEL });
/* 100 */       alert.setTitle("Parar");
/* 101 */       alert.setHeaderText("Parar desencriptado");
/* 102 */       alert.setContentText("Al confirmar la operación, no se revertirán los archivos ya desencriptados. Esta seguro que desea cancelar el desencriptado?");
/* 103 */       Optional<ButtonType> result = alert.showAndWait();
/* 104 */       if (result.get() == ButtonType.OK) {
/* 105 */         this.stopped = true;
/*     */         return;
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void decipherButtonOnAction(ActionEvent event) {
/* 113 */     handleDecipher();
/*     */   }
/*     */ 
/*     */   
/*     */   public void replaceCheckBoxOnAction(ActionEvent event) {
/* 118 */     boolean replaceState = this.replaceCheckbox.isSelected();
/* 119 */     this.destinationButton.setDisable(replaceState);
/* 120 */     this.destinationField.setDisable(replaceState);
/* 121 */     this.deleteOriginCheckbox.setDisable(replaceState);
/*     */   }
/*     */ 
/*     */   
/*     */   private void handleDecipher() {
/* 126 */     if (!Files.isDirectory(Paths.get(this.originField.getText(), new String[0]), new java.nio.file.LinkOption[0])) {
/* 127 */       (new Alert(Alert.AlertType.WARNING, 
/* 128 */           "No existe el directorio de origen", new ButtonType[] { ButtonType.OK })).showAndWait();
/*     */       return;
/*     */     } 
/* 131 */     if (!this.replaceCheckbox.isSelected() && !Files.isDirectory(Paths.get(this.destinationField.getText(), new String[0]), new java.nio.file.LinkOption[0])) {
/* 132 */       (new Alert(Alert.AlertType.WARNING, 
/* 133 */           "No existe el directorio de destino", new ButtonType[] { ButtonType.OK })).showAndWait();
/*     */       return;
/*     */     } 
/* 136 */     if (this.replaceCheckbox.isSelected()) {
/* 137 */       Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType[] { ButtonType.OK, ButtonType.CANCEL });
/* 138 */       alert.setTitle("Confirmacion");
/* 139 */       alert.setHeaderText("Reemplazo de Videos");
/* 140 */       alert.setContentText("Al confirmar la operación, los archivos de origen serán reemplazados por los desencriptados. Está seguro que desea desencriptar?");
/* 141 */       Optional<ButtonType> result = alert.showAndWait();
/* 142 */       if (result.get() == ButtonType.CANCEL) {
/*     */         return;
/*     */       }
/*     */     } 
/*     */     
/* 147 */     if (this.deleteOriginCheckbox.isSelected()) {
/* 148 */       Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType[] { ButtonType.OK, ButtonType.CANCEL });
/* 149 */       alert.setTitle("Confirmacion");
/* 150 */       alert.setHeaderText("Eliminado de Videos");
/* 151 */       alert.setContentText("Al confirmar la operación, los archivos de origen serán eliminados una vez desencriptados. Está seguro que desea desencriptar?");
/* 152 */       Optional<ButtonType> result = alert.showAndWait();
/* 153 */       if (result.get() == ButtonType.CANCEL) {
/*     */         return;
/*     */       }
/*     */     } 
/*     */     
/* 158 */     Task<Integer> decipherTask = new Task<Integer>()
/*     */       {
/*     */         protected Integer call() throws Exception {
/* 161 */           DecipherController.this.decipher(new File(DecipherController.this.originField.getText()), new File(DecipherController.this.destinationField.getText()));
/*     */           
/* 163 */           return Integer.valueOf(1);
/*     */         }
/*     */       };
/* 166 */     decipherTask.setOnFailed(e -> decipherTask.getException().printStackTrace());

    /*     */
/*     */     
/* 169 */     blockButtons();
/*     */     
/* 171 */     Thread mainDecipherThread = new Thread(decipherTask);
/*     */     
/* 173 */     mainDecipherThread.start();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void blockButtons() {
/* 181 */     this.decipherButton.setDisable(true);
/* 182 */     this.originButton.setDisable(true);
/* 183 */     this.destinationButton.setDisable(true);
/* 184 */     this.exitButton.setText("Parar");
/* 185 */     this.stopped = false;
/*     */   }
/*     */   
/*     */   private void releaseButtons() {
/* 189 */     this.decipherButton.setDisable(false);
/* 190 */     this.originButton.setDisable(false);
/* 191 */     this.destinationButton.setDisable(false);
/* 192 */     this.exitButton.setText("Salir");
/* 193 */     this.stopped = true;
/*     */   }
/*     */   
/*     */   private void decipher(File from, File to) throws InterruptedException {
/* 197 */     this.walk = null;
/*     */     
/* 199 */     ExecutorService exec = Executors.newFixedThreadPool(this.NUM_THREADS, runnable -> {
/*     */           Thread t = Executors.defaultThreadFactory().newThread(runnable);
/*     */           t.setDaemon(true);
/*     */           return t;
/*     */         });
/*     */     try {
/* 205 */       DirectoryWalker walker = new DirectoryWalker();
/* 206 */       Files.walkFileTree(from.toPath(), walker);
/* 207 */       Platform.runLater(() -> {
/*     */             this.progressTextArea.setText(" Calculando archivos...");
/*     */ 
/*     */             
/*     */             String sizeString = (walker.getTotalSize() < 1048576L) ? (String.valueOf(walker.getTotalSize() / 1024L) + "KBs") : ((walker.getTotalSize() < 1073741824L) ? (String.valueOf((float)(walker.getTotalSize() / 1048576L)) + " MBs") : (String.valueOf((float)walker.getTotalSize() / 1.07374182E9F) + " GBs"));
/*     */ 
/*     */             
/*     */             this.progressTextArea.setText(String.valueOf(this.progressTextArea.getText()) + " \nSe encontraron " + walker.getPathes().size() + " videos.");
/*     */             
/*     */             this.progressTextArea.setText(String.valueOf(this.progressTextArea.getText()) + " \nTamaño total de videos a desencriptar: " + sizeString);
/*     */           });
/*     */       
/* 219 */       this.walk = walker.getPathes();
/* 220 */     } catch (Exception e) {
/* 221 */       e.printStackTrace();
/*     */     } 
/* 223 */     Thread.sleep(1000L);
/* 224 */     Platform.runLater(() -> {
/*     */           final Encryptor encryptor;
/*     */           
/*     */           if (this.walk.size() > 50) {
/*     */             Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType[] { ButtonType.OK, ButtonType.CANCEL });
/*     */             
/*     */             alert.setTitle("ATENCION!");
/*     */             
/*     */             alert.setHeaderText("Versión de prueba");
/*     */             alert.setContentText("La versión de prueba solo admite hasta 50 archivos para desencriptar. Si desea continuar, solo se desencriptarán los primeros 50 archivos. \n¿Desea continuar?");
/*     */             Optional<ButtonType> result = alert.showAndWait();
/*     */             if (result.get() == ButtonType.CANCEL) {
/*     */               return;
/*     */             }
/*     */             this.walk = this.walk.subList(0, 49);
/*     */           } 
/*     */           try {
/*     */             encryptor = new Encryptor();
/* 242 */           } catch (GeneralSecurityException|java.io.IOException e2) {
/*     */             e2.printStackTrace();
/*     */             
/*     */             return;
/*     */           } 
/*     */           
/*     */           Task<Void> task2 = new Task<Void>()
/*     */             {
/*     */               protected Void call() throws Exception
/*     */               {
/* 252 */                 DecipherController.this.runForLoop(from, to, encryptor, exec);
/* 253 */                 return null;
/*     */               }
/*     */             };
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*     */           task2.run();
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*     */           exec.shutdown();
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*     */           try {
/*     */             while (!this.stopped && !exec.isTerminated()) {
/*     */               Thread.sleep(1L);
/*     */             }
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             if (this.stopped) {
/*     */               exec.shutdownNow();
/*     */             }
/*     */ 
/*     */ 
/*     */             
/*     */             //Platform.runLater(());
                      Platform.runLater(() -> {
                      // Código que deseas ejecutar en el hilo de JavaFX
                      });


            /* 285 */           } catch (InterruptedException e) {
/*     */             e.printStackTrace();
/*     */           } 
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void runForLoop(final File from, final File to, final Encryptor encryptor, ExecutorService exec) {
/* 294 */     final AtomicInteger videoOrdinal = new AtomicInteger(1);
/* 295 */     for (Path filePath : this.walk) {
/*     */       
/* 297 */       if (this.stopped) {
/* 298 */         this.stopped = false;
/*     */         
/*     */         break;
/*     */       } 
/* 302 */       Runnable runnable = new Runnable()
/*     */         {
/*     */           public void run()
/*     */           {
/* 306 */             String state = DecipherController.this.decipherTask(encryptor, filePath, to, from);
/* 307 */             Platform.runLater(() -> {
/*     */                   DecipherController.this.progress.set(videoOrdinal.get() / DecipherController.this.walk.size()); String str; switch ((str = state).hashCode()) {
/*     */                     case -1281977283:
/*     */                       if (str.equals("failed")) {
/*     */                         DecipherController.this.failedAmount = DecipherController.this.failedAmount + 1; DecipherController.this.progressTextArea.setText(String.valueOf(DecipherController.this.progressTextArea.getText()) + "\nEl video " + filePath.getFileName() + " no pudo ser desencriptado (" + videoOrdinal.get() + " de " + DecipherController.this.walk.size() + " videos)");
/*     */                         DecipherController.this.progressTextArea.appendText("");
/*     */                         break;
/*     */                       } 
/*     */                     case 3548:
/*     */                       if (str.equals("ok")) {
/*     */                         DecipherController.this.decryptedAmount = DecipherController.this.decryptedAmount + 1;
/*     */                         DecipherController.this.progressTextArea.setText(String.valueOf(DecipherController.this.progressTextArea.getText()) + "\nVideo " + filePath.getFileName() + " desencriptado satisfactoriamente (" + videoOrdinal.get() + " de " + DecipherController.this.walk.size() + " videos)");
/*     */                         DecipherController.this.progressTextArea.appendText("");
/*     */                         break;
/*     */                       } 
/*     */                     case 1201687819:
/*     */                       if (str.equals("duplicate")) {
/*     */                         DecipherController.this.duplicateAmount = DecipherController.this.duplicateAmount + 1;
/*     */                         DecipherController.this.progressTextArea.setText(String.valueOf(DecipherController.this.progressTextArea.getText()) + "\nEl video " + filePath.getFileName() + " ya se encontro en el destino (" + videoOrdinal.get() + " de " + DecipherController.this.walk.size() + " videos)");
/*     */                         DecipherController.this.progressTextArea.appendText("");
/*     */                         break;
/*     */                       } 
/*     */                     default:
/*     */                       throw new IllegalArgumentException("Unexpected value: " + state);
/*     */                   } 
/*     */                   videoOrdinal.incrementAndGet();
/*     */                 }); } };
/* 334 */       exec.execute(runnable);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String decipherTask(Encryptor encryptor, Path filePath, File to, File from) {
/*     */     try {
/* 342 */       if (this.replaceCheckbox.isSelected()) {
/* 343 */         encryptor.decrypt(filePath.toFile());
/*     */       } else {
/* 345 */         File toWithSubdirectories = null;
/* 346 */         if (from.toPath().getNameCount() < filePath.getNameCount() - 1) {
/* 347 */           toWithSubdirectories = new File(String.valueOf(to.toString()) + File.separatorChar + filePath.subpath(from.toPath().getNameCount(), filePath.getNameCount() - 1));
/*     */         } else {
/* 349 */           toWithSubdirectories = new File(to.toString());
/* 350 */         }  toWithSubdirectories.mkdirs();
/* 351 */         encryptor.decrypt(filePath.toFile(), new File(String.valueOf(toWithSubdirectories.getAbsolutePath()) + File.separatorChar + filePath.getFileName())); byte b; int i; File[] arrayOfFile;
/* 352 */         for (i = (arrayOfFile = filePath.getParent().toFile().listFiles()).length, b = 0; b < i; ) { File possibleTxtFile = arrayOfFile[b];
/* 353 */           if (possibleTxtFile.toString().endsWith(".txt"))
/* 354 */             if (this.deleteOriginCheckbox.isSelected()) {
/* 355 */               Files.move(possibleTxtFile.toPath(), toWithSubdirectories.toPath().resolve(possibleTxtFile.toPath().getFileName()), new java.nio.file.CopyOption[0]);
/*     */             } else {
/* 357 */               Files.copy(possibleTxtFile.toPath(), toWithSubdirectories.toPath().resolve(possibleTxtFile.toPath().getFileName()), new java.nio.file.CopyOption[0]);
/*     */             }   b++; }
/* 359 */          if (this.deleteOriginCheckbox.isSelected())
/* 360 */           Files.delete(filePath); 
/*     */       } 
/* 362 */       return "ok";
/*     */     
/*     */     }
/* 365 */     catch (FileAlreadyExistsException e) {
/* 366 */       return "duplicate";
/* 367 */     } catch (GeneralSecurityException|java.io.IOException e1) {
/* 368 */       return "failed";
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\mmula\Downloads\TL_Dec\TrafficLight_Decipher.jar!\application\DecipherController.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */