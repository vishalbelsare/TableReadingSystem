package extract.types;

import tablecontents.*;
public class Ubiquitination extends Reaction{
	private static  Ubiquitination instance = null;
	
	private Ubiquitination(){
		//TODO add the neccesary requirements
		conjugationBase.add("ubiquitinat");
	}
	public static Reaction getInstance(){
		if(instance == null){
			instance = new Ubiquitination();
		}
		return instance;
	}

}