package utils;

import java.util.Iterator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

public class WebDriverUtils {

	public static final WebDriver getWebDriver() {
		//https://stackoverflow.com/questions/6509628/how-to-get-http-response-code-using-selenium-webdriver/39979509#39979509
		//get http response code from selenium web driver by enabling performance logging and retrieve the code from the log
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(LogType.PERFORMANCE, java.util.logging.Level.ALL);
        ChromeOptions options = new ChromeOptions();
        options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        
		return new ChromeDriver(options);
	}
	
	/**
	 * 
	 * @param driver
	 * @param url
	 * @param wantHttpCode
	 * @return -1 if dont want http code
	 */
	public static final int navigate(final WebDriver driver, final String url, final boolean wantHttpCode) {
		driver.navigate().to(url);
		int httpCode = -1;
		if(wantHttpCode) {
			httpCode = getHttpCode(driver);
		}
		return httpCode;
	}
	
	/**
	 * 
	 * @param driver
	 * @param target
	 * @param wantHttpCode
	 * @return -1 if dont want http code
	 */
	public static final int click(final WebDriver driver, final String target, final boolean wantHttpCode) {
		//TODO find target, and click it
		int httpCode = -1;
		if(wantHttpCode) {
			httpCode = getHttpCode(driver);
		}
		return httpCode;
	}
	
	public static final void fill(final WebDriver driver, final String target, final String content) {
		//TODO find target, and click it
	}
	
	public static final int getHttpCode(final WebDriver driver) {
		LogEntries logs = driver.manage().logs().get("performance");
		for (Iterator<LogEntry> it = logs.iterator(); it.hasNext();)
        {
            LogEntry entry = it.next();
            try
            {
                System.out.println(entry);
            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		return 0;
	}
	
	public static final void setWebDriverSystemProperty() {
		System.setProperty("webdriver.chrome.driver", "src/main/resources/webDrivers/chromedriver.exe");
	}
}
