package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Car having informations
 * @author Linxuhao
 *
 */
public class Car {

	private Map<String,String> informations;
	
	/**
	 * generate a car with empty informations from a list fields you are asking it to collect
	 * @param fields
	 */
	public Car(List<String> fields) {
		super();
		this.informations = new HashMap<String,String>();
		for(String field : fields){
			informations.put(field, null);
		}
	}

	public Map<String,String> getInformations() {
		return informations;
	}

	public void setInformations(Map<String,String> informations) {
		this.informations = informations;
	}
	
}
