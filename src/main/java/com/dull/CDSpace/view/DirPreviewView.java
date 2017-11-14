package com.dull.CDSpace.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.dull.CDSpace.model.NodeItem;
import com.dull.CDSpace.utils.FileUtil;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/*
 * Author 杜亮亮
 * 2016.3.21
 */
public class DirPreviewView {
	private final Image dirIcon = new Image(getClass().getResourceAsStream("/image/iconfont-dir.png"));
	private final Image clientIcon = new Image(getClass().getResourceAsStream("/image/iconfont-client.png"));
	private final Image serverIcon = new Image(getClass().getResourceAsStream("/image/iconfont-server.png"));
	
	private TreeItem<NodeItem> treeItem;
	
	private TextField tfDirName;
	private TextField tfUrl;
	
	public DirPreviewView() {
		
	}
	
	public VBox initView() {
		VBox vbox = new VBox(10);
		vbox.setPrefHeight(600);
		vbox.setPrefWidth(600);
		
		GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 0, 10));
        grid.setVgap(10);
        grid.setHgap(10);
        vbox.getChildren().addAll(grid);
        Label tfDirLable = new Label("名称:");
        grid.add(tfDirLable, 0, 0);
        tfDirName = new TextField();//directory name
        tfDirName.setMinWidth(360);
        grid.add(tfDirName, 1, 0);
        Label tfUrlLable = new Label("Url:");
        grid.add(tfUrlLable, 0, 1);
        tfUrl = new TextField();//directory name
        tfUrl.setMinWidth(360);
        grid.add(tfUrl, 1, 1);
        
        Button saveButton = new Button("save");
        saveButton.setOnAction((ActionEvent e) -> {
        	saveButton();
        });
        grid.add(saveButton, 0,2);
        
		return vbox;
	}
	
	public void setContent(TreeItem<NodeItem> treeItem) {
		tfDirName.setText(treeItem.getValue().getFileName());
		String fileParentDirectoryPath = treeItem.getValue().getFile().getPath();
		File urlFile = new File(fileParentDirectoryPath + "/" + "url");
		if (urlFile.isFile()) {
			try {
				String url = FileUtils.readFileToString(urlFile, StandardCharsets.UTF_8);
		    	tfUrl.setText(url);
			} catch (IOException e) {
			}
		}
	}
	
	public void saveButton() {
		String oldName = treeItem.getValue().getFileName();
    	String newName = tfDirName.getText();
    	String newUrl = tfUrl.getText();
    	String fileParentDirectoryPath = treeItem.getValue().getFile().getPath();
    	boolean isNewNameExist = false;
    	if (!oldName.equals(newName)) {
    		for (File file : new File(fileParentDirectoryPath).getParentFile().listFiles()) {
    			if (file.isDirectory() && file.getName().equals(newName)) {
    				isNewNameExist = true;
    				Alert alert = new Alert(AlertType.ERROR);
    				alert.setTitle("Error Dialog");
    				alert.setHeaderText("An Error Dialog.");
    				alert.setContentText("Ooops, this name is repeated!");
    				alert.showAndWait();
    				break;
    			}
    		}
			if (!isNewNameExist) {
				File oldFile = new File(fileParentDirectoryPath);
    			File newFile = new File(oldFile.getParentFile(),newName);
    			treeItem.setValue(new NodeItem(newFile, newName));
    			oldFile.renameTo(newFile);
    			fileParentDirectoryPath = newFile.getAbsolutePath();
			}
			treeItem.getChildren().clear();//清空节点
			refreshTreeItem(treeItem);
    	}
    	if (newUrl!=null&&!newUrl.isEmpty()) {
			File urlFile = new File(new File(fileParentDirectoryPath), "url");
			try {
				FileUtils.write(urlFile, newUrl, StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void refreshTreeItem(TreeItem<NodeItem> item) {
		File[] files = item.getValue().getFile().listFiles();
		for (File file : files) {
			if(file.isDirectory()) {
				TreeItem<NodeItem> newTreeItem = new TreeItem<NodeItem>(new NodeItem(file, file.getName()), new ImageView(dirIcon));
				item.getChildren().add(newTreeItem);
				refreshTreeItem(newTreeItem);
			} else {
				if (FileUtil.getFileType(file.getName()) == 1) {
					TreeItem<NodeItem> newTreeItem = new TreeItem<NodeItem>(new NodeItem(file, file.getName()), new ImageView(clientIcon));
					item.getChildren().add(newTreeItem);
				} else if (FileUtil.getFileType(file.getName()) == 2) {
					TreeItem<NodeItem> newTreeItem = new TreeItem<NodeItem>(new NodeItem(file, file.getName()), new ImageView(serverIcon));
					item.getChildren().add(newTreeItem);
				}
			}
		}
	}

	public TreeItem<NodeItem> getTreeItem() {
		return treeItem;
	}

	public void setTreeItem(TreeItem<NodeItem> treeItem) {
		this.treeItem = treeItem;
	}

	public TextField getTfOfurl() {
		return tfDirName;
	}

	public void setTfOfurl(TextField tfDirName) {
		this.tfDirName = tfDirName;
	}
}
