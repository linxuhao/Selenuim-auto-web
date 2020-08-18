package spider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import controller.FormuleController;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import model.Car;
import model.CarContentPage;

/**
 * handles spiders
 * @author Linxuhao
 *
 */
public class SpiderHandler implements Runnable{
	
	/**
	 * being a politness spider, do not ddos attack lacentral.fr
	 */
	public static long gentlemanWaitingTime = 1000; 
	public static int maxIndexSpiderNumber = 1;
	public static int maxContentSpiderNumber = 5;
	private ConcurrentHashMap<String, Car> carInformations;
	private CopyOnWriteArrayList<ContentSpider> contentSpiderHandler;
	private CopyOnWriteArrayList<IndexSpider> indexSpiderHandler;
	private Stack<CarContentPage> pagesToScan;
	private Set<CarContentPage> workingPages;
	/**
	 * name of fields you need in a car's information
	 */
	private List<String> fields;
	private int maxNumber;
	private String startUrl;
	private boolean isRunning;
	private long startTime;
	/**
	 * User interactive part
	 */
	private FloatProperty currentProgression;
	private String msg;
	private FormuleController controller;
	
	public SpiderHandler(String startUrl, List<String> fields, FormuleController controller, int maxNumber) {
		super();
		this.setCarInformations(new ConcurrentHashMap<String, Car>());
		this.contentSpiderHandler = new CopyOnWriteArrayList<ContentSpider>();
		this.indexSpiderHandler = new CopyOnWriteArrayList<IndexSpider>();
		this.setPagesToScan(new Stack<CarContentPage>());
		this.setRunning(false);
		this.startUrl = startUrl;
		this.fields = fields;
		this.currentProgression = new SimpleFloatProperty();
		this.setController(controller);
		this.msg = null;
		this.workingPages = new HashSet<CarContentPage>();
		this.maxNumber = maxNumber;
		this.startTime = System.currentTimeMillis();
	}

	public ConcurrentHashMap<String, Car> getCarInformations() {
		return carInformations;
	}

	public void setCarInformations(ConcurrentHashMap<String, Car> carInformations) {
		this.carInformations = carInformations;
	}

	public CopyOnWriteArrayList<ContentSpider> getContentSpiderHandler() {
		return contentSpiderHandler;
	}

	public void setContentSpiderHandler(CopyOnWriteArrayList<ContentSpider> contentSpiderHandler) {
		this.contentSpiderHandler = contentSpiderHandler;
	}

	public CopyOnWriteArrayList<IndexSpider> getIndexSpiderHandler() {
		return indexSpiderHandler;
	}

	public void setIndexSpiderHandler(CopyOnWriteArrayList<IndexSpider> indexSpiderHandler) {
		this.indexSpiderHandler = indexSpiderHandler;
	}
	
	public List<String> getFields() {
		return fields;
	}
	
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public Stack<CarContentPage> getPagesToScan() {
		return pagesToScan;
	}

	public void setPagesToScan(Stack<CarContentPage> pagesToScan) {
		this.pagesToScan = pagesToScan;
	}

	public float getCurrentProgression() {
		return currentProgression.floatValue();
	}

	public void setCurrentProgression(float currentProgression) {
		this.currentProgression.set(currentProgression);
	}

	public FloatProperty CurrentProgressionProperty() {
		return currentProgression;
	}

	public FormuleController getController() {
		return controller;
	}

	public void setController(FormuleController controller) {
		this.controller = controller;
	}
	

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Set<CarContentPage> getWorkingPages() {
		return workingPages;
	}

	public void setWorkingPages(Set<CarContentPage> workingPages) {
		this.workingPages = workingPages;
	}

	@Override
	public void run() {
		this.setRunning(true);
		while(isRunning){
			
			//init the index scan spiders
			if(indexSpiderHandler.isEmpty()){
				for(int i = 0; i < SpiderHandler.maxIndexSpiderNumber; i++){
					IndexSpider indexSpider = new IndexSpider(this, startUrl);
					indexSpiderHandler.add(indexSpider);
					Thread indexThread = new Thread(indexSpider, "IndexSpider #" + i);
					indexThread.start();
				}
			}
			//if there are empty spider spot, and page waiting to be scanned, scan it with a new spider !
			if(contentSpiderHandler.size() < SpiderHandler.maxContentSpiderNumber && !pagesToScan.isEmpty()){
				
				workingPages.add(pagesToScan.peek());
				CarContentPage page = pagesToScan.pop();
				
				ContentSpider cSpider = new ContentSpider(this, page);
				contentSpiderHandler.add(cSpider);
				Thread contentThread = new Thread(cSpider, "ContentSpider #" + page.getUrl());
				contentThread.start();
			}
			//if no spider is working and no pages left to scan, means nothing left to do
			if(indexSpiderHandler.isEmpty() && contentSpiderHandler.isEmpty() && pagesToScan.isEmpty() ){
				this.setMsg(getTheEndMessage("work succed"));
				this.stop();
			}
			if(maxNumber > 0){
				if(carInformations.size() >= maxNumber){
					this.setMsg(getTheEndMessage("work succed by reaching max number"));
					this.stop();
				}
			}
			updateProgression();
			//try to be polite, so 10 hits max each seconds
			try {
				Thread.sleep(gentlemanWaitingTime);
			} catch (InterruptedException e) {
				this.stop();
				e.printStackTrace();
			}
		}
		//if is not finished normaly, there is no msg
		if(this.getMsg() == null){
			this.setMsg(getTheEndMessage("work stoped by error"));
		}
		System.out.println(this.getMsg());
		//double check
		this.stop();
		//ask the javafx thread to do the update stuff
		Platform.runLater(new Runnable(){
			public void run() {
                controller.displayMessage(msg);
            }
		});
		writeInformationsInExcel();
		controller.activateButtons();
	}

	private String getTheEndMessage(String reason) {
		StringBuilder sb = new StringBuilder();
		sb.append(reason).append(", there are ").append(carInformations.size()).append(" cars scanned")
		.append("\n").append("there are ").append(pagesToScan.size()).append(" pages left to scan");
		long timeSpent = (System.currentTimeMillis() - this.startTime) / 1000;
		sb.append("\n").append("time spent is : ").append(timeSpent).append(" seconds")
		.append("\n").append("which means ").append(timeSpent/carInformations.size())
		.append(" seconds for each car information");
		return sb.toString();
	}
	
	private void updateProgression() {
		int total = carInformations.size() + pagesToScan.size();
		int max = maxNumber;
		if(maxNumber <= 0){
			max = total;
		}
		float contentProgression = (float)carInformations.size() / Math.min(total, max);
		float indexProgression = 1;
		if(!indexSpiderHandler.isEmpty()){
			IndexSpider iSpider = indexSpiderHandler.get(0);
			indexProgression = (float)iSpider.getScannedUrl().size() / (iSpider.getScannedUrl().size() + iSpider.getUrlToScan().size());
		}
		float progression = (contentProgression + indexProgression) / 2;
		this.currentProgression.set(progression);
		
	}
	
	/**
	 * stop every threads( all childrens are supposed to be stoped before this is called automaticly) 
	 */
	public void stop(){
		this.setRunning(false);
		this.indexSpiderHandler.stream().forEach(spider -> spider.stop());
		this.contentSpiderHandler.stream().forEach(spider -> spider.stop());

	}

	private void writeInformationsInExcel() {
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Cars");
        int rowNum = 0;
        System.out.println("Creating excel");
        
        Row labels = sheet.createRow(rowNum++);
        int colNum = 0;
        for(String field : fields){
        	Cell cell = labels.createCell(colNum++);
        	cell.setCellValue(field);
        }
        
        for(Car car : carInformations.values()){
        	Row carRow = sheet.createRow(rowNum++);
        	int colNumber = 0;
        	for(String field : fields){
            	Cell cell = carRow.createCell(colNumber++);
            	cell.setCellValue(car.getInformations().getOrDefault(field, ""));
            }
        }
        
        try {
        	String userHomeFolder = System.getProperty("user.home");
        	File excelFile = new File(userHomeFolder,"/Desktop/cars.xlsx");
            FileOutputStream outputStream = new FileOutputStream(excelFile);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Write to excel done");
        
	}

}
