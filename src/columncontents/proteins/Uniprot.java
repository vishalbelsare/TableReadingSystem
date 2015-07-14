package columncontents.proteins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uniprot extends Protein{

	private String regEx = "([OPQ][0-9][A-Z0-9]{3}[0-9])|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}";

	public String groundIdentity(String ungrounded) {
		if(t.uniprot.containsKey(ungrounded))
			return("Uniprot:" + t.uniprot.get(ungrounded));
		return null;
	}

	public String matchesFormat(String input) {
		return super.matchesFormat(input, regEx);
	}	
	
	
}