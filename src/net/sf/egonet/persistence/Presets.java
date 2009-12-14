package net.sf.egonet.persistence;

import java.util.TreeMap;

public class Presets {
	public static TreeMap<String,String[]> get() {
		TreeMap<String,String[]> result = new TreeMap<String,String[]>();
		result.put("Kin", new String[] {
				"Grand Mother","Grand Father","Mother","Father",
				"Grand Son","Grand Daughter","Son","Daughter",
				"Sister","Brother","Aunt","Uncle","Niece","Nephew","Cousin",
				"Mother-in-Law","Father-in-Law","Sister-in-Law","Brother-in-Law",
				"Step Son","Step Daughter","Half-Brother","Half-Sister"});
		result.put("Gender", new String[] {"Male","Female"});
		result.put("Yes/No", new String[] {"Yes","No"});
		result.put("States", new String[] {
				"Alabama","Alaska","Arizona","Arkansas","California","Colorado",
				"Connecticut","Deleware","Florida","Georgia","Hawaii","Idaho",
				"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine",
				"Maryland","Massachusetts","Michigan","Minnesota","Mississippi",
				"Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey",
				"New Mexico","New York","North Carolina","North Dakota","Ohio",
				"Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina",
				"South Dakota","Tennessee","Texas","Utah","Vermont","Virginia",
				"Washington","West Virginia","Wisconsin","Wyoming"});
		return result;
	}
}
