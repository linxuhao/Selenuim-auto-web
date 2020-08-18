package spider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import model.Car;
import model.CarContentPage;

/**
 * a spider scan a car
 * @author Linxuhao
 *
 */
public class ContentSpider implements Runnable{
	
	public static final String GENERAL_INFO_KEY = "infoGeneraleTxt";
	public static final String MAIN_CONTENT_KEY = "mainContent";
	public static final String END_MAIN_CONTENT_KEY = "mainAside";
	
	public static final String FIELD_NAME_PRICE = "prix";
	public static final String FIELD_NAME_LOCALISATION = "localisation";
	public static final String FIELD_NAME_FINITION = "finition";
	
	public static final String PRICE_KEY = "gpfzj";
	public static final String FINITION_KEY = "versionTxt";
	
	public static final int CONNECT_TIME_OUT = 10000;
	private SpiderHandler handler;
	private CarContentPage page;
	private boolean isRunning;
	
	public ContentSpider(SpiderHandler handler, CarContentPage page) {
		super();
		this.handler = handler;
		this.page = page;
	}
	
	public SpiderHandler getHandler() {
		return handler;
	}

	public void setHandler(SpiderHandler handler) {
		this.handler = handler;
	}

	
	public CarContentPage getPage() {
		return page;
	}

	public void setPage(CarContentPage page) {
		this.page = page;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	@Override
	public void run() {
		this.setRunning(true);
		
		Car car = new Car(handler.getFields());
		searchOnPageAndFillTheFields(page.getUrl(), car);
		//save the data into the handler once work is finished
		handler.getCarInformations().put(page.getUrl(), car);
		this.stop();
	}
	
	private void searchOnPageAndFillTheFields(String urlToSearch, Car car) {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL(urlToSearch).openConnection();
			conn.setConnectTimeout(CONNECT_TIME_OUT);
			conn.setRequestProperty("User-Agent","LinxuhaoBot(xuhao.lin@etu.utc.fr)"
					+ " - (Collection des données sur les voitures d'occasion pour faire une étude de variation de prix)");
			conn.connect();
			
			InputStream input = conn.getInputStream();
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(input));
	        String inputLine;
	        StringBuilder sb = new StringBuilder();
	        boolean isMainContent = false;
	        while ((inputLine = in.readLine()) != null){
	        	if(inputLine.contains(MAIN_CONTENT_KEY)){	
	        		isMainContent = true;
	        	}
	        	if(inputLine.contains(END_MAIN_CONTENT_KEY)){	
	        		isMainContent = false;
	        	}
	        	if(isMainContent){
	        		sb.append(inputLine);
	        	}
	        }
	        fillTheCar(car, sb.toString());
	        input.close();
	        in.close();
	        conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			//if there is a issue while connecting to the site, it means we have to stop
			handler.stop();
			e.printStackTrace();
		}
	}

	private void fillTheCar(Car car, String bodyContent) {
		Document doc = Jsoup.parseBodyFragment(bodyContent);
		Element body = doc.body();
		Element infosGeneral = body.selectFirst("ul." + GENERAL_INFO_KEY);
		//for ignore case comparator
		Map<String, String> generalInfo = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		//collect all general informations 
		for(Element element : infosGeneral.children()){
			String[] things = element.text().split(":");
			String fieldName = things[0].trim();
			String content = "";
			if(things.length >= 2){
				content = things[1].trim();
			}
			generalInfo.put(fieldName, content);
		}
		//try to fill every fields user asks
		for(String field : this.handler.getFields()){
			if(field.equalsIgnoreCase(FIELD_NAME_PRICE)){
				//if user wants the price, find the price
				Element el = body.selectFirst("div." + PRICE_KEY);
				String price = el.selectFirst("strong").text();
				car.getInformations().put(field, price);
			}else{
				if(field.equalsIgnoreCase(FIELD_NAME_LOCALISATION)){
					car.getInformations().put(field, page.getCarLocalisation());
				}else{
					if(field.equalsIgnoreCase(FIELD_NAME_FINITION)){
						car.getInformations().put(field, page.getCarVersion());
					}else{
						//fields we can find in general informations 
						if(generalInfo.containsKey(field)){
							car.getInformations().put(field, generalInfo.get(field));
						}else{
							System.err.println(field + " has not found match");
						}
					}
				}
			}
		}
	}

	public void stop(){
		this.setRunning(false);
		this.handler.getContentSpiderHandler().remove(this);
	}
}
