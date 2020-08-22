package utils;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

public class WebDriverUtils {
	
	public static final int HTTP_CODE_URL_NOT_FOUND = -2;

	private static BrowserMobProxy proxy;
	private static final Map<String, Integer> urlToResponseCodeMap = new HashMap<>();

	synchronized public static final BrowserMobProxy getBrowserMobProxy() {
		if (null == proxy) {
			proxy = new BrowserMobProxyServer();
			proxy.start(0);
			proxy.addResponseFilter((response, messageContent, MessageInfo) -> {
				urlToResponseCodeMap.put(MessageInfo.getUrl(), response.getStatus().code());
			});
		}
		return proxy;
	}

	public static final WebDriver getNewWebDriver() {
		final Proxy seleniumProxy = ClientUtil.createSeleniumProxy(getBrowserMobProxy());
		final ChromeOptions options = new ChromeOptions();
		options.setProxy(seleniumProxy);
		options.addArguments("--ignore-certificate-errors");
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
		if (wantHttpCode) {
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
		// TODO find target, and click it
		int httpCode = -1;
		if (wantHttpCode) {
			httpCode = getHttpCode(driver);
		}
		return httpCode;
	}

	public static final void fill(final WebDriver driver, final String target, final String content) {
		// TODO find target, and click it
	}

	public static final int getHttpCode(final WebDriver driver) {
		final String url = driver.getCurrentUrl();
		if(urlToResponseCodeMap.containsKey(url)) {
			return urlToResponseCodeMap.get(url);
		}
		return -2;
	}

	public static final void setWebDriverSystemProperty() {
		System.setProperty("webdriver.chrome.driver", "src/main/resources/webDrivers/chromedriver.exe");
	}
}
