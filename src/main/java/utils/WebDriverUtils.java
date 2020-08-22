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
	 * @return httpcode of the action, -1 if dont want http code
	 */
	public static final int navigate(final WebDriver driver, final String url) {
		driver.navigate().to(url);
		return getHttpCode(driver);
	}

	/**
	 * Click returns an http code because there are cases of hyperlink clicking
	 * @param driver
	 * @param target
	 * @param wantHttpCode
	 * @return httpcode of the action, -1 if dont want http code
	 */
	public static final int click(final WebDriver driver, final String target) {
		return getHttpCode(driver);
	}

	public static final void fill(final WebDriver driver, final String target, final String content) {
		// TODO find target, and click it
	}

	/**
	 * Get the http response code of the currently displayed page
	 * @param driver
	 * @return
	 */
	public static final int getHttpCode(final WebDriver driver) {
		final String url = driver.getCurrentUrl();
		return urlToResponseCodeMap.getOrDefault(url, HTTP_CODE_URL_NOT_FOUND);
	}

	public static final void setWebDriverSystemProperty() {
		System.setProperty("webdriver.chrome.driver", "src/main/resources/webDrivers/chromedriver.exe");
	}
}
