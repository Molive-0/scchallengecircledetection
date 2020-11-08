package ml.molive.scchallengecircledetection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("ScChallengeCircleDetection");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        // set the proper behavior on closing the application
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest((we -> {
            try {
                controller.setClosed();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        controller.startCamera();
    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
