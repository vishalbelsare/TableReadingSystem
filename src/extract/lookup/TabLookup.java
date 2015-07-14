package extract.lookup;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Lookup tables for grounding entities
 * EnglishNames are standardized, by eliminating all seperators and putting everything to uppercase
 * @author sloates
 *
 */
public class TabLookup{
	
	public HashMap<String,String> uniprot = new HashMap<String,String>();
	public HashMap<String,String> swisprot = new HashMap<String,String>();
	public HashMap<String,String> genename = new HashMap<String,String>();
	public HashMap<String, LinkedList<String>> english = new HashMap<String,LinkedList<String>>();
	public HashMap<String, String> uniToGene = new HashMap<String, String>();
	
	private static TabLookup instance=null;
	
	public static TabLookup getInstance(){
		if(instance == null) {
			instance = new TabLookup();
		}
		return instance;
	}
	private TabLookup(){
		File proteins = new File("uniprot-taxonomy%3A-Mammalia+%5B40674%5D-.tab");
		Scanner s;
		String uni ="";
		try {
			s = new Scanner(proteins);
			s.nextLine();
			String curr =  s.nextLine();
			while(s.hasNext()){
				String[]line = curr.split("\t");
				uni= line[0];
				String swis = line[1];
				String description = line[3];
				for(String genes: line[4].split("\\s")){
					if(swis.contains("HUMAN")){
						genename.put(genes.toUpperCase(),uni);
						uniToGene.put(uni, genes.toUpperCase());
					}
				}
				uniprot.put(uni, uni);
				swisprot.put(swis, uni);
				
				//TODO decide on whether
				for(String eng: description.split("\\s\\(")){
					eng = eng.replaceAll("\\)", "");
					eng = eng.replaceAll("\\W+", " ");
					if(english.containsKey(eng.toUpperCase())){
						english.get(eng.toUpperCase()).add(uni);
					}else{
						LinkedList<String> newEng = new LinkedList<String>();
						newEng.add(uni);
						english.put(eng.toUpperCase(), newEng);
					}
				}
				curr = s.nextLine();
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}