package RedditImgDL.controller;

import RedditImgDL.Tasks.DownloadAllTask;
import RedditImgDL.UI;
import RedditImgDL.Options.RequestOptions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class Controller {
    private RequestOptions options = new RequestOptions();

    @FXML
    private AnchorPane mainWindow;

    @FXML
    private TextField urlField;

    @FXML
    private TextField titleContainsRegexTextField;

    @FXML
    private TextField titleDoesNotContainRegexTextField;

    @FXML
    private CheckBox outputFullJsonCheckBox;

    @FXML
    private TextArea mainTextArea;

    @FXML
    private Button startButton;

    @FXML
    private Button setSaveDirectoryButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button clearButton;

    @FXML
    void handleStartButtonPressed(ActionEvent event) {
        if (this.options.isJobRunning()) {
            this.mainTextArea.appendText("Job is already running!\n");
            return;
        }

        this.options.setUrl(this.urlField.getText());
        this.options.setTitleContainsRegex(this.titleContainsRegexTextField.getText());
        this.options.setTitleDoesNotContainRegex(this.titleDoesNotContainRegexTextField.getText());

        this.options.setJobRunning(true);

        DownloadAllTask task = new DownloadAllTask(this.options);
        this.progressBar.progressProperty().bind(task.progressProperty());
        task.messageProperty().addListener((obv, old, ne) -> {
            this.mainTextArea.appendText(ne);
        });

        Thread th = new Thread(task);
        th.start();

    }

    @FXML
    void handleFullOutputButtonChecked(ActionEvent event) {
        this.options.setFullOutput(this.outputFullJsonCheckBox.isSelected());
    }

    @FXML
    void handleSetSavePressed(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(UI.primaryStage);
        this.options.setSaveDir(selectedDirectory);
        mainTextArea.appendText("Path set to: " + selectedDirectory.toString() + "\n");
    }

    @FXML
    void handleClearButtonPressed(ActionEvent event) {
        if (!options.isJobRunning()) {
            this.progressBar.progressProperty().unbind();
        }
        this.progressBar.setProgress(0);
        this.mainTextArea.clear();
        this.titleContainsRegexTextField.clear();
        this.titleDoesNotContainRegexTextField.clear();
        this.outputFullJsonCheckBox.disarm();
    }
}
