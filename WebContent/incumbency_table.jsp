<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*"
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

<%!

Dataset d;
Multimap<String, Row> resultMap;
HashMap<Row, String> rowToId;
HashMap<String, Row> idToRow;
%>

<%!
public void sessionDidActivate(HttpSessionEvent arg0) {
	// TODO Auto-generated method stub
	
	
}
%>

<%!
public void jspInit() {
	String file = "/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv";
	
	try {
		d = new Dataset(file);
	    SurfExcel.assignUnassignedIds(d.getRows(), "ID");
	    rowToId = new HashMap<Row, String>();
	    idToRow = new HashMap<String, Row>();
	    Bihar.generateInitialIDMapper(d.getRows(),rowToId,idToRow);
	    resultMap = Bihar.getExactSamePairs(d.getRows(),d);
	}catch(IOException ioex){
		ioex.printStackTrace();
	}
	
    
    //writer.println("let's see where we are");
	
}
%>

<%

	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();
	
	
	if(request.getParameter("save")!=null && request.getParameter("save").equals("true")) {
		//String checkedRows = request.getParameter("row");
		//System.out.println(checkedRows);
		String [] userRows = request.getParameterValues("row");
		for(int i=0; i<userRows.length;i++)
			System.out.println(userRows[i]);
		if(userRows.length>0 && rowToId!=null && idToRow!=null){
			Bihar.merge(rowToId,idToRow,userRows);
		}
		
	}
	
	
    

    %>
    <h4>Check for Incumbent here</h4>
    <form method="post">
    <table>
    <tr>
    	<th>Candidate</th>
    	<th>Is same?</th>
    </tr>
    
    <!--  START OF A COMMENT HERE===================================================================
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
            	 <td><input type="checkbox" name="row" value="<%=row.get("ID")%>"></td>
            	 </tr>
            	 
            	 <%
        	 }
             // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
        	 
             
             
             
         }


%>
END OF THE COMMENT============================================================================-->

<%
	//---------TRYING NEW STUFF HERE
	HashSet<String> set = new LinkedHashSet<String>();
	    for (String canonicalVal: resultMap.keySet()) {
	         Collection<Row> idsForThisCVal = resultMap.get(canonicalVal);
	         
	         // UI should allow for merging between any 2 of these ids.
	         
	         for (Row row: idsForThisCVal) {
	        	 if(row.get("ID").equals(rowToId.get(row)))
	            	 set.add(rowToId.get(row));
	        	 }
	         
	             // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
	        }
	    
	    for(String id:set){
	       	 %>
	       	 <tr>
            	 <td>
            	 <%=idToRow.get(id) %>
            	 </td>
            	 <td><input type="checkbox" name="row" value="<%=id%>"></td>
           	 </tr>
            	 
            	 <%
	       	 
	        }
%>
	<input type="submit" name="save" value="true"/>
	</form>
	</table>

</body>
</html>