package com.dull.CDSpace.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.dull.CDSpace.model.Header;
import com.dull.CDSpace.model.HttpClientRequest;
import com.dull.CDSpace.model.NodeItem;
import com.dull.CDSpace.utils.httpClient.HttpClientResponse;
import com.dull.CDSpace.utils.httpClient.HttpClientUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/*
 * Author 杜亮亮
 * 2016.3.21
 */
public class HttpClientEditorView {
	private ObservableList<Header> data = FXCollections.observableArrayList();
	
	private TextArea taOfURL;
	
	private ComboBox<String> methodComboBox;
	
	private TextArea taOfRequestBody;
	
	private TableView<Header> table;
	
	private TextArea taOfResponseBody;
	
	private TextArea taOfResponseHeader;
	
	private TreeItem<NodeItem> treeItem;

	private Thread httpThread;
	private Runnable httpRunnable;
	
	private Button sendButton;
	public HttpClientEditorView() {
		httpRunnable = new Runnable() {
			public void run() {
				try {
					taOfResponseBody.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\t start it.\n");
					HttpClientResponse response = new HttpClientResponse();
					String methodOfHTTP = methodComboBox.getSelectionModel().getSelectedItem().toString();
					String urlText = taOfURL.getText().replaceAll("\\s","");
					String bodyText = taOfRequestBody.getText().replaceAll("\\s","");
					if (!urlText.startsWith("http")) {
						urlText = getParentUrl() + urlText;
					}
					taOfResponseBody.appendText(methodOfHTTP+" URL:"+urlText +"\n");
					if (methodOfHTTP.equals("GET")){
			    		if (bodyText!=null) {
							if (urlText.contains("?")) {
								urlText += "&" + bodyText;
							}else{
								urlText += "?" + bodyText;
							}
						}
						response = HttpClientUtil.doGet(urlText, exchangeHeaders(data));
					} else if (methodOfHTTP.equals("POST")) {
						response = HttpClientUtil.doPost(urlText, exchangeHeaders(data), bodyText);
					} else if (methodOfHTTP.equals("DELETE")) {
						response = HttpClientUtil.doDelete(urlText, exchangeHeaders(data));
					} else if (methodOfHTTP.equals("PUT")) {
						response = HttpClientUtil.doPut(urlText, exchangeHeaders(data), bodyText);
					}
					if (response.getStateCode() != null) {
						taOfResponseBody.appendText(response.getStateCode() + "\n" + response.getResponse()+"\n");
						taOfResponseHeader.setText(response.getHeaders());
					} else {
						taOfResponseBody.appendText("Send request fail. Please check the content of request or the state of http server!");
						taOfResponseHeader.setText("");
					}
					if (sendButton!=null) {
						sendButton.setDisable(false);
					}
				} catch (Exception e) {
					taOfResponseBody.appendText("Tread stop it;");
				}finally {
					httpThread = null;
					httpThread = new Thread(httpRunnable);
				}
			}
		};
		httpThread = new Thread(httpRunnable);
	}
	
	private String getParentUrl(){
		StringBuffer sb = new StringBuffer();
		String parentPath = treeItem.getValue().getFile().getPath();
		File parentFile = new File(parentPath);
		while(!parentFile.getName().equals("project")){
			File urlFile = new File(parentFile, "url");
			if (urlFile.isFile()) {
				try {
					String url = FileUtils.readFileToString(urlFile, StandardCharsets.UTF_8);
					if (url!=null&&!url.isEmpty()) {
						sb.insert(0, url);
					}
				} catch (IOException e) {
				}
			}
			parentFile = parentFile.getParentFile();
		}
		return sb.toString();
	}
	
	public VBox initCenterBorder() {
		VBox vbox = new VBox(10);
		vbox.setPrefHeight(800);
		vbox.setPrefWidth(1000);
        
        TabPane tpOfRequest = new TabPane();
        Tab tabOfURL = new Tab();
        tabOfURL.setClosable(false);
        tabOfURL.setText("URL");
        tabOfURL.setContent(initURLPane());
        Tab tabOfRequestHeader = new Tab();
        tabOfRequestHeader.setClosable(false);
        tabOfRequestHeader.setText("Request Header");
        tabOfRequestHeader.setContent(initHeaderPane());
        Tab tabOfRequestBody = new Tab();
        tabOfRequestBody.setClosable(false);
        tabOfRequestBody.setText("Request Body");
        tabOfRequestBody.setContent(initBodyPane());
        tpOfRequest.getTabs().addAll(tabOfURL, tabOfRequestBody, tabOfRequestHeader);
        
        TabPane tpOfResponse = new TabPane();
        Tab tabOfResponseBody = new Tab();
        tabOfResponseBody.setClosable(false);
        tabOfResponseBody.setText("Response Body");
        tabOfResponseBody.setContent(initResponseBodyPane());
        Tab tabOfResponseHeader = new Tab();
        tabOfResponseHeader.setClosable(false);
        tabOfResponseHeader.setText("Response Header");
        tabOfResponseHeader.setContent(initResponseHeaderPane());
        tpOfResponse.getTabs().addAll(tabOfResponseBody, tabOfResponseHeader);
        
        vbox.getChildren().addAll(initRequestGrid(), tpOfRequest, tpOfResponse);
        
		return vbox;
	}
	
	private Pane initRequestGrid() {//Http Request Pane
		GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 0, 10));
        grid.setVgap(10);
        grid.setHgap(10);
        //Http Method ComboBox
		methodComboBox = new ComboBox<String>();
		methodComboBox.getItems().addAll(
            "GET",
            "POST",
            "PUT",
            "DELETE"
        );
		methodComboBox.setValue("GET");
		methodComboBox.setMinWidth(100);
        grid.add(methodComboBox, 0, 0);
        
        //send button
        sendButton = new Button("Send");
        sendButton.setOnAction((ActionEvent e) -> {
        	sendButton();
        });
        sendButton.setMinWidth(100);
        grid.add(sendButton, 1, 0);
        //save button
        Button saveButton = new Button("Save");
        saveButton.setOnAction((ActionEvent e) -> {
        	saveButton();
        });
        saveButton.setMinWidth(100);
        grid.add(saveButton, 2, 0);
        //stop button
        Button stopButton = new Button("Stop");
        stopButton.setOnAction((ActionEvent e) -> {
        	stopButton();
        });
        stopButton.setMinWidth(100);
        grid.add(stopButton, 3, 0);
        //clear Cookie
        Button clearButton = new Button("ClearCookie");
        clearButton.setOnAction((ActionEvent e) -> {
        	clearButton();
        });
        clearButton.setMinWidth(100);
        grid.add(clearButton, 4, 0);
        return grid;
	}
	
	@SuppressWarnings("unchecked")
	private Pane initHeaderPane() {
		VBox vbox = new VBox(10);
		
		HBox headersTableBox = new HBox();
        headersTableBox.setPadding(new Insets(10, 10, 0, 10));
        table = new TableView<Header>();
        table.setEditable(true);
        table.setPrefWidth(580);
        table.setPrefHeight(200);
        headersTableBox.getChildren().add(table);
        
        TableColumn<Header, String> headerNameCol = new TableColumn<Header, String>("Header Name");
        headerNameCol.setPrefWidth(290);
        headerNameCol.setCellValueFactory(new PropertyValueFactory<>("name")); 
        headerNameCol.setCellFactory(TextFieldTableCell.<Header>forTableColumn());
        headerNameCol.setOnEditCommit(
            (CellEditEvent<Header, String> t) -> {
                ((Header) t.getTableView().getItems().get(
                         t.getTablePosition().getRow())
                         ).setName(t.getNewValue());
        });
        
        TableColumn<Header, String> headerValueCol = new TableColumn<Header, String>("Header Value");
        headerValueCol.setPrefWidth(290);
        headerValueCol.setCellValueFactory(new PropertyValueFactory<>("value")); 
        headerValueCol.setCellFactory(TextFieldTableCell.<Header>forTableColumn());
        headerValueCol.setOnEditCommit(
            (CellEditEvent<Header, String> t) -> {
                ((Header) t.getTableView().getItems().get(
                         t.getTablePosition().getRow())
                         ).setValue(t.getNewValue());
        });
		
        table.setItems(data);
        table.getColumns().addAll(headerNameCol, headerValueCol);
        
        //Http Add Header Pane
        GridPane addHeadergrid = new GridPane();
        addHeadergrid.setPadding(new Insets(0, 10, 0, 10));
        addHeadergrid.setHgap(10);
        final TextField addHeaderName = new TextField();
        addHeaderName.setPromptText("Header Name");
        addHeaderName.setPrefWidth(175);
        addHeadergrid.add(addHeaderName, 0, 0);
        final TextField addHeaderValue = new TextField();
        addHeaderValue.setPromptText("Header Value");
        addHeaderValue.setPrefWidth(175);
        addHeadergrid.add(addHeaderValue, 1, 0);
        final Button addButton = new Button("add");
        addButton.setMinWidth(100);
        addButton.setOnAction((ActionEvent e) -> {
            data.add(new Header(
            		addHeaderName.getText(),
                    addHeaderValue.getText()));
            addHeaderName.clear();
            addHeaderValue.clear();
        });
        addHeadergrid.add(addButton, 2, 0);
        final Button deleteButton = new Button("delete");
        deleteButton.setMinWidth(100);
        deleteButton.setOnAction((ActionEvent e) -> {
            data.remove(table.getFocusModel().getFocusedIndex());
        });
        addHeadergrid.add(deleteButton, 3, 0);
        
        vbox.getChildren().addAll(headersTableBox, addHeadergrid);
        return vbox;
	}
	
	private Pane initBodyPane() {
        HBox hb = new HBox();
        hb.setPadding(new Insets(10, 10, 0, 10));
        taOfRequestBody = new TextArea();
        taOfRequestBody.setWrapText(true);//自动换行
        taOfRequestBody.setPromptText("Enter Http Request Body");
        taOfRequestBody.setPrefWidth(580);
        hb.getChildren().add(taOfRequestBody);
        
        return hb;
	}
	
	private Pane initURLPane() {
        HBox hb = new HBox();
        hb.setPadding(new Insets(10, 10, 0, 10));
        taOfURL = new TextArea();
        taOfURL.setWrapText(true);//自动换行
        taOfURL.setPromptText("Enter Http Request URL");
        taOfURL.setPrefWidth(580);
        hb.getChildren().add(taOfURL);
        
        return hb;
	}
	
	private Pane initResponseBodyPane() {
        HBox consoleHb = new HBox();
        consoleHb.setPadding(new Insets(10, 10, 10, 10));
        taOfResponseBody = new TextArea();
        taOfResponseBody.setEditable(false);
        taOfResponseBody.setWrapText(true);
        taOfResponseBody.setPrefWidth(580);
        taOfResponseBody.setPrefHeight(280);
        consoleHb.getChildren().add(taOfResponseBody);
        
        return consoleHb;
	}
	
	private Pane initResponseHeaderPane() {
        HBox consoleHb = new HBox();
        consoleHb.setPadding(new Insets(10, 10, 10, 10));
        taOfResponseHeader = new TextArea();
        taOfResponseHeader.setEditable(false);
        taOfResponseHeader.setWrapText(true);
        taOfResponseHeader.setPrefWidth(580);
        taOfResponseHeader.setPrefHeight(280);
        consoleHb.getChildren().add(taOfResponseHeader);
        
        return consoleHb;
	}
	
	private HashMap<String, String> exchangeHeaders(ObservableList<Header> data){
		HashMap<String, String> headers = new HashMap<String, String>();
		for (int i = 0; i < data.size(); i++) {
			Header header = data.get(i);
			headers.put(header.getName(), header.getValue());
		}
		return headers;
	}
	
	private ObservableList<Header> exchangeMapToList(HashMap<String, String> headers){
		ObservableList<Header> data = FXCollections.observableArrayList();
		if (null != headers && !headers.isEmpty()) {
        	Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
				Header header = new Header(entry.getKey(), entry.getValue());
				data.add(header);
			}
        }
		return data;
	}
	
	public void setContent(HttpClientRequest request){
		methodComboBox.setValue(request.getMethod());
		taOfURL.setText(request.getUrl());
		taOfRequestBody.setText(request.getBody());
		ObservableList<Header> dataInfile = exchangeMapToList(request.getHeaders());
		data.clear();
		for (Header header : dataInfile) {
			data.add(header);
		}
		table.setItems(data);
		taOfResponseBody.setText("");
		taOfResponseHeader.setText("");
	}
	
	public void setEmptyContent(){
		methodComboBox.setValue("GET");
		taOfURL.setText("");
		taOfRequestBody.setText("");
		data.clear();
		taOfResponseBody.setText("");
		taOfResponseHeader.setText("");
	}
	
	public void saveButton() {
		HttpClientRequest request = new HttpClientRequest();
    	request.setMethod(methodComboBox.getSelectionModel().getSelectedItem().toString());
    	request.setUrl(taOfURL.getText());
    	request.setBody(taOfRequestBody.getText());
    	request.setHeaders(exchangeHeaders(data));
    	HttpClientRequest.requestToFile(request, this.treeItem.getValue().getFile().getPath());
	}
	
	public void sendButton() {
		if (sendButton!=null) {
			sendButton.setDisable(true);
		}
		httpThread.start();
	}
	public void stopButton(){
		if (sendButton!=null) {	
			sendButton.setDisable(false);
		}
		httpThread.interrupt();
		httpThread = null;
		httpThread = new Thread(httpRunnable);
	}
	
	public void clearButton(){
		HttpClientUtil.cookies.clear();
	}
	
	public ObservableList<Header> getData() {
		return data;
	}

	public void setData(ObservableList<Header> data) {
		this.data = data;
	}

	public TextArea getTaOfRequestBody() {
		return taOfRequestBody;
	}

	public void setTaOfRequestBody(TextArea taOfRequestBody) {
		this.taOfRequestBody = taOfRequestBody;
	}

	public TextArea getTaOfResponseBody() {
		return taOfResponseBody;
	}

	public void setTaOfResponseBody(TextArea taOfResponseBody) {
		this.taOfResponseBody = taOfResponseBody;
	}

	public TextArea getTaOfResponseHeader() {
		return taOfResponseHeader;
	}

	public void setTaOfResponseHeader(TextArea taOfResponseHeader) {
		this.taOfResponseHeader = taOfResponseHeader;
	}

	public TreeItem<NodeItem> getTreeItem() {
		return treeItem;
	}

	public void setTreeItem(TreeItem<NodeItem> treeItem) {
		this.treeItem = treeItem;
	}

	public TableView<Header> getTable() {
		return table;
	}

	public void setTable(TableView<Header> table) {
		this.table = table;
	}
}
