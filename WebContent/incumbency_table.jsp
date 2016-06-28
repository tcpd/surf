<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*"
    import="in.edu.ashoka.lokdhaba.SurfExcel"  
    import="in.edu.ashoka.lokdhaba.Dataset"
    import="in.edu.ashoka.lokdhaba.Row"
    import="in.edu.ashoka.lokdhaba.MergeManager"
    import="in.edu.ashoka.lokdhaba.JspMergeManager"
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
MergeManager mergeManager;
//Multimap<String, Row> resultMap;
//HashMap<Row, String> rowToId;
//HashMap<String, Row> idToRow;

%>



<%!
public void jspInit() {
	String file = "/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv";
	
	try {
		d = new Dataset(file);
		mergeManager = new JspMergeManager(d);
		mergeManager.initializeIds();
	    mergeManager.performInitialMapping();
	    mergeManager.addSimilarCandidates();
	    
	    /* SurfExcel.assignUnassignedIds(d.getRows(), "ID");
	    rowToId = new HashMap<Row, String>();
	    idToRow = new HashMap<String, Row>();
	    Bihar.generateInitialIDMapper(d.getRows(),rowToId,idToRow);
	    resultMap = Bihar.getExactSamePairs(d.getRows(),d); */
	}catch(IOException ioex){
		ioex.printStackTrace();
	}
	
    
    //writer.println("let's see where we are");
	
}
%>

<%

	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();
	
	
	if(request.getParameter("submit")!=null && request.getParameter("submit").equals("Save")) {
		//String checkedRows = request.getParameter("row");
		//System.out.println(checkedRows);
		String [] userRows = request.getParameterValues("row");
		if(userRows.length>0){
			mergeManager.merge(userRows);
			mergeManager.updateMappedIds();
		}
		
	}
	mergeManager.save();
	
	
    

    %>
    <h4>Check for Incumbent here</h4>
    <div style="display:table; margin:0 auto; padding:10px; border:1px solid black; border-radius:3px;">
    <form method="post">
    <table style="border: 3px solid #000000; border-collapse: collapse; padding:10px">
    <tr style="border-bottom: 3px solid black;">
    	<th>Is same</th>
    	<th>Candidate</th>
    </tr>
    
    


<%
	ArrayList<Multimap<String, Row>> incumbentsList = mergeManager.getIncumbents();
	boolean newGroup=false, newPerson=false;
	for(Multimap<String, Row> incumbentsGroup:incumbentsList){
		newGroup=true;
		for(String key:incumbentsGroup.keySet()){
			newPerson=true;
			for(Row row:incumbentsGroup.get(key)){
				String tableData;
				String rowStyleData;
				if(mergeManager.isMappedToAnother(row.get("ID"))){
					tableData = "<mapped>";
				} else {
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("ID")+"\"/>";
				}
				if(newGroup==true){
					newGroup=false;
					newPerson=false;
					rowStyleData = "style=\"border-top: 2px solid black;\"";
				}else if(newPerson==true){
						newPerson=false;
						rowStyleData = "style=\"border-top: 1px solid blue;\"";
					}else{rowStyleData = "";}
				
				
				%>
				<tr <%=rowStyleData %>>
					<td><%=tableData %></td>
					<td>
					<%=row%>
					</td>
				</tr>
				
				<%
			}
		}
	}

%>
	
	</table>
	<p style="margin-bottom:0;">
	<div style="position:fixed; top:.5in; right:.5in;">
	<input type="submit" name="submit" value="Save" style="width:200px;"/>
	</div>
	</p>
	
	</form>
	</div>

</body>
</html>