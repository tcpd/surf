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
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<link rel="stylesheet" type="text/css" href="style.css">
<title>Incumbency Checker</title>
</head>
<body>

<%!
boolean isFirst;
Dataset d;
MergeManager mergeManager;

//add other csv here or eventually take the file input from user
static final String ge="/Users/Kshitij/Documents/CS/Incumbency Project/lokdhaba/GE/candidates/csv/candidates_info_updated.csv";
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
		if(userRows.length>0){
			mergeManager.merge(userRows);
			mergeManager.updateMappedIds();
			mergeManager.save();
		}
		
	}
	
	
	
    

    %>
<form method="post">
    <nav class="navbar navbar-default navbar-fixed-top">
  <div class="containerir-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="#">Incumbency Checker</a>
    </div>
    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      <ul class="nav navbar-nav navbar-right"> 
      	<!--<li><p class="navbar-text">Group 5 of 20</p></li>-->
      	<div class="save-button"><li><input type="submit" name="submit" value="Save"/></li></div>
      </ul>
    </div><!-- /.navbar-collapse -->
  </div><!-- /.container-fluid -->
</nav>
    <div class="table-div">
    <table class="table table-hover">
    <tbody class="inside-table">
    <tr class="table-row">
    	<th class="cell-table">Same?</th>
    	<th class="cell-table">Name</th>
    	<th class="cell-table">Sex</th>
    	<th class="cell-table">Year</th>
    	<th class="cell-table">Constituency</th>
    	<th class="cell-table">Party</th>
    	<th class="cell-table">State</th>
    	<th class="cell-table">Position</th>
    	<th class="cell-table">Votes</th>
    	<th class="cell-table">ID</th>
    	<th>Person ID</th>
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
<!--</div>
	<p style="margin-bottom:0;">
	<div class="button-float">
	<input type="submit" name="submit" value="Save"/>
	</div>-->
</form>

</body>
</html>