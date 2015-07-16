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
			String partA, ColumnContents f, Column col) {
		//TODO make this structure into a class.
		for (ParticipantA partAentry : participantAs){
			if (partAentry.equalString(partA)){
				partAentry.addToData(f, col);
				return;
			}
		}
		ParticipantA newA = new ParticipantA(partA);
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
	private String groundPartA(String form,Set<String>partBs){
		String partA = null;
		if (form.toUpperCase().equals(form)){
			partA = abbrLookup(form);
		} else {
			partA = translatePartA(form);//TODO check for bad words and grounding
		}
		if(!partBs.contains(partA)){
			return partA;
		}
		return null;
	}
	private String checkPartA(TableBuf.Column col, Set<String> partBs){
		
		String [] split = col.getHeader().getData().split("\\s|;");
		List<String> possA = new ArrayList<String>();
		for(String word: split){
			for (String form : allForms(word)){
				String partA = groundPartA(form,partBs);
				if (partA != null)
					possA.add(partA);
			}
		}
		if(!possA.isEmpty())
			return possA.get(0);
		//TODO check against participantB List
		return null;
	}
	
	private String checkPartAText(Set<String> allB,String pmcid, Reaction r, List<String> possA){
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
			String cut = s.substring(8);
			if(t.uniToGene.containsKey(cut)){	
				allB.add(t.uniToGene.get(cut));
			}
		}
		allB.addAll(partB.values());
		for(ColumnContents f : contents.keySet()){
			for (TableBuf.Column col : contents.get(f)){
				String partA = checkPartA(col, allB);
				if (partA != null){
					addPartA(participantAs, partA, f, col);
				}
			}
		}
		if (participantAs.isEmpty()){
			List<String> possA = new ArrayList<String>();
			for(String caption : table.getCaptionList()){
				Pattern p = Pattern.compile("[A-Z[a-z]][\\w]*[A-Z]+[\\w]*");
				Matcher m = p.matcher(caption);
				while(m.find()){
					String word = groundPartA(m.group(),allB);
					if(word!= null){
						possA.add(word);
					}
				}
			}
			String partA = checkPartAText(allB, table.getSource().getPmcId().substring(3), r,possA);
			if(partA!= null){
				participantAs.add(new ParticipantA(partA, contents));
				return participantAs;
			}
		}else{
			return participantAs;
		}
		participantAs.add(new ParticipantA("unknown", contents));
		return participantAs;
	}

}
