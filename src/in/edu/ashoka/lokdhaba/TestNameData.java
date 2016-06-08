package in.edu.ashoka.lokdhaba;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
/**
 * 
 * @author sudx
 * This is a dummy class for testing the webpage
 */
public class TestNameData implements NameData {
	
	Queue<NamePair> pipeline;
	
	@Override
	public void initialize() {
		// DO NOTHING
		
	}

	

	@Override
	public void iterateNameData() {
		pipeline = new LinkedList<>();
		//create dummy data
		pipeline.add(new TestNamePair("arihant", "arrihant"));
		pipeline.add(new TestNamePair("rakesh", "rakhesh"));
		pipeline.add(new TestNamePair("rajaram", "rajuram"));
		pipeline.add(new TestNamePair("rena", "raina"));
	}

	@Override
	public void setSame(NamePair np) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void purgeNamePair(NamePair np) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public Iterator iterator(){
    	return new NameDataIterator(pipeline);
    }
	

}
