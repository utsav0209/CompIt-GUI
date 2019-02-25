package compit;

import huffman.*;
import shannon.*;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class Controller {

    public void huf_zip(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Huffzip.zip(file.getAbsolutePath());
        }
    }

    public void huf_unzip(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Huffunzip.unzip(file.getAbsolutePath());
        }
    }

    public void sf_zip(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Sfzip.zip(file.getAbsolutePath());
        }
    }

    public void sf_unzip(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Sfunzip.unzip(file.getAbsolutePath());
        }
    }

}
