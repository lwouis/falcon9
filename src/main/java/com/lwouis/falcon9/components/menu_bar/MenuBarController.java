package com.lwouis.falcon9.components.menu_bar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

import com.lwouis.falcon9.AppState;
import com.lwouis.falcon9.DiskPersistanceManager;
import com.lwouis.falcon9.components.item_list.ItemListController;
import com.lwouis.falcon9.models.Launchable;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

public class MenuBarController {
  @FXML
  private MenuBar menuBar;

  private ItemListController itemListController;

  public void setItemListController(ItemListController itemListController) {
    this.itemListController = itemListController;
  }

  @FXML
  private void chooseFile() {
    FileChooser fileChooser = new FileChooser();
    List<File> files = fileChooser.showOpenMultipleDialog(menuBar.getScene().getWindow());
    if (files == null) {
      return;
    }
    for (File file : files) {
      AppState.getLaunchableObservableList()
              .add(new Launchable(file.getName(), file.getAbsolutePath(), getFileIcon(file)));
    }
  }

  private Image getFileIcon(File file) {
    FileSystemView view = FileSystemView.getFileSystemView();
    javax.swing.Icon icon = view.getSystemIcon(file);
    BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB);
    icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
    return SwingFXUtils.toFXImage(bufferedImage, null);
  }

  @FXML
  public void removeSelected() {
    itemListController.removeSelected();
  }

  @FXML
  public void fillWithDummy() {
    String pathToJsonFile = ClassLoader.getSystemResource("dummyItems.json").getPath();
    DiskPersistanceManager.loadFromDisk(new File(pathToJsonFile));
  }
}
