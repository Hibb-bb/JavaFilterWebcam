package fxml.controller;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import com.github.sarxos.webcam.Webcam;

public class WebCamPreviewController implements Initializable {

	public int  filter_mode = 0;
	@FXML Button btnStartCamera;
	@FXML public Button get_pic;
	@FXML Button btnStopCamera;
	@FXML Button btnDisposeCamera;
	@FXML ComboBox<WebCamInfo> cbCameraOptions;
	@FXML BorderPane bpWebCamPaneHolder;
	@FXML FlowPane fpBottomPane;
	@FXML ImageView imgWebCamCapturedImage;

	@FXML ComboBox<String> filterOptions;
	private BufferedImage grabbedImage;
//	private WebcamPanel selWebCamPanel = null;
	private Webcam selWebCam = null;
	private boolean stopCamera = false;
	private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
	
	private String cameraListPromptText = "Choose Camera";
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		fpBottomPane.setDisable(true);
		ObservableList<String> foptions = FXCollections.observableArrayList(
				);
		ObservableList<WebCamInfo> options = FXCollections.observableArrayList(
				);
		int webCamCounter = 0;
		for(Webcam webcam:Webcam.getWebcams())
		{
			WebCamInfo webCamInfo = new WebCamInfo();
			webCamInfo.setWebCamIndex(webCamCounter);
			webCamInfo.setWebCamName(webcam.getName());
			options.add(webCamInfo);
			webCamCounter ++;
		}
		ArrayList<String> filterlist = new ArrayList<String>();
		filterlist.add("Normal");
		filterlist.add("Invert");
		filterlist.add("Cold");
		filterlist.add("Grey");
		filterlist.add("Warm");
		for(int i =0;i<filterlist.size();i++) {
			foptions.add(filterlist.get(i));
		}
		get_pic.setOnAction((event) -> {
			try{
				take_picture();
			}
			catch (Exception a) {
				System.out.print("failed");
			}
			
		});
		filterOptions.setItems(foptions);
		filterOptions.setPromptText("Select Filter");
		filterOptions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub
				filterOptions.setPromptText(arg2);
				switch(arg2) {
				case "Normal":
					filter_mode = 0;
					break;
				case "Invert":
					filter_mode = 1;
					break;
				case "Cold":
					filter_mode = 2;
					break;
				case "Grey":
					filter_mode = 3;
					break;
				case "Warm":
					filter_mode=4;
					break;
				default:
					filter_mode = 0;
				}
				
			}
			
		});
		cbCameraOptions.setItems(options);
		cbCameraOptions.setPromptText(cameraListPromptText);
		cbCameraOptions.getSelectionModel().selectedItemProperty().addListener(new  ChangeListener<WebCamInfo>() {

	        @Override
	        public void changed(ObservableValue<? extends WebCamInfo> arg0, WebCamInfo arg1, WebCamInfo arg2) {
	            if (arg2 != null) {
	               
	            	System.out.println("WebCam Index: " + arg2.getWebCamIndex()+": WebCam Name:"+ arg2.getWebCamName());
	            	initializeWebCam(arg2.getWebCamIndex());
	            }
	        }
	    });
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				
				setImageViewSize();
				
			}
		});
		
	}
	protected void setImageViewSize() {
		
		double height = bpWebCamPaneHolder.getHeight();
		double width  = bpWebCamPaneHolder.getWidth();
		imgWebCamCapturedImage.setFitHeight(height);
		imgWebCamCapturedImage.setFitWidth(width);
		imgWebCamCapturedImage.prefHeight(height);
		imgWebCamCapturedImage.prefWidth(width);
		imgWebCamCapturedImage.setPreserveRatio(true);
		
	}
	protected void initializeWebCam(final int webCamIndex) {
		
		Task<Void> webCamIntilizer = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				if(selWebCam == null)
				{
					selWebCam = Webcam.getWebcams().get(webCamIndex);
					selWebCam.open();
				}else
				{
					closeCamera();
					selWebCam = Webcam.getWebcams().get(webCamIndex);
					selWebCam.open();
					
				}
				startWebCamStream();
				return null;
			}
			
		};
		
		new Thread(webCamIntilizer).start();
		fpBottomPane.setDisable(false);
		btnStartCamera.setDisable(true);
	}
	
	protected void startWebCamStream() {
		
		stopCamera  = false;
		Task<Void> task = new Task<Void>() {

		
			@Override
			protected Void call() throws Exception {

				while (!stopCamera) {
					try {
						if ((grabbedImage = selWebCam.getImage()) != null) {
							switch(filter_mode) {
							case 0:
								break;
							case 1:
								grabbedImage = negative(grabbedImage);
								break;
							case 2:
								grabbedImage = soft(grabbedImage);
								break;
							case 3:
								grabbedImage = grey(grabbedImage);
								break;
							case 4:
								grabbedImage = warm(grabbedImage);
								break;
							default:
								break;
							}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									final Image mainiamge = SwingFXUtils
											.toFXImage(grabbedImage, null);
									imageProperty.set(mainiamge);
								}
							});

							grabbedImage.flush();

						}
					} catch (Exception e) {
					} finally {

					}

				}

				return null;

			}

		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		imgWebCamCapturedImage.imageProperty().bind(imageProperty);
		
	}

	
	
	private void closeCamera()
	{
		if(selWebCam != null)
		{
			selWebCam.close();
		}
	}
	
	public void stopCamera(ActionEvent event)
	{
		stopCamera = true;
		btnStartCamera.setDisable(false);
		btnStopCamera.setDisable(true);
	}
	
	public void startCamera(ActionEvent event)
	{
		stopCamera = false;
		startWebCamStream();
		btnStartCamera.setDisable(true);
		btnStopCamera.setDisable(false);
	}
	
	public void disposeCamera(ActionEvent event)
	{
		stopCamera = true;
		closeCamera();
		Webcam.shutdown();
		btnStopCamera.setDisable(true);
		btnStartCamera.setDisable(true);
	}
	
	public BufferedImage negative(BufferedImage img) { //filter_mode = 1
		BufferedImage res = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		for(int i =0;i<img.getWidth();i++) {
			for(int j =0;j<img.getHeight();j++) {
				Color color = new Color(img.getRGB(i, j));
				Color tmpcol = new Color( 255-color.getRed(),255-color.getGreen(),255 - color.getBlue());
				res.setRGB(i, j, tmpcol.getRGB());
			}
		}
		return res;
	}
	
	
	public BufferedImage soft(BufferedImage img) { //filter_mode = 2
		int r,g;
		BufferedImage res = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		for(int i =0;i<img.getWidth();i++) {
			for(int j =0;j<img.getHeight();j++) {
				Color color = new Color(img.getRGB(i, j));
				if(color.getRed() < 20 || color.getGreen() < 20) {
					g = color.getGreen()+20;
					r = color.getRed()+20;
				}
				else {
					g = color.getGreen();
					r = color.getRed();
				}
				
				Color tmpcol = new Color( r-20,g-20,color.getBlue());
				res.setRGB(i, j, tmpcol.getRGB());
			}
		}
		return res;
	}
	
	public BufferedImage grey(BufferedImage img) { // filter_mode = 3
		BufferedImage res = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		for(int i =0;i<img.getWidth();i++) {
			for(int j =0;j<img.getHeight();j++) {
				Color color = new Color(img.getRGB(i, j));
				int t = (color.getRed() + color.getBlue() + color.getGreen())/3;
				Color tmpcol = new Color( t,t,t);
				res.setRGB(i, j, tmpcol.getRGB());
			}
		}
		return res;
	}
	
	
	public BufferedImage warm(BufferedImage img) { // filter_mode = 4
		int g,b;
		BufferedImage res = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		for(int i =0;i<img.getWidth();i++) {
			for(int j =0;j<img.getHeight();j++) {
				Color color = new Color(img.getRGB(i, j));
				if(color.getGreen() < 20 || color.getBlue() < 20) {
					g = color.getGreen()+20;
					b = color.getBlue()+20;
				}
				else {
					g = color.getGreen();
					b = color.getBlue();
				}
				
				Color tmpcol = new Color(color.getRed(),g-20,b-20);
				res.setRGB(i, j, tmpcol.getRGB());
			}
		}
		return res;
	}
	
	@FXML
	public void take_picture() throws IOException {
		//take picture
		BufferedImage img = selWebCam.getImage();
		switch(filter_mode) {
		case 0:
			break;
		case 1:
			img = negative(img);
			break;
			
		case 2:
			img = soft(img);
			break;
			
		case 3:
			img = grey(img);
			break;
			
		case 4:
			img = warm(img);
			break;
		default:
			break;
			
		}
		ImageIO.write(img,"PNG",new File("img.png"));
		
	}
	
	class Filter{
		private int mode;
		private String name;
		public Filter(int m,String s) {
			this.mode = m;
			this.name = s;
		}
		
		public String getname() {
			return this.name;
		}
		
		public int getmode() {
			return this.mode;
		}
	}

	
	class WebCamInfo
	{
		private String webCamName ;
		private int webCamIndex ;
		
		public String getWebCamName() {
			return webCamName;
		}
		public void setWebCamName(String webCamName) {
			this.webCamName = webCamName;
		}
		public int getWebCamIndex() {
			return webCamIndex;
		}
		public void setWebCamIndex(int webCamIndex) {
			this.webCamIndex = webCamIndex;
		}
		
		@Override
		public String toString() {
		        return webCamName;
	     }
		
	}
}
