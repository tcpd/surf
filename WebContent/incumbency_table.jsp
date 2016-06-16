<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="in.edu.ashoka.lokdhaba.SurfExcel"  
    import="in.edu.ashoka.lokdhaba.Dataset"
    import="in.edu.ashoka.lokdhaba.Row"
    import="java.util.*"
    import="in.edu.ashoka.lokdhaba.Bihar"
    import="com.google.common.collect.Multimap"
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Incumbency Checker</title>
</head>
<body>

<%

	
	String file = "/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv";
	
    Dataset d = new Dataset(file);
    
    Multimap<String, Row> resultMap = Bihar.getExactSamePairs(d.getRows(),d);

    %>
    <h4>Check for Incumbent here</h4>
    <form>
    <table>
    <tr>
    	<th>Candidate</th>
    	<th>Is same?</th>
    </tr>
    
    
    <% 
    int i =0;
    //List<Row> rowsTobeDisplayed = new ArrayList<Row>();
    for (String canonicalVal: resultMap.keySet()) {
         Collection<Row> idsForThisCVal = resultMap.get(canonicalVal);
         
         // UI should allow for merging between any 2 of these ids.
         for (Row row: idsForThisCVal) {
        	 
        	 
        	 
        		 %>
        		 
            	 <tr>
            	 <td>
            	 <%=row.toString() %>
            	 </td>
            	 <td><input type="checkbox" id="<%=row.get("ID")%>"></td>
            	 </tr>
            	 
            	 <%
        	 }
             // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
        	 
             
             
             
         }


%>
	<input type="submit" name="save"/>
	</form>
	</table>

</body>
</html>