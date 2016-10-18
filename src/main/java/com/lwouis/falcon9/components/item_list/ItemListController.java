package com.lwouis.falcon9.components.item_list;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.boris.pecoff4j.PE;
import org.boris.pecoff4j.ResourceDirectory;
import org.boris.pecoff4j.ResourceEntry;
import org.boris.pecoff4j.constant.ResourceType;
import org.boris.pecoff4j.io.PEParser;
import org.boris.pecoff4j.io.ResourceParser;
import org.boris.pecoff4j.resources.StringPair;
import org.boris.pecoff4j.resources.StringTable;
import org.boris.pecoff4j.resources.VersionInfo;
import org.boris.pecoff4j.util.ResourceHelper;

import com.lwouis.falcon9.AppState;
import com.lwouis.falcon9.components.launchable_cell.LaunchableCell;
import com.lwouis.falcon9.models.Launchable;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import sun.awt.shell.ShellFolder;

public class ItemListController implements Initializable {
  @FXML
  public Label launchableLabel;

  @FXML
  private ListView<Launchable> launchableListView;

  @FXML
  private TextField filterTextField;

  private static final String ENTER = "\r";

  private static final String DELETE = "";

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initializeFilterTextField();
    initializeLaunchableListView();
  }

  private void initializeFilterTextField() {
    filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
      String filterText = filterTextField.getText();
      FilteredList<Launchable> launchableFilteredList = AppState.getLaunchableFilteredList();
      if (filterText == null || filterText.length() == 0) {
        launchableFilteredList.setPredicate(s -> true);
      }
      else {
        String filterTextTrimmed = filterText.trim(); // ignore extra spaces on the sides
        launchableFilteredList.setPredicate(s -> StringUtils.containsIgnoreCase(s.getName(), filterTextTrimmed));
      }
    });
  }

  private void initializeLaunchableListView() {
    AppState.getLaunchableSortedList().setComparator((o1, o2) -> {
      Collator coll = Collator.getInstance();
      coll.setStrength(Collator.PRIMARY);
      return coll.compare(o1.getName(), o2.getName());
    });
    launchableListView.setItems(AppState.getLaunchableSortedList());
    launchableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    launchableListView.setCellFactory(lv -> new LaunchableCell());
  }

  @FXML
  public void removeSelected() {
    AppState.getLaunchableObservableList().removeAll(launchableListView.getSelectionModel().getSelectedItems());
  }

  @FXML
  public void onMouseEvent(MouseEvent mouseEvent) throws IOException {
    if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      launchSelectedInternal();
    }
  }

  @FXML
  public void onKeyEvent(KeyEvent keyEvent) throws IOException {
    if (keyEvent.getCharacter().equals(ENTER)) {
      launchSelectedInternal();
    }
    else {
      if (keyEvent.getCharacter().equals(DELETE)) {
        removeSelected();
      }
    }
  }

  private void launchSelectedInternal() {
    for (Launchable launchable : launchableListView.getSelectionModel().getSelectedItems()) {
      File file = new File(launchable.getAbsolutePath());
      try {
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().open(file);
        }
        else {
          new ProcessBuilder(launchable.getAbsolutePath()).start();
        }
      }
      catch (IOException e) {
        e.printStackTrace();
        // TODO: show the user that the file doesn't exist
      }
    }
  }

  @FXML
  public void onDragDropped(DragEvent dragEvent) {
    Dragboard db = dragEvent.getDragboard();
    boolean success = false;
    if (db.hasFiles()) {
      success = true;
      addFiles(db.getFiles());
    }
    dragEvent.setDropCompleted(success);
    dragEvent.consume();
  }

  public void addFiles(List<File> files) {
    List<Launchable> launchableList = new ArrayList<>();
    for (File file : files) {
      launchableList.add(new Launchable(getProductName(file), file.getAbsolutePath(), getFileIcon(file)));
    }
    AppState.getLaunchableObservableList().addAll(launchableList);
  }

  private String getProductName(File file) {
    try {
      PE pe = PEParser.parse(file);
      ResourceDirectory rd = pe.getImageData().getResourceTable();
      ResourceEntry[] entries = ResourceHelper.findResources(rd, ResourceType.VERSION_INFO);
      if (entries.length == 0) {
        return file.getName();
      }
      VersionInfo versionInfo = ResourceParser.readVersionInfo(entries[0].getData());
      StringTable properties = versionInfo.getStringFileInfo().getTable(0);
      for (int i = 0; i < properties.getCount(); i++) {
        StringPair property = properties.getString(i);
        if (property.getKey().equals("ProductName")) {
          return property.getValue();
        }
      }
      return file.getName();
    }
    catch (Throwable t) {
      t.printStackTrace();
      return file.getName();
    }
  }

  private Image getFileIcon(File file) {
    try {
      java.awt.Image icon = ShellFolder.getShellFolder(file).getIcon(true); // true is 32x32, false if 16x16
      BufferedImage bufferedImage = new BufferedImage(icon.getWidth(null), icon.getHeight(null),
              BufferedImage.TYPE_INT_ARGB);
      Graphics2D bGr = bufferedImage.createGraphics();
      bGr.drawImage(icon, 0, 0, null);
      bGr.dispose();
      return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  @FXML
  public void onDragOver(DragEvent dragEvent) {
    launchableListView.setOpacity(0.5);
    dragEvent.acceptTransferModes(TransferMode.ANY);
  }
  @FXML
  public void onDragExited(DragEvent dragEvent) {
    launchableListView.setOpacity(1);
  }
}