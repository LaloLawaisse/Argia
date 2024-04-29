package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import javafx.fxml.FXMLLoader;

import java.net.URL;



public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			
			URL url= getClass().getResource("Sample.fxml");
			if (url != null) {
				
				
				
			    String path = url.getPath();
			    System.out.println("Ruta del archivo FXML: " + path);
			    BorderPane root = FXMLLoader.load(url);
			    // Resto del código...
			    //BorderPane root = new BorderPane();
				
				//BorderPane root = FXMLLoader.<BorderPane>load(getClass().getResource("fxml/Sample.fxml"));
				
				
				Scene scene = new Scene(root, 600.0D, 557.0D);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				primaryStage.setTitle("Desencriptador Traffic-Light-Trial PRUEBA ARGIA ");
				primaryStage.setScene(scene);
				primaryStage.show();
				
			} else {
			    System.out.println("No se encontró el archivo FXML en la ruta especificada.");
			}
						
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
