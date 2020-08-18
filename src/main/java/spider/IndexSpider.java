package spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import model.CarContentPage;

/**
 * a spider scan the result pages and put links to cars in a stack
 * @author Linxuhao
 *
 */
public class IndexSpider implements Runnable{
	
	/**
	 * carr version must have this to be marked toScan
	 */
	public static final String VERSION_CONTAIN_1 = "IV";
	/**
	 * carr version must have this to be marked toScan
	 */
	public static final String VERSION_CONTAIN_2 = "INTENS";
	/**
	 * carr version must not have this to be marked toScan
	 */
	public static final String VERSION_BAN_1 = "ESTATE";
	/**
	 * the key word to find pagination part on lacentral page
	 */
	public static final String PAGINATION_KEY = "rch-pagination";
	/**
	 * the key word to find content part on lacentral page
	 */
	public static final String CONTENT_KEY = "resultListContainer";
	private SpiderHandler handler;
	private Set<String> scannedUrl;
	private Set<String> urlToScan;
	private String baseUrl;
	private boolean isRunning;
	
	public IndexSpider(SpiderHandler handler, String url) {
		super();
		this.handler = handler;
		this.urlToScan = new LinkedHashSet<String>();
		URL firstToScan;
		try {
			this.urlToScan.add(url);
			firstToScan = new URL(url);
			this.baseUrl = firstToScan.getProtocol().concat("://")
					.concat(firstToScan.getAuthority());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.scannedUrl = new HashSet<String>();
	}
	
	public SpiderHandler getHandler() {
		return handler;
	}

	public void setHandler(SpiderHandler handler) {
		this.handler = handler;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public Set<String> getUrlToScan() {
		return urlToScan;
	}

	public void setUrlToScan(Set<String> urlToScan) {
		this.urlToScan = urlToScan;
	}

	public Set<String> getScannedUrl() {
		return scannedUrl;
	}

	public void setScannedUrl(Set<String> scannedUrl) {
		this.scannedUrl = scannedUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	public void run() {
		this.setRunning(true);
		while(this.isRunning){
			//if no url left to scan, stop self
			if(this.urlToScan.isEmpty()){
				this.stop();
			}else{
				try {
					String url = pop(this.urlToScan);
					searchAndAddLinksFromUrl(url);
				} catch (Exception e) {
					//if anything wrong happens, stop all crawling
					handler.stop();
					e.printStackTrace();
				}
			}
			//being polite, not doing a ddos attack
			try {
				Thread.sleep(SpiderHandler.gentlemanWaitingTime);
			} catch (InterruptedException e) {
				handler.stop();
				e.printStackTrace();
			}
		}
	}
	
	private void searchAndAddLinksFromUrl(String url) throws IOException {
		String[] urlString = url.split("=");
		System.out.println("scanned index page number : " + urlString[urlString.length-1]);
		URL urlToScan = new URL(url);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(urlToScan.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null){
        	// lacentral's content are all in same freak line, so i get this line(the entire content i care about)
        	// and i dont care about all other lines that's where comes the break
        	if(inputLine.contains(CONTENT_KEY)){
        		Document doc = Jsoup.parseBodyFragment(inputLine);
        		Element body = doc.body();
        		Element pagination = body.selectFirst("div." + PAGINATION_KEY);
        		Elements paginationLinks = pagination.select("a[href]");
        		for(Element link : paginationLinks){
        			String linkHref = baseUrl + link.attr("href");
        			//add the url to scan if i didnt scanned it before
        			if(!this.scannedUrl.contains(linkHref)){
        				this.urlToScan.add(linkHref);
        			}
        		}
        		Element content = body.selectFirst("div." + CONTENT_KEY);
        		Elements contentLinks = content.select("a[href]");
        		int addCount = 0;
        		for(Element link : contentLinks){
        			String linkHref = baseUrl + link.attr("href");
        			String carLocalisation = link.selectFirst("div." + "dptCont").text().replace(",", "");
        			String carVersion = link.selectFirst("span." + "version").text();
        			//add the page we are interested to scan if is intens and IV
        			if(carVersion.contains(VERSION_CONTAIN_1) && carVersion.contains(VERSION_CONTAIN_2)){
        				//add ban words
        				if(!carVersion.contains(VERSION_BAN_1)){
        					CarContentPage page = new CarContentPage(linkHref, carLocalisation, carVersion);
                			//add the url to scan if i didnt scanned it before
                			if(!handler.getCarInformations().containsKey(linkHref) && !handler.getPagesToScan().contains(page) && !handler.getWorkingPages().contains(page)){
                				handler.getPagesToScan().push(page);
                				addCount++;
                			}
        				}
        				
        			}
        			
        		}
        		System.out.println("Added " + addCount + " pages to scan, now there are " + handler.getPagesToScan().size() + " waiting to be scanned");
        		break;
        	}
        }
        in.close();
		
	}

	/**
	 * pop a url string from a set and add it to scanned url
	 * @param urls
	 * @return
	 */
	private String pop(Set<String> urls) {
		Iterator<String> i = urls.iterator();
		String next = i.next();
		//add into scanned url
		scannedUrl.add(next);
		//remove from url to scan
		i.remove();
		return next;
	}

	public void stop(){
		System.out.println("Index finished, There are " + scannedUrl.size() + " pages scanned");
		this.setRunning(false);
		handler.getIndexSpiderHandler().remove(this);
	}
	
}
