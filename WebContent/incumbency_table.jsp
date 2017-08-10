<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
import="in.edu.ashoka.surf.Row"
import="in.edu.ashoka.surf.MergeManager"
import="in.edu.ashoka.surf.Row"
import="in.edu.ashoka.surf.MergeManager"
import="java.util.*"
import="com.google.common.collect.Multimap"
%>
<%@ page import="edu.stanford.muse.util.Util" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" type="text/css" href="style.css">
    <script src="https://code.jquery.com/jquery-3.1.0.min.js"   integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s="   crossorigin="anonymous"></script>
    <script src="https://code.jquery.com/ui/1.12.0/jquery-ui.min.js"   integrity="sha256-eGE6blurk5sHj+rmkfsGYeKyZx3M4bG+ZlFyA7Kns7E="   crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
<!--  	 <link href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css" type="text/css" rel="stylesheet" /> -->
<!-- 	<script src="//code.jquery.com/jquery-1.11.3.min.js" type="text/javascript"></script> -->
<!-- 	<script src="//code.jquery.com/ui/1.11.4/jquery-ui.min.js" type="text/javascript"></script> -->
<!-- 	<script src="https://cdn.jsdelivr.net/jquery.ui-contextmenu/1.12.0/jquery.ui-contextmenu.min.js" type="text/javascript"></script> -->
	<script src="helper.js" type="text/javascript"></script>

<title>Surf</title>
</head>
<body>

   
<div id="loading" style="padding-top: 20%">
	<img id="loading-image" src="loading.gif" alt="LOADING.."/>
</div>

   <%
   
	   	String userName, email, algorithm, dataset, filterParam, filterParamNav, filterValueNav;
	  	String [] filterValue;
	  	
	  	userName = session.getAttribute("userName").toString();
	   	email = session.getAttribute("email").toString();
	   	filterParam = session.getAttribute("filterParam").toString();
	   	filterValue = (String [])session.getAttribute("filterValue");
	   	filterParamNav = session.getAttribute("filterParamNav").toString();
	   	filterValueNav = session.getAttribute("filterValueNav").toString();

		ArrayList<Multimap<String, Row>> incumbentsList = (ArrayList<Multimap<String, Row>>)session.getAttribute("subList");			
		int[] progressData = (int[])session.getAttribute("progressData");	
		MergeManager mergeManager = (MergeManager)session.getAttribute("mergeManager");

	%>
	


	<!-- Modal -->
	<div class="modal fade" id="filterModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="myModalLabel">Settings Menu</h4>
	      </div>
	      <div class="modal-body">
	       	<div class="filterForm">
				<form class="form" role="filter" method="get" action="${pageContext.request.contextPath}/IncumbencyServlet" onsubmit="${pageContext.request.contextPath}/IncumbencyServlet">
					<div class="form-group">
						Algorithm:
						<select class="form-control" name="algorithm" id="algorithm">
								<option value="exactSameName">Exact Same Name</option>
								<option value="exactSameNameWithConstituency">Exact Same Name with Constituency</option>
								<option value="editDistance1">Approximate Name with Edit Distance 1</option>
								<option value="editDistance2">Approximate Name with Edit Distance 2</option>
								<option value="compatibleNames">Compatible names in same constituency</option>
								<option value="search">Search</option>
								<option value="dummyAllName">All names</option>
						</select>
					</div>
					<div class="form-group">
						Arguments for Algorithm:
						<input type="text" class="form-control" id="algo-arg" name="algo-arg">
					</div>
					<div class="form-group">
						Filter:
						<select class="form-control" id="filterParam" name="filterParam" onchange="populateDropdown()">
							<option value="State" id="Primary">State</option>
							<option value="Party" >Party</option>
							<option value="PC_name" >Constituencies</option>
						</select>
					</div>
					<div class="form-group">
						<select multiple class="form-control" id="filterValue" name="filterValue">
							<option value="All Records">All Records</option>
						</select>
					</div>
					<div class=form-group>
						Choose only Winners:
						<select id="onlyWinners" class="form-control" name="onlyWinners">
							<option value="false">No</option>
							<option value="true">Yes</option>
						</select>
					</div>
					<div class=form-group>
						Sort Order:
						<select id="comparatorType" class="form-control" name="comparatorType">
							<option value="confidence">By Confidence</option>
							<option value="alphabetical">Alphabetical</option>
						</select>
					</div>
				<div class="modal-footer">
					<button type="submit" onclick="saveFilterSettings()" id="settingsSubmit" class="btn btn-default" style="margin:0 auto; display:table;">Submit</button>
	      		</div>
   		</form>
    </div>
  </div>
</div>
</div>
</div>
<div>
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
					<span class="logo" style="font-size:30px">Surf</span>
				</div>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<input type="submit" class="btn btn-default navbar-btn navbar-right" name="submit" value="Save" id="saveButton" onclick="$('#loading').fadeIn()"/>
				<input type="submit" style="margin-right:0.9em; height:35px;" class="btn btn-default navbar-btn navbar-right" name="submit" value="Force Merge" id="forceMergeButton"/>
				<button type="button" onclick="loadFilterSettings()" style="margin-right:0.9em; height:35px;"class= "btn btn-default navbar-btn navbar-right" data-toggle="modal" data-target="#filterModal">
						<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
						Settings
				</button>
				<input type="submit" style="margin-right:0.9em; height:35px;" class="btn btn-default navbar-btn navbar-right" name="submit" value="Reset" id="resetButton" onclick="return resetButtonPressed()"/>
				<!adding search bar>
				<div class="col-sm-3 col-md-3 pull-right">
						<div class="input-group">
							<input type="text" style="margin-right:0em; cursor:auto; height:35px;" class="form-control btn btn-default navbar-btn navbar-right" placeholder="Search Name..." name="searchValue" id="searchValue">
							<span class="input-group-btn">
								<button style="margin-right:0.9em; height:35px;" class="btn btn-default navbar-btn navbar-right" type="button" id="searchButton"><i class="glyphicon glyphicon-search"></i></button>
							</span>
						</div>
				</div>
				<div class="collapse navbar-collapse navbar-right" id="bs-example-navbar-collapse-1">
					<ul class="nav navbar-nav">
						<ol class="breadcrumb">
						  <li><a data-toggle="modal" data-target="#filterModal"><%= filterParamNav %></a></li>
						  <li><a data-toggle="modal" data-target="#filterModal"><%= filterValueNav %></a></li>
						</ol>
						<li><div class="navbar-text"><%= progressData[3] %> Groups</div></li>
						<li><div class="navbar-text"><%= progressData[1] %> Records</div></li>
						<li><div class="navbar-text"><%= progressData[4] %> Records Reviewed</div></li>
						<li><div class="navbar-text"><%= userName%></div></li>
<!-- 						<li><div class="navbar-text" id="test">Howdy</div></li> -->
					</ul>
				</div>
				<div style="width: 100%; height: 100%">
				<table class="nav nav-pills nav-stacked table-header">
					<thead>
					<tr class="table-row">
						<th class="cell-table table-cell-merge">Merge</th>
						<th class="cell-table table-cell-name">Name</th>
						<th class="cell-table table-cell-sex">Sex</th>
						<th class="cell-table table-cell-year">Year</th>
						<th class="cell-table table-cell-constituency">Constituency</th>
						<th class="cell-table table-cell-party">Party</th>
						<th class="cell-table table-cell-state">State</th>
						<th class="cell-table table-cell-position">Position</th>
						<th class="cell-table table-cell-votes">Votes</th>
						<!-- <th class="cell-table">ID</th>
                        <th>Person ID</th> -->
						<th class="cell-table table-cell-comments">Comments</th>
						<th class="cell-table table-cell-unmerge">UnMerge</th>
						<th class="cell-table table-cell-done">Done</th>
					</tr>
					</thead>
				</table>
				</div>
				<!-- /.navbar-collapse -->
			</div><!-- /.container-fluid -->
		</nav>


		<div>
			<div class="table-div table-responsive" id="table-container">
				<table class="table">
					<tbody class="inside-table" id="table-body">
<%
							
    //MAKES THE CSS FOR DISPLAYING RECORDS AS GROUPS
							
	boolean newGroup=false, newPerson=false;
	int gid = 0;
	for(Multimap<String, Row> incumbentsGroup:incumbentsList){
		newGroup=true;
		//TRYING TO SORT data based on constituency and then year
		final Multimap<String, Row> incumbentsGroupFinal = incumbentsGroup;
		List<String> keyList = new ArrayList<String>(incumbentsGroup.keySet());
		if(((String)session.getAttribute("algorithm")).equals("search")){
		    //Do nothing
		}else{
			keyList.sort(new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					int result = incumbentsGroupFinal.get(s1).iterator().next().get("PC_name").toLowerCase().compareTo(
							incumbentsGroupFinal.get(s2).iterator().next().get("PC_name").toLowerCase());
					return result==0 ? (incumbentsGroupFinal.get(s1).iterator().next().get("Year").toLowerCase().compareTo(
							incumbentsGroupFinal.get(s2).iterator().next().get("Year").toLowerCase())):result;
				}
			});
		}


		//TILL HERE
		for(String key:keyList){
			newPerson=true;
			for(Row row:incumbentsGroup.get(key)){
				String tableData = "";
				String rowStyleData;
				String unMerge;
				String rowCompletionColor;
				boolean isChildPerson = false;
				if(mergeManager.isMappedToAnother(row.get("ID"))){
					tableData = "<mapped dummy tag>";
					isChildPerson = true;
				} 
				/*else {
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("ID")+"\"/>";
					pageContext.setAttribute("tableData","");	//same as above; used ny jstl
				}*/
				
				if(incumbentsGroup.get(key).size()>1){
					unMerge = "<input type=\"checkbox\" class=\"checkBox\" name=\"demerges\" value=\""+row.get("ID")+"\"/>";
				}else{
					unMerge = "";
				}
				
				if(row.get("is_done").toString().equals("yes")){
					rowCompletionColor = "row-done";
				}else{
					rowCompletionColor = "row-not-done";
				}
				
				if(newGroup==true){
					gid++;
					String groupId = "name=\"g"+gid+"\"";
					String groupValue = "g"+gid;
					newGroup=false;
					newPerson=false;
					rowStyleData = "class=\"table-row-new-person trow\"";
					pageContext.setAttribute("groupId",groupId);
					pageContext.setAttribute("groupValue",groupValue);
					//IF NEW GROUP, CREATE A HEADER FOR THE ROWS
					%>
						<tr <%=rowStyleData %>>
							<td colspan="7">
								<button type="button" ${groupId} id="merge-all" onclick="selectAllRowsInGroupForMerge('${groupValue}')" >Select and merge all</button>
							</td>
							<td colspan="5">
								<button style="float:right;" type="button" ${groupId} id="done-all" onclick="selectAllRowsInGroupForDone('${groupValue}')">Mark as Done</button>
								<button style="float: right; margin-right: 10px" type="button" ${groupId} id="done-all-uptill" onclick="selectUpTillHereForDone('${groupValue}')">Merge all groups above</button>
							</td>
						</tr>
					<%
					rowStyleData = "class=\"table-row-same-person trow " + rowCompletionColor + " \"";
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("mapped_ID")+"\"/>";
					isChildPerson = false;
				}
				else if(newPerson==true){
					newPerson=false;
					rowStyleData = "class=\"table-row-same-person trow "+rowCompletionColor+" \"";
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("mapped_ID")+"\"/>";
					isChildPerson = false;
				}
				else{rowStyleData = "class=\""+rowCompletionColor+" \""; isChildPerson=true;}
				pageContext.setAttribute("isChildPerson",isChildPerson);	//needed for jstl later on
				String hoverText = "ID: " + row.get("ID") + " Person ID: " + row.get("mapped_ID");
				hoverText += " Canonical: " + Util.escapeHTML(row.get ("cname"));
				hoverText += " Tokenized: " + Util.escapeHTML(row.get ("tname"));
				hoverText += " Sorted: " + Util.escapeHTML(row.get ("stname"));
				String pc_num = "Constituency number: " + row.get("AC_no") + " Subregion: " + row.get("subregion");
			%>

			<tr <%=rowStyleData %> ${groupId} title="<%=hoverText%>" id=<%=row.get("ID")%> data-personid="<%=row.get("mapped_ID")%>">
				<td class="cell-table mergeCol table-cell-merge"><%=tableData %></td>
				<td class="cell-table table-cell-name">
					<a href="http://www.google.com/search?q=<%=row.get("Name").replace(" ","+")+"+"+row.get("PC_name").replace(" ","+")+"+"+row.get("Year")%>" target="_blank">
						<%=row.get("Name")%>
					</a>
				</td>
				<td class="cell-table table-cell-sex">
					<%=Util.escapeHTML(row.get("Sex"))%>
				</td>
				<td class="cell-table table-cell-year">
					<%=Util.escapeHTML(row.get("Year"))%>
				</td>
				<td class="cell-table table-cell-constituency">
					<a href="https://www.google.co.in/maps/place/<%=row.get("PC_name").replace(" ","+")+","+row.get("State").replace("_","+")%>" target="_blank">
						<span title="<%=pc_num%>"%><%=Util.escapeHTML(row.get("PC_name"))%></span>
					</a>
				</td>
				<td class="cell-table table-cell-party">
					<%=Util.escapeHTML(row.get("Party"))%>
				</td>
				<td class="cell-table table-cell-state">
					<%=Util.escapeHTML(row.get("State"))%>
				</td>
				<td class="cell-table table-cell-position">
					<%=Util.escapeHTML(row.get("Position"))%>
				</td>
				<td class="cell-table table-cell-votes">
					<%=Util.escapeHTML(row.get("Votes1"))%>
				</td>
				
				<%-- <td class="cell-table">
					<%=row.get("ID")%>
				</td>
				<td class="cell-table">
					<%=row.get("mapped_ID")%>
				</td> --%>
				
				
				
				<%-- <c:set var="tableData" scope="page" value="lolo"></c:set> --%>
				<c:choose>
					<c:when test="${isChildPerson eq true }">
						<td class="cell-table table-cell-comments" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=row.get("ID")%>')">
						</td>
						<td class="cell-table unmerge-col table-cell-unmerge"><%=unMerge%></td>
						<td class="cell-table table-cell-done"></td>
					</c:when>
					<c:otherwise>
						<td class="cell-table table-cell-comments" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=row.get("ID")%>')">
							<div class="comment-box"><div style="padding:0.3em"><%=row.get("comments")%></div></div>
						</td>
						<td class="cell-table unmerge-col table-cell-unmerge"><%=unMerge%></td>
						<c:out value="${tableData}"></c:out>
						<td class="cell-table table-cell-done">
							<%
								String selected = row.get("is_done");
								String selectedHTML;
								if(selected==null||selected.equals("no")){
									selectedHTML="";
								}
								else if(selected.equals("yes")){
									selectedHTML="checked=\"checked\"";
								}
								else{
									selectedHTML="";
								}
							%>
							<input type="checkbox" id="isDone-<%=row.get("ID")%>" onclick="createNameParameter('<%=row.get("ID")%>')" <%=selectedHTML%>>
						</input>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
			
			<%
		}
	}
}



%>

</tbody>
</table>
</div>
</div>
</form>

<div id="page-block">
	
	<!-- Page Navigation bar here -->
	<nav aria-label="Page navigation">
  	<ul class="pagination pre-margin">
  	
  	<!-- Listing previous page url here-->
  	<c:if test="${currentPage != 1}">
	    <li class="page-item" onclick="resetScroll(); $('#loading').fadeIn();">
		<a class="page-link" href="IncumbencyServlet?page=${currentPage - 1}" aria-label="Previous">
		<span aria-hidden="true">&laquo;</span>
		<span class="sr-only">Previous</span>
	 	</a>
	    </li>
    </c:if>
    
    <!-- Listing page numbers here -->
    <c:forEach begin="1" end="${noOfPages}" var="i">
				<c:choose>
					<c:when test="${currentPage eq i}">
						<c:set var="pageIsActive" value="page-item active"></c:set>
					</c:when>
					<c:otherwise>
						<c:set var="pageIsActive" value="page-item"></c:set>
					</c:otherwise>
				</c:choose>
				<li class="${pageIsActive}" onclick="resetScroll(); $('#loading').fadeIn();">
					<a class="page-link" href="IncumbencyServlet?page=${i}">${i}</a>
				</li>
	</c:forEach>
    
    <!-- Listing next page url here -->
    
    <c:if test="${currentPage lt noOfPages}">
				<li class="page-item" onclick="resetScroll(); $('#loading').fadeIn()">
      			<a class="page-link" href="IncumbencyServlet?page=${currentPage + 1}" aria-label="Next">
        		<span aria-hidden="true">&raquo;</span>
        		<span class="sr-only">Next</span>
      			</a>
    			</li>
	</c:if>
    
    
  </ul>
</nav>

</div>
</div>

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

</script>

<script type="application/javascript">
//CREATE VARIABLES TO BE USED AS LOADING VARIABLES
var filterVariables = new Array();
filterVariables[0]=('${algorithm}')
filterVariables[1]=null
filterVariables[2]=('${onlyWinners}')
filterVariables[3]=(<%=(session.getAttribute("algo-arg").equals(""))?"\'\'":"'"+(String)session.getAttribute("algo-arg")+"'"%>);
filterVariables[4]=('${comparatorType}')
filterVariables[5]='${filterParam}'

</script>

<!-- //SCRIPT FOR HOVER POP OVER
<script>
$(document).ready(function(){
    $('[data-toggle="popover"]').popover();   
});
</script> -->

</body>
</html>
