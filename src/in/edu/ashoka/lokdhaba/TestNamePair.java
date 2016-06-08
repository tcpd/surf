package in.edu.ashoka.lokdhaba;

/**
 * 
 * @author sudx
 * This is a dummy class for testing the webpage
 */
public class TestNamePair extends NamePair {
	String str1, str2;
	public TestNamePair(Row name1, Row name2) {
		super(name1, name2);
		// TODO Auto-generated constructor stub
	}
	
	public TestNamePair(String name1,String name2) {
		super(null, null);
		this.str1 = name1;
		this.str2 = name2;
	}
	
	@Override
	public String getName1String(){
		return str1;
	}
	
	@Override
	public String getName2String(){
		return str2;
	}
	

}
