package tablecontents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tablecontents.ColumnContents;
import extract.buffer.TableBuf;

public abstract class Site implements ColumnContents {
	private String headerRegEx = "site|residue|syt|location|tyrosine";//|position";//TODO write the header
	private String regEx = null;
	/**
	 * Returns cutoff values for positions
	 * @return
	 */
	public int[] validPosition(){
		return new int[]{
			1,20,10000
		};
	}
	
	@Override
	public String headerMatch(String match) {
		Pattern p = Pattern.compile(headerRegEx,Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(match);
		if(m.find()){
			return m.group();
		}
		return null;
	}
	
	public String bestColumn(HashMap<ColumnContents,List<TableBuf.Column>> cols, int row){
		return null;
	}
	
	public String cellMatch(String match, String regEx) {
		String sites = "";
		Pattern p = Pattern.compile(regEx,Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(match);
		while(m.find())
			sites += "," + m.group();
		sites = sites.replaceAll(",$", "");
		if(sites.equals(""))
			return null;
		return sites;
	}
	@Override
	public HashMap<String, String> extractData (List<TableBuf.Column> cols, int row){
		HashMap<String,String> siteBase = new HashMap<String,String>();
		String sites = "";
		String aminos = "";
		Pattern site = Pattern.compile(regEx);
		for(TableBuf.Column c : cols){
			TableBuf.Cell cell = c.getData(row);
			if(cell != null){
				String data = cell.getData();
				if(data != null){
					Matcher m = site.matcher(data);
					while(m.find()){
						String [] both = m.group().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
						if(both.length== 2){
							sites += both[1] + ", ";
							aminos += both[0].replaceAll("[^A-Z[a-z]]", "") + ", ";
						}
					}
				}
			}
		}
		if(!sites.equals("")){
			siteBase.put("site", sites.substring(0, sites.length()-1));
		}
		if(!aminos.equals("")){
			siteBase.put("base",sites.substring(0,sites.length() - 1 ) );
		}
		return siteBase;		
	}
}
 