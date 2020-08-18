package model;

public class CarContentPage {

	private String url;
	private String carLocalisation;
	private String carVersion;
	
	public CarContentPage(String url, String carLocalisation, String carVersion) {
		super();
		this.url = url;
		this.carLocalisation = carLocalisation;
		this.carVersion = carVersion;
		
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getCarLocalisation() {
		return carLocalisation;
	}
	
	public void setCarLocalisation(String carLocalisation) {
		this.carLocalisation = carLocalisation;
	}
	
	@Override
	public boolean equals(Object other){
		boolean result = false;
		if(other instanceof CarContentPage){
			CarContentPage otherPage = (CarContentPage) other;
			result = otherPage.getUrl().trim().equals(this.getUrl().trim());
		}
		return result;
	}

	public String getCarVersion() {
		return carVersion;
	}

	public void setCarVersion(String carVersion) {
		this.carVersion = carVersion;
	}
}
