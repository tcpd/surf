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
	<script src="https://code.jquery.com/jquery-3.1.0.min.js"   integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s="   crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
	<script>


//SCRIPTS TO HANDLE COMMENTS

//function to strip alphabet from id
function stripId(commentId){
	var str = "";
	for(var i=0;i<commentId.length;i++){
		if(commentId[i]>='0' && commentId[i]<='9')
			str+=commentId[i];
	}
	return str;
}

//script function to handle comments
function commentHandler(commentId){
	var commentNode = document.getElementById(commentId);
	var id = stripId(commentId)
	
	var child = commentNode.childNodes[0];
	if(child.tagName=="input"){
		child.focus();
		return;
	}
	
	var text = commentNode.innerText;
	if(text==null)
		text="";
	var inputNode = document.createElement("input");
	inputNode.setAttribute("name", "commentParam"+id);
	inputNode.setAttribute("id","input"+commentId);
	inputNode.setAttribute("value",text);
	inputNode.setAttribute("onclick","");
	commentNode.replaceChild(inputNode, commentNode.childNodes[0]);

	var node = commentNode.childNodes[0];
	node.focus();
	
	
	
	
}
</script>
<title>Incumbency Checker</title>
</head>
<body>

	<%!
   //SETTING UP RREQUIRED VARIABLES

	boolean isFirst;
	Dataset d;
	MergeManager mergeManager;

	

    //add other csv here or eventually take the file input from user
    //static final String ge="/Users/Kshitij/Documents/CS/Incumbency Project/lokdhaba/GE/candidates/csv/candidates_info_updated.csv";
	//static final String bihar="";
	//static final String rajasthan="";
	%>



	<%!
	public void jspInit() {
	isFirst=true;
       //writer.println("let's see where we are");

   }
   %>
   
   <!--
   
   //setUpParameter sets up a parameter which can be used across same page reloads and different pages.
	
   final String setUpParameter(String param){
	   if(request.getParameter(param)!= null){
			param = request.getParameter(param).toString();
			session.setAttribute(param, param);
			return param;
		}
	   
	   else{
			param = session.getAttribute(param);
			return param;
		}   
    }
   -->

   
   <%
   
	//SETTING UP THE 4 VARIABLES
	
	String userName, email, algorithm, dataset, filterParam, filterValue;
   
		if(request.getParameter("userName")!= null){
			userName = request.getParameter("userName").toString();
			session.setAttribute("userName", userName);	
				
		}
		   
		else{
			userName = session.getAttribute("userName").toString();
		}
		
		if(request.getParameter("email")!= null){
			email = request.getParameter("email").toString();
			session.setAttribute("email", email);	
				
		}
		   
		else{
			email = session.getAttribute("email").toString();
		}
	
	   if(request.getParameter("algorithm")!= null){
			algorithm = request.getParameter("algorithm").toString();
			session.setAttribute("algorithm", algorithm);	
			
	   }
	   
	   else{
			algorithm = session.getAttribute("algorithm").toString();
		}
	   
	   if(request.getParameter("dataset")!= null){
			dataset = request.getParameter("dataset").toString();
			session.setAttribute("dataset", dataset);	
			
	   }
	   
	   else{
			dataset = session.getAttribute("dataset").toString();
		}
	   
	   if(request.getParameter("filterParam")!= null){
			filterParam = request.getParameter("filterParam").toString();
			session.setAttribute("filterParam", filterParam);	
		}
	   
	   else{
			filterParam = session.getAttribute("filterParam").toString();
		}
	   
	   if(request.getParameter("filterValue")!= null){
			filterValue = request.getParameter("filterValue").toString();
			session.setAttribute("filterValue", filterValue);	
		}
	   
	   else{
			filterValue = session.getAttribute("filterValue").toString();
		}
   
	   
	//SETTING UP THE DATASET FOR MERGEMANAGER
	
	//paths go here
    String ge = pageContext.getServletConfig().getInitParameter("gePath").toString();
   
	if(isFirst){
		String file="";
		if(request.getParameter("dataset").equals("ge")){
			file = ge;
		}
	
		try {
			d = new Dataset(file);
			Bihar.initRowFormat(d.getRows(), d);
		
		}
		
		catch(IOException ioex){
			ioex.printStackTrace();
		}
		
		isFirst = false;
	}


	//SETs UP mergeManager
	mergeManager = MergeManager.getManager(algorithm, d);

	
	//Initial Mapping by mergeManager
	if(mergeManager.isFirstReading()){
		mergeManager.initializeIds();
		mergeManager.performInitialMapping();
	}
	else{
	    mergeManager.load();
	}
	
	mergeManager.addSimilarCandidates();


	//response.setContentType("text/html");
	//PrintWriter writer = response.getWriter();

	ArrayList<Multimap<String, Row>> incumbentsList;
	
	//SAVES MERGES AND COMMENTS 
	
	if(request.getParameter("submit")!=null && request.getParameter("submit").equals("Save")) {
			//String checkedRows = request.getParameter("row");
			//System.out.println(checkedRows);
	
			String [] userRows = request.getParameterValues("row");
			
			//Collect comment related information
			
			Map<String,String[]> parameterMap = request.getParameterMap();
			
			Map<String,String> map = new HashMap<String,String>();
			for(String name:parameterMap.keySet()){
			if(name.contains("commentParam")){
					map.put(name.substring(12),parameterMap.get(name)[0]);	//strip the key value before storing
				}
				
			}
			System.out.println(map);
			boolean shouldSave = false;	//change to true in your code block if you want to update the csv
			if(userRows!=null && userRows.length>0){
			mergeManager.merge(userRows);
			mergeManager.updateMappedIds();
			shouldSave = true;
		}
		if(!map.isEmpty()){
		mergeManager.updateComments(map);
		shouldSave = true;
		}
		
		if(shouldSave)
		mergeManager.save(ge);
		}
	
		//WORKING WITH FILTER PARAMETERS
		
			if(filterParam != null){
			if(filterValue != null){
			if(filterValue.equals("All Records")){
			incumbentsList = mergeManager.getIncumbents();
		}
		else{
			incumbentsList = mergeManager.getIncumbents(request.getParameter("filterParam"), request.getParameter("filterValue"));
		}   
		}
		else{
			incumbentsList = mergeManager.getIncumbents();
		}
		}
		else{
			incumbentsList = mergeManager.getIncumbents(); //Default
		}
					
		int[] progressData = mergeManager.getListCount(incumbentsList);	

	%>
	

			<div class="container filterForm">
				<form class="form-inline" role="filter" method="get" action="incumbency_table.jsp" onsubmit="incumbency_table.jsp">
					<div class="form-group">
						<select class="form-control" id="filterParam" name="filterParam" onchange="populateDropdown()">
							<option value="State" id="Primary">State</option>
							<option value="Party" >Party</option>
							<option value="PC_name" >Constituencies</option>
						</select>
						<select class="form-control" id="filterValue" name="filterValue">
							<option value="All Records">All Records</option>
						</select>
						<button type="submit" class="btn btn-default">Filter</button>
					</div>
				</form>
			</div>

			<form method="post">
				<nav class="navbar navbar-default navbar-fixed-top">
					<div class="container-fluid">
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
						<input type="submit" class="btn btn-default navbar-btn navbar-right" name="submit" value="Save" id="saveButton"/>
						<div class="collapse navbar-collapse navbar-right" id="bs-example-navbar-collapse-1">
							<ul class="nav navbar-nav">
								<li><div class="navbar-text"><%= progressData[0] %> Total Records</div></li>
								<li><div class="navbar-text"><%= progressData[2] %> Records Mapped</div></li>
								<li><div class="navbar-text"><%= userName%></div></li>
								<li><div class="navbar-text"><%= email%></div></li>
								<!--<li><div class="navbar-text" id="test">Howdy</div></li>-->
							</ul>
						</div>
						<!-- /.navbar-collapse -->
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
								<th>Comments</th>
							</tr>
<%
							
    //MAKES THE CSS FOR DISPLAYING RECORDS AS GROUPS
							
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
				} 
				else {
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("ID")+"\"/>";
				}
				if(newGroup==true){
					newGroup=false;
					newPerson=false;
					rowStyleData = "class=\"table-row-new-person trow\"";
				}
				else if(newPerson==true){
					newPerson=false;
					rowStyleData = "class=\"table-row-same-person trow\"";
				}
				else{rowStyleData = "";}
			
			
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
				<td class="cell-table" id="comment-<%=row.get("ID")%>" onclick="commentHandler('comment-<%=row.get("ID")%>')">
					<%-- <div id=comment-<%=row.get("ID")%> onclick="commentHandler('comment-<%=row.get("ID")%>')"> --%>
					<%=row.get("comments")%>
					<!-- </div> -->
				</td>
			</tr>
			
			<%
		}
	}
}



%>


</tbody>
</table>
</form>

<script type="text/javascript">

//CONSTRUCTS MAPS FROM FILTER PARAMETERS TO FILTER VALUES

<% String[] filterParams = {"State", "PC_name", "Party"}; //Enter new parameters here

Map<String,Set<String>> filterData = mergeManager.getAttributesDataSet(filterParams);%>

var filterDataValues = {};
<% for(int i = 0; i<filterParams.length; i++){ %>
	filterDataValues["<%=filterParams[i]%>"] = new Array();
<%}%>

<%
for(String key : filterData.keySet()){
	for(String value:filterData.get(key)){
		%>
		filterDataValues["<%=key%>"].push("<%=value%>");
		<%
	}
	
}
%>


var values = new Array();

//SETS DEFAULT FOR FILTERVALUE

var filterValue = document.getElementById("filterValue");
values = filterDataValues["State"]; //Default
values.sort();
for(var i = 0; i < values.length; i++) {
	var opt = values[i];
	var el = document.createElement("option");
	el.textContent = opt;
	el.value = opt;
	filterValue.appendChild(el);
};

//POPULATES DROPDOWN FOR FILTER VALUES

function populateDropdown() {
	var filterParamValue = document.getElementById("filterParam").value;
	while(filterValue.hasChildNodes()){
		filterValue.removeChild(filterValue.firstChild);
	}
	var allRecords = document.createElement("option");
	allRecords.textContent = "All Records";
	allRecords.value = "All Records";
	filterValue.appendChild(allRecords);
	values = filterDataValues[filterParamValue];
	values.sort();
	for(var i = 0; i < values.length; i++) {
		var opt = values[i];
		var el = document.createElement("option");
		el.textContent = opt;
		el.value = opt;
		filterValue.appendChild(el);
	};

}


//TESTING SCRIPT 

$("#test").on("click", function(){

});
</script>

<!-- SCRIPT FOR HIGHLIGHTING AND CHECKING ROWS -->
<script type = "text/javascript">
$("document").ready(function(){
	$("tr td:not(:last-child)").on("click", function(){
		$(this).parent().toggleClass("success");
		var checkboxValue = $(this).parent().find("td:first-child input[type]").prop("checked");
		$(this).parent().find("td:first-child input[type]").prop("checked", !checkboxValue);
	});
});

</script>
</body>
</html>