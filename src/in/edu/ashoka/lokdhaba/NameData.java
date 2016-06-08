package in.edu.ashoka.lokdhaba;

import java.util.Iterator;

/**
 * Created by sudx on 6/6/16.
 */
public interface NameData {
	public void initialize();	//scanning the file; generating common names
    //public NamePair getNamePair();	//returns a name pair object
    public void iterateNameData();	//generate name pairs object based on common names
    public void setSame(NamePair np);	//sets the identifier for the names as same for the provided name pair
    public void purgeNamePair(NamePair np);	//remove the name pair from listing; when established that name are different
    public Iterator iterator();
}

