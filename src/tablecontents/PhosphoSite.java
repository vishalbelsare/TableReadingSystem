package tablecontents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tablecontents.ColumnContents;

public class PhosphoSite extends Site implements ColumnContents {
	//TODO fix this up
	private static PhosphoSite phos = null;
	public static ColumnContents getInstance() {
		if(phos == null)
			phos = new PhosphoSite();
		return phos;
	}
	private PhosphoSite(){
		regEx = "\\b(p?[SYT][\\W]{0,1}\\d{3,5})\\b|\\b(([Ss][Ee][Rr])|([Tt][Yy][Rr])|([Tt][Hh][Rr]))-*\\W?\\d{3,5}\\b";
		extractRegEx = "\\b(p?[SYT][\\W]{0,1}\\d{1,5})\\b|\\b(([Ss][Ee][Rr])|([Tt][Yy][Rr])|([Tt][Hh][Rr]))-*\\W?\\d{1,5}\\b";
	}
	@Override
	public String headerMatch(String match) {
		return null;
	}
	
	@Override
	public String cellMatch(String match) {
		return super.cellMatch(match,regEx);
	}
}
