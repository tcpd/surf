package in.edu.ashoka.lokdhaba;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.*;

/**
 * Created by sudx on 6/6/16.
 */
public class ConcreteNameData implements NameData {

    Multimap<String, Multimap<String, Row>> mappedNames;
    Queue<NamePair> pipeline;

    public void initialize() {
    	try {
    		Bihar.main(null);
        	mappedNames = Bihar.getSimilarPairs("/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv");
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    }

    public void iterateNameData() {
    	pipeline = new LinkedList<NamePair>();
        List<Row> list = new ArrayList<Row>();
        list.clear();
        for(String key :mappedNames.keys()) {

            Collection<Multimap<String, Row>> collection = mappedNames.get(key);

            for(Multimap<String, Row> mp:collection){
                for(Row row:mp.values()) {
                    list.add(row);
                }
            }


            /*for(int i=1; i<list.size() && list.size()>1; i++) {
                NamePair namePair = new NamePair(list.get(i-1),list.get(i));
                pipeline.add(namePair);
            }*/
            for(int i=0;i<list.size();i++) {
                for(int j=i;j<list.size();j++) {
                    if(i==j)
                        continue;
                    else {
                        NamePair namePair = new NamePair(list.get(i),list.get(j));
                        pipeline.add(namePair);
                    }
                }
            }

        }
    }

    @Override
    public void setSame(NamePair np) {

    }

    @Override
    public void purgeNamePair(NamePair np) {

    }

    
    
    @Override
    public Iterator iterator(){
    	return new NameDataIterator(pipeline);
    }
}

class NameDataIterator implements Iterator {
	
	//Queue<NamePair> pipeline;
	Iterator iterator;
	public NameDataIterator(Queue<NamePair> pipeline) {
		//this.pipeline = pipeline;
		iterator = pipeline.iterator();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Object next() {
		// TODO Auto-generated method stub
		return iterator.next();
	}
	
}
