package in.edu.ashoka.lokdhaba;

/**
 * Created by sudx on 6/6/16.
 */
public class NamePair {

    private Row name1,name2;
    public NamePair(Row name1, Row name2) {
        this.name1 = name1;
        this.name2 = name2;
    }

    public Row getName1(){return name1;}
    public Row getName2(){return name2;}
    public String getName1String(){return name1.get("Candidate_name");}
    public String getName2String(){return name2.get("Candidate_name");}
    
}
