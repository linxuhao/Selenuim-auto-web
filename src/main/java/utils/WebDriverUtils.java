package utils;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import customExceptions.LoggedException;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import static constants.SeleniumXPathConstants.ATTRIBUTE_CLASSNAME;
import static constants.SeleniumXPathConstants.ATTRIBUTE_ID;
import static constants.SeleniumXPathConstants.ATTRIBUTE_NAME;
import static constants.SeleniumXPathConstants.ATTRIBUTE_TEXT;
import static constants.SeleniumXPathConstants.ATTRIBUTE_VALUE;
import static constants.SeleniumXPathConstants.ATTRIBUTE_PLACEHOLDER;
import static constants.SeleniumXPathConstants.TAG_BUTTON;
import static constants.SeleniumXPathConstants.TAG_INPUT;
import static constants.SeleniumXPathConstants.TAG_LINK;
import static constants.SeleniumXPathConstants.TAG_SELECT;

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
	 * 
	 * @param driver
	 * @param target
	 * @param wantHttpCode
	 * @return httpcode of the action, -1 if dont want http code
	 */
	public static final int click(final WebDriver driver, final String target) {
		final WebElement element = findByXPathQuerySupplier(driver,
				() -> createClickableElementFindXPathQueryList(target));
		if (null != element) {
			element.click();
		} else {
			throw new LoggedException(Level.ERROR, "Click, Couldnt find the target " + target);
		}
		return getHttpCode(driver);
	}

	public static final void fill(final WebDriver driver, final String target, final String content) {
		final WebElement element = findByXPathQuerySupplier(driver,
				() -> createSimpleElementFindXPathQueryList(TAG_INPUT, target));
		if (null != element) {
			element.sendKeys(content);
		} else {
			throw new LoggedException(Level.ERROR, "Fill, Couldnt find the target " + target);
		}
	}

	public static void select(final WebDriver driver, final String target, final String content) {
		final WebElement element = findByXPathQuerySupplier(driver,
				() -> createSimpleElementFindXPathQueryList(TAG_SELECT, target));
		if (null != element) {
			Select selectElement = new Select(element);
			selectElement.selectByVisibleText(content);
		} else {
			throw new LoggedException(Level.ERROR, "Select, Couldnt find the target " + target);
		}
	}

	private static WebElement findByXPathQuerySupplier(final WebDriver driver,
			final Supplier<List<String>> querySupplier) {
		return findByXPathQueryList(driver, querySupplier.get());
	}

	/**
	 * <b>Priority : </b><br>
	 * id, <br>
	 * placeholder,<br>
	 * name,<br>
	 * value,<br>
	 * classname<br>
	 * 
	 * @return
	 */
	private static List<String> createSimpleElementFindXPathQueryList(final String tagName, final String target) {
		final String byId = createSimpleXPathQuery(tagName, ATTRIBUTE_ID, target);
		final String byPlaceholder = createSimpleXPathQuery(tagName, ATTRIBUTE_PLACEHOLDER, target);
		final String byName = createSimpleXPathQuery(tagName, ATTRIBUTE_NAME, target);
		final String byValue = createSimpleXPathQuery(tagName, ATTRIBUTE_VALUE, target);
		final String byClassname = createSimpleXPathQuery(tagName, ATTRIBUTE_CLASSNAME, target);
		return Arrays.asList(byId, byPlaceholder, byName, byValue, byClassname);
	}

	/**
	 * 
	 * @param tagname
	 * @param attributeName
	 * @param target
	 * @return xpath=//{tagname}[contains(@{attributeName},'{target}')]
	 */
	private static String createSimpleXPathQuery(final String tagname, final String attributeName,
			final String target) {
		return String.format(StringEscapeUtils.escapeJava("//%s[contains(@%s,'%s')]"), tagname, attributeName,
				target);
	}

	/**
	 * https://stackoverflow.com/questions/23078308/selenium-and-xpath-locating-a-link-by-containing-text
	 * 
	 * @param tagname
	 * @param target
	 * @return xpath=//{tagname}[text()[contains(.,'{target}')]]"
	 */
	private static String createTextXPathQuery(final String tagname, final String target) {
		return String.format(StringEscapeUtils.escapeJava("//%s[%s[contains(.,'%s')]]"), tagname, ATTRIBUTE_TEXT,
				target);
	}

	/**
	 * <b>Priority : </b><br>
	 * button with id, name<br>
	 * <br>
	 * link (a) with id, name<br>
	 * <br>
	 * <br>
	 * input with id, name <br>
	 * <br>
	 * button, link, input with value<br>
	 * <br>
	 * button, link, input with text<br>
	 * <br>
	 * button link, input with classname<br>
	 * 
	 * @param target
	 * @return
	 */
	private static List<String> createClickableElementFindXPathQueryList(final String target) {
		final List<String> queryList = new ArrayList<>();
		queryList.add(createSimpleXPathQuery(TAG_BUTTON, ATTRIBUTE_ID, target));
		queryList.add(createSimpleXPathQuery(TAG_BUTTON, ATTRIBUTE_NAME, target));
		
		queryList.add(createSimpleXPathQuery(TAG_LINK, ATTRIBUTE_ID, target));
		queryList.add(createSimpleXPathQuery(TAG_LINK, ATTRIBUTE_NAME, target));
		
		queryList.add(createSimpleXPathQuery(TAG_INPUT, ATTRIBUTE_ID, target));
		queryList.add(createSimpleXPathQuery(TAG_INPUT, ATTRIBUTE_NAME, target));
		
		queryList.add(createTextXPathQuery(TAG_BUTTON, target));
		queryList.add(createTextXPathQuery(TAG_LINK, target));
		queryList.add(createTextXPathQuery(TAG_INPUT, target));
		
		queryList.add(createSimpleXPathQuery(TAG_BUTTON, ATTRIBUTE_VALUE, target));
		queryList.add(createSimpleXPathQuery(TAG_LINK, ATTRIBUTE_VALUE, target));
		queryList.add(createSimpleXPathQuery(TAG_INPUT, ATTRIBUTE_VALUE, target));
		
		queryList.add(createSimpleXPathQuery(TAG_BUTTON, ATTRIBUTE_CLASSNAME, target));
		queryList.add(createSimpleXPathQuery(TAG_LINK, ATTRIBUTE_CLASSNAME, target));
		queryList.add(createSimpleXPathQuery(TAG_INPUT, ATTRIBUTE_CLASSNAME, target));

		return queryList;
	}

	/**
	 * Find by priority in the list, the smaller the index in the query list, the
	 * higher the priority
	 * 
	 * @param driver
	 * @param xPathQueryList
	 * @return null if not find
	 */
	private static final WebElement findByXPathQueryList(final WebDriver driver, final List<String> xPathQueryList) {
		for (String xPathQuery : xPathQueryList) {
			final List<WebElement> elements = driver.findElements(By.xpath(xPathQuery));
			if (!elements.isEmpty()) {
				//get the first
				return elements.get(0);
			}
		}
		return null;
	}

	/**
	 * Get the http response code of the currently displayed page
	 * 
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
