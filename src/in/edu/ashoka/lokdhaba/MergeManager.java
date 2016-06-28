package in.edu.ashoka.lokdhaba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.Multimap;

public interface MergeManager {
	
	//initialize the id's for each row
	public void initializeIds();
	
	void performInitialMapping();
	
	//add candiates which will be judged based on their group
	public void addSimilarCandidates();
	
	public void merge(String [] ids);
	public void deMerge(String [] ids);
	public void save();
	public ArrayList<Multimap<String,Row>> getIncumbents();
	
	//check whether this row is mapped to another name
	public boolean isMappedToAnother(String id);
	
	public String getRootId(String id);
	public void updateMappedIds();
}
