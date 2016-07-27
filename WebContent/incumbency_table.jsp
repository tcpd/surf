<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.*"
    import="in.edu.ashoka.lokdhaba.SurfExcel"  
    import="in.edu.ashoka.lokdhaba.Dataset"
    import="in.edu.ashoka.lokdhaba.Row"
    import="in.edu.ashoka.lokdhaba.MergeManager"
    import="java.util.*"
    import="in.edu.ashoka.lokdhaba.Bihar"
    import="com.google.common.collect.Multimap"
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="style.css">
<title>Incumbency Checker</title>
</head>
<body>

<%!
boolean isFirst;
Dataset d;
MergeManager mergeManager;

//add other csv here or eventually take the file input from user
static final String ge="/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info_updated.csv";
static final String bihar="";
static final String rajasthan="";

%>



<%!
public void jspInit() {
	isFirst=true;
    //writer.println("let's see where we are");
	
}
%>

<%

	if(isFirst){
		String file="";
		if(request.getParameter("dataset").equals("ge")){
			file = ge;
		}
		else if(request.getParameter("dataset").equals("bihar"))
			file = bihar;
		else if(request.getParameter("dataset").equals("rajasthan"))
			file = rajasthan;
		else {}
		
		try {
			d = new Dataset(file);
			Bihar.initRowFormat(d.getRows(), d);
		    
		    
		    /* SurfExcel.assignUnassignedIds(d.getRows(), "ID");
		    rowToId = new HashMap<Row, String>();
		    idToRow = new HashMap<String, Row>();
		    Bihar.generateInitialIDMapper(d.getRows(),rowToId,idToRow);
		    resultMap = Bihar.getExactSamePairs(d.getRows(),d); */
		}catch(IOException ioex){
			ioex.printStackTrace();
		}
		
	    isFirst = false;
	}
	
	//get merge manager
	mergeManager = MergeManager.getManager(request.getParameter("algorithm"), d);
	
	if(mergeManager.isFirstReading()){
		mergeManager.initializeIds();
	    mergeManager.performInitialMapping();
	}else{
		mergeManager.load();
	}
	mergeManager.addSimilarCandidates();
	

	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();
	
	
	if(request.getParameter("submit")!=null && request.getParameter("submit").equals("Save")) {
		//String checkedRows = request.getParameter("row");
		//System.out.println(checkedRows);
		String [] userRows = request.getParameterValues("row");
		if(userRows!=null && userRows.length>0){
			mergeManager.merge(userRows);
			mergeManager.updateMappedIds();
			mergeManager.save(ge);
		}
		
	}
	
	
	
    

    %>
    <h4>Check for Incumbent here</h4>
    <form method="post">
    <table class="actual-table">
    <tbody class="inside-table">
    <tr class="table-row">
    	<th class="cell-table">Is same</th>
    	<th class="cell-table">Name</th>
    	<th class="cell-table">Sex</th>
    	<th class="cell-table">Year</th>
    	<th class="cell-table">Constituency</th>
    	<th class="cell-table">Party</th>
    	<th class="cell-table">State</th>
    	<th class="cell-table">Position</th>
    	<th class="cell-table">Votes</th>
    	<th class="cell-table">ID</th>
    	<th>Mapped ID</th>
    </tr>
    
    


<%
	ArrayList<Multimap<String, Row>> incumbentsList = mergeManager.getIncumbents();
    		
		//checking stuff
		mergeManager.getListCount(incumbentsList);
    		
	boolean newGroup=false, newPerson=false;
	for(Multimap<String, Row> incumbentsGroup:incumbentsList){
		newGroup=true;
		for(String key:incumbentsGroup.keySet()){
			newPerson=true;
			for(Row row:incumbentsGroup.get(key)){
				String tableData;
				String rowStyleData;
				if(mergeManager.isMappedToAnother(row.get("ID"))){
					tableData = "<mapped dummy tag>";
				} else {
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("ID")+"\"/>";
				}
				if(newGroup==true){
					newGroup=false;
					newPerson=false;
					rowStyleData = "class=\"table-row-new-person\"";
				}else if(newPerson==true){
						newPerson=false;
						rowStyleData = "class=\"table-row-same-person\"";
					}else{rowStyleData = "";}
				
				
				%>
				<tr <%=rowStyleData %>>
					<td class="cell-table"><%=tableData %></td>
					<td class="cell-table">
					<%=row.get("Name")%>
					</td>
					<td class="cell-table">
					<%=row.get("Sex")%>
					</td>
					<td class="cell-table">
					<%=row.get("Year")%>
					</td>
					<td class="cell-table">
					<%=row.get("PC_name")%>
					</td>
					<td class="cell-table">
					<%=row.get("Party")%>
					</td>
					<td class="cell-table">
					<%=row.get("State")%>
					</td>
					<td class="cell-table">
					<%=row.get("Position")%>
					</td>
					<td class="cell-table">
					<%=row.get("Votes1")%>
					</td>
					<td class="cell-table">
					<%=row.get("ID")%>
					</td>
					<td class="cell-table">
					<%=row.get("mapped_ID")%>
					</td>
				</tr>
				
				<%
			}
		}
	}

%>
	</tbody>
	</table>
	<p style="margin-bottom:0;">
	<div class="button-float">
	<input type="submit" name="submit" value="Save" style="width:200px;"/>
	</div>
	
	
	</form>

</body>
</html>