package tablecontents;

public class MethylAmino extends Amino {
	private String cellRegEx = "\bk\b|\blys\b";
	private static MethylAmino methAmino = null;
	public static MethylAmino getInstance(){
		if(methAmino == null)
			methAmino = new MethylAmino();
		return methAmino;
	}
	private MethylAmino(){
		
	}

	@Override
	public String cellMatch(String match) {
		return super.cellMatch(match, cellRegEx);
	}
}
