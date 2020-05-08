package org.codespeak.distribution.client.scenes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.codespeak.distribution.client.Configuration;
import org.codespeak.distribution.client.data.Dependency;
import org.codespeak.distribution.client.data.Program;
import org.codespeak.distribution.client.handler.BackendHandler;
import org.codespeak.distribution.client.util.AlertUtil;

/**
 * Controller for the program dependencies scene
 *
 * @author Vector
 */
public class ProgramDependenciesSceneController implements Initializable {

    private Map<String, Dependency> dependencyNameMap = new HashMap<String, Dependency>();
    private Program program;
    private Dependency currentlySelectedDependency;
    private Runtime runtime;
    
    @FXML private Label programNameLabel;
    @FXML private Label dependencyNameLabel;
    @FXML private ListView<String> dependencyList;
    @FXML private Label dependencyDescriptionLabel;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        program = null;
        currentlySelectedDependency = null;
        runtime = Runtime.getRuntime();
    }    
    
    /**
     * Called when displaying dependency information from a program
     * @param program program to show dependency information from
     */
    public void showProgramDependencies(Program program) {
        this.program = program;
        
        programNameLabel.setText(program.getName());
        
        List<Dependency> dependencies = program.getDependencies();
        ObservableList<String> dependencyItems = dependencyList.getItems();
        
        for (Dependency dependency : dependencies) {
            String name = dependency.getName();
            
            dependencyItems.add(name);
            dependencyNameMap.put(name, dependency);
        }
    }
    
    @FXML
    public void onDependencySelect() {
        int selectedIndex = dependencyList.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex > -1) {
            ObservableList<String> dependencyItems = dependencyList.getItems();            
            String dependencyName = dependencyItems.get(selectedIndex);
            Dependency dependency = dependencyNameMap.get(dependencyName);
            
            dependencyNameLabel.setText(dependency.getName());
            dependencyDescriptionLabel.setText(dependency.getDescription());
            
            currentlySelectedDependency = dependency;
        }
    }
    
    @FXML
    public void onInstallDependencyButtonClick(ActionEvent event) throws IOException {
        if (currentlySelectedDependency != null) {
            String URL = currentlySelectedDependency.getURL();
            String ext = URL.substring(URL.lastIndexOf("."));
            
            ReadableByteChannel readableByteChannel = BackendHandler.getRemoteFileChannelFromURL(URL);
            File tempFile = File.createTempFile("csds_client", ext);
            FileChannel outChannel = new FileOutputStream(tempFile).getChannel();
            
            outChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            
            readableByteChannel.close();
            outChannel.close();
            
            runtime.exec("cmd /c " + tempFile.toString());
        } else {
            Alert alert = AlertUtil.createAlert("Select a dependency first.");
            alert.show();
        }
    }

    @FXML
    public void onTestProgramButtonClick(ActionEvent event) throws IOException {
        Path programDirectoryAndLaunchFile = program.getDirectory(true);
        
        runtime.exec(programDirectoryAndLaunchFile.toString());
    }
    
    @FXML
    public void onCloseButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
}
