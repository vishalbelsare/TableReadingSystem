package columncontents.proteins;

public class GeneName extends Protein {

	private String regEx = "[A-Z]{3}[0-9[A-Z]]{1,5}";
	private static GeneName gene = null;
	public static GeneName getInstance(){
		if(gene == null)
			gene = new GeneName();
		return gene;
	}
	private GeneName(){
		
	}
	public String groundIdentity(String ungrounded) {
		ungrounded = ungrounded.toUpperCase();
		if(t.genename.containsKey(ungrounded))
			return("Uniprot:" + t.genename.get(ungrounded));
		return null;
	}
	public String matchesFormat(String input) {
		return super.matchesFormat(input, regEx);
	}	

}
