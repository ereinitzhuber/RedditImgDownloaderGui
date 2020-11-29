package RedditImgDL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;



public class UI extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        UI.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/RedditDL.fxml"));
        primaryStage.setTitle("RedditImgDL");
        primaryStage.getIcons().add(new Image(UI.class.getResourceAsStream("/images/icon.png")));
        primaryStage.setScene(new Scene(root, 535, 521));
        primaryStage.show();
    }

    public void stop() {
        System.exit(0);
    }
}
