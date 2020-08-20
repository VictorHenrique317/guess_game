package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.HttpNumber;
import utils.LedDisplay;

public class Main extends Application {
    private static Controller controller;
    private static Stage mainStage;
    private static Parent mainRoot;
    private static Scene mainScene;
    /**
     Nessa implementação estarei utilizando o JavaFX (uma API oficial criada pela Oracle para
     substituir a antiga biblioteca Swing), juntamente com uma ferramenta para agilizar o processo de
     criação da interface (Scene Builder). É importante dizer que o SceneBuilder não adiciona nenhuma
     funcionalidade nova, apenas acelera o desenvolvimento da UI com as ferramentas nativas do Java.

     Estou utilizando um tamanho de 667x375, esses valores são baseados na tela de um Iphone6
     segundo o Device Mode do Google Chrome.
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        FXMLLoader initialScreenLoader = new FXMLLoader();
        initialScreenLoader.setLocation(getClass().getClassLoader().getResource("InitialScreen.fxml"));
        Parent initialRoot = initialScreenLoader.load();

        // ==========================================================================================//
        FXMLLoader mainScreenLoader = new FXMLLoader();
        mainScreenLoader.setLocation(getClass().getClassLoader().getResource("MainScreen.fxml"));
        mainRoot = mainScreenLoader.load();
        mainScene = new Scene(initialRoot, 375, 667 );
        mainStage.setTitle("");
        mainStage.setScene(mainScene);
        controller = mainScreenLoader.getController();

        LedDisplay.initialize();
        HttpNumber.request();
        mainStage.show();
    }

    /**
     *  Troca a Root da Scene para a Root da tela principal
     */
    public static void initMainScreen(){
        mainScene.setRoot(mainRoot);
    }

    public static Controller getController() {
        return controller;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
