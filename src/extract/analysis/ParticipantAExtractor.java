package extract.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tablecontents.ColumnContents;
import tablecontents.Fold;
import tablecontents.ParticipantA;
import tablecontents.Protein;
import extract.TextExtractor;
import extract.buffer.TableBuf;
import extract.buffer.TableBuf.Column;
import extract.lookup.AbbreviationLookup;
import extract.lookup.ChemicalLookup;
import extract.lookup.TabLookup;
import extract.types.Reaction;

public class ParticipantAExtractor {

	private void addPartA(List<ParticipantA> participantAs,
			String partAuntrans, String partA, ColumnContents f, Column col) {
		for (ParticipantA partAentry : participantAs){
			if (partAentry.equalString(partA)){
				partAentry.addToData(f, col);
				return;
			}
		}
		ParticipantA newA = new ParticipantA(partA, partAuntrans);
		newA.addToData(f, col);
		participantAs.add(newA);
	}
	
	private String translatePartA(String partA){
		ChemicalLookup chem = ChemicalLookup.getInstance();
		//TODO put in abbreviation and last capital method
		if(chem.chemicals.containsKey(partA.toUpperCase()))
			return chem.chemicals.get(partA.toUpperCase());
		for (Protein p : Protein.protList){
			String trans = p.cellMatch(partA);
			if (trans != null){
				return trans;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns original and last capital of partA
	 * @param partA
	 * @return
	 */
	private List<String> allForms(String partA){
		boolean cap = false;
		List<String> allForms = new ArrayList<String>();
		allForms.add(partA);
		for(int i = partA.length()-1; i >= 0; i--){
			if(cap == false && Character.isUpperCase(partA.charAt(i)) ){
				cap = true;
			}else if(cap == true && !Character.isUpperCase(partA.charAt(i))){
				allForms.add(partA.substring(i+1,partA.length()));
			}
		}
		return allForms;	
	}
	
	private String abbrLookup(String abbr) {
		String longForm = AbbreviationLookup.lookupAbbr(abbr.trim()).replaceAll("\\W", " ").toUpperCase();
		TabLookup proteinBase = TabLookup.getInstance();
		ChemicalLookup chem = ChemicalLookup.getInstance();
		if(proteinBase.english.containsKey(longForm)){
			List<String> intersect = proteinBase.english.get(longForm);
			if(proteinBase.english.containsKey(abbr)){
				List<String> abbrList = proteinBase.english.get(abbr);
				abbrList.retainAll(intersect);
				if(abbrList.size() > 0)
					return abbrList.get(0);
			}
		}
		return null;
	}
	private Pair<String, String> groundPartA(String form,Set<String>partBs){
		String partA = null;
		partA = translatePartA(form);
		if (partA == null && form.toUpperCase().equals(form)){
			partA = abbrLookup(form);
		}
		//TODO check this partB thing
		if(partA != null){ //&& !partBs.contains(partA)){
			return new Pair<String, String>(form, partA);
		}
		return null;
	}
	private Pair<String,String> checkPartA(TableBuf.Column col, Set<String> partBs){
		
		String normalized = col.getHeader().getData().replaceAll("-","");
		String [] split = normalized.split("\\s|;");//TODO look at removing / from it
		List<Pair<String,String>> possA = new ArrayList<Pair<String,String>>();
		for(String word: split){
			for (String form : allForms(word)){
				Pair<String,String> partA = groundPartA(form,partBs);
				if (partA != null)
					possA.add(partA);
			}
		}
		if(!possA.isEmpty())
			return possA.get(0);
		return null;
	}
	
	private String checkPartAText(Set<String> allB,String pmcid, Reaction r, Set<String> possA){
		List<String>  textA= TextExtractor.extractParticipantA(allB, pmcid,r.getConjugationBase());
		for(String aText : textA){
			String transTextA = translatePartA(aText);
			for(String aTable : possA){
				if(aTable.equals(transTextA))
					return aTable;
			}
		}
		return null;
	}

	public List<ParticipantA> getParticipantAs(TableBuf.Table table,
			HashMap<Integer,String> partB, HashMap<ColumnContents,List<TableBuf.Column>> contents,
			Reaction r){
		List<ParticipantA> participantAs = new ArrayList<ParticipantA>();
		Set<String> allB = new HashSet<String>();
		TabLookup t = TabLookup.getInstance();
		for(String s : partB.values()){
			if (s.contains("Uniprot")){
				String cut = s.substring(8);
				if(t.uniToGene.containsKey(cut)){	
					allB.add(t.uniToGene.get(cut));
				}
			}
		}
		allB.addAll(partB.values());
		for(ColumnContents f : contents.keySet()){
			for (TableBuf.Column col : contents.get(f)){
				Pair<String,String> partA = checkPartA(col, allB);
				
				if (partA != null){
					addPartA(participantAs, partA.getA(), partA.getB(), f, col);
				}
			}
		}
		if (participantAs.isEmpty()){
			System.out.println("NO FOLD A");
			//Translated maps to Untranslated
			HashMap<String, String> possA = new HashMap<String, String>();
			for(String caption : table.getCaptionList()){
				caption = caption.replaceAll("-", "");
				Pattern p = Pattern.compile("[A-Z[a-z]][\\w]*[A-Z]+[\\w]*");//TODO examine this regex
				Matcher m = p.matcher(caption);
				while(m.find()){
					String a = m.group();
					System.out.println(a);
					Pair<String, String> word = groundPartA(a,allB);
					if(word!= null){
						possA.put(word.getB(), word.getA());
					}
				}
			}
			String partA = checkPartAText(allB, table.getSource().getPmcId().substring(3), r, possA.keySet());
			if(partA!= null){
				participantAs.add(new ParticipantA(partA, possA.get(partA), contents));
				return participantAs;
			}
		}else{
			return participantAs;
		}
		participantAs.add(new ParticipantA("unknown", "unknown", contents));
		return participantAs;
	}

}
