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
    <script src="https://code.jquery.com/ui/1.12.0/jquery-ui.min.js"   integrity="sha256-eGE6blurk5sHj+rmkfsGYeKyZx3M4bG+ZlFyA7Kns7E="   crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
<!--  	 <link href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css" type="text/css" rel="stylesheet" /> -->
<!-- 	<script src="//code.jquery.com/jquery-1.11.3.min.js" type="text/javascript"></script> -->
<!-- 	<script src="//code.jquery.com/ui/1.11.4/jquery-ui.min.js" type="text/javascript"></script> -->
<!-- 	<script src="https://cdn.jsdelivr.net/jquery.ui-contextmenu/1.12.0/jquery.ui-contextmenu.min.js" type="text/javascript"></script> -->
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
};

//script function to handle comments
function commentHandler(commentId){
    var commentNode = document.getElementById(commentId);
	var id = stripId(commentId);
	var child = commentNode.childNodes[0];
	var text = commentNode.childNodes[1].innerText;
	commentNode.removeChild(commentNode.childNodes[1]);
	//The following snippet doesn't work.
	//if(child == text){
	//	child.focus();
	//	return;
	//}

	if(text==null){
		commentNode.appendChild(child);
	}
	else{
		var inputNode = document.createElement("textarea");
		inputNode.setAttribute("name", "commentParam"+id);
		inputNode.setAttribute("id","input"+commentId);
		inputNode.setAttribute("value", text);
		inputNode.setAttribute("textContent", text);
		inputNode.setAttribute("class", "form-control");
		inputNode.setAttribute("rows", "3");
		inputNode.setAttribute("cols", "20");
		inputNode.setAttribute("wrap", "hard");
		inputNode.setAttribute("onclick","");
		inputNode.innerText = text;
	}
	

	commentNode.replaceChild(inputNode, commentNode.childNodes[0]);

	var node = commentNode.childNodes[0];
	node.focus();	
};

//script to display the full comment

// function commentDisplayer(commentId){
//     var commentNode = document.getElementById(commentId);
//     var text = commentNode.innerText;
//     if(text.length>1){
//     	var commentBox = document.getElementById(commentId);
//     	var showMoreButton = document.createElement("button");
//     	showMoreButton.innerText ="Hello";
//     	showMoreButton.className += "btn btn-default"
//     	commentBox.appendChild(showMoreButton);
// 		var test = document.getElementById("test");
// 		test.style.color = "red";
//     }
// };



// //SETS VALUES FOR DROPDOWNS IN MODALS ON THE BASIS OF PREVIOUS SUBMISSION

// function SelectElement(id, valueToSelect){
// 	var element = document.getElementById(id);
// 	element.value = valueToSelect;
// }

// $("document").ready(function(algorithm, dataset, filterPar, filterVal){
<%-- 	<% if(request.getParameter("algorithm") != null && request.getParameter("dataset") != null && request.getParameter("filterParam") != null --%>
// 			&& request.getParameter("filterValue") != null){
<%-- 			%> SelectElement("algorithm", <%=request.getParameter("algorithm")%>); --%>
<%-- 			SelectElement("dataset", <%=request.getParameter("dataset")%>); --%>
<%-- 			SelectElement("filterParam", <%=request.getParameter("filterParam")%>); --%>
<%-- 			SelectElement("filterValue", <%=request.getParameter("filterValue")%>); <% --%>
// 		}
<%-- 	%> }); --%>

cookieName="page_scroll"
	expdays=365

	// An adaptation of Dorcht's cookie functions.

	function setCookie(name, value, expires, path, domain, secure){
	    if (!expires){expires = new Date()}
	    document.cookie = name + "=" + escape(value) + 
	    ((expires == null) ? "" : "; expires=" + expires.toGMTString()) +
	    ((path == null) ? "" : "; path=" + path) +
	    ((domain == null) ? "" : "; domain=" + domain) +
	    ((secure == null) ? "" : "; secure")
	}

	function getCookie(name) {
	    var arg = name + "="
	    var alen = arg.length
	    var clen = document.cookie.length
	    var i = 0
	    while (i < clen) {
	        var j = i + alen
	        if (document.cookie.substring(i, j) == arg){
	            return getCookieVal(j)
	        }
	        i = document.cookie.indexOf(" ", i) + 1
	        if (i == 0) break;
	    }
	    return null
	}

	function getCookieVal(offset){
	    var endstr = document.cookie.indexOf (";", offset)
	    if (endstr == -1)
	    endstr = document.cookie.length
	    return unescape(document.cookie.substring(offset, endstr))
	}

	function deleteCookie(name,path,domain){
	    document.cookie = name + "=" +
	    ((path == null) ? "" : "; path=" + path) +
	    ((domain == null) ? "" : "; domain=" + domain) +
	    "; expires=Thu, 01-Jan-00 00:00:01 GMT"
	}

	function saveScroll(){ // added function
	    var expdate = new Date ()
	    expdate.setTime (expdate.getTime() + (expdays*24*60*60*1000)); // expiry date

	    var x = (document.pageXOffset?document.pageXOffset:document.body.scrollLeft)
	    var y = (document.pageYOffset?document.pageYOffset:document.body.scrollTop)
	    Data=x + "_" + y
	    setCookie(cookieName,Data,expdate)
	}

	function loadScroll(){ // added function
	    inf=getCookie(cookieName)
	    if(!inf){return}
	    var ar = inf.split("_")
	    if(ar.length == 2){
	        window.scrollTo(parseInt(ar[0]), parseInt(ar[1]))
	    }
	}

</script>
<title>Candidate Mapper</title>
</head>
<body onload="loadScroll()" onunload="saveScroll()">

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
   
<div id="loading">
	<img id="loading-image" src="https://s-media-cache-ak0.pinimg.com/564x/a3/ed/70/a3ed7024b3aeda6ca80c2c820e7636c4.jpg" alt="LOADING.."/>
</div>

   <%
   
	//SETTING UP THE VARIABLES which can be used in the whole session
	
	String userName, email, algorithm, dataset, filterParam, filterValue, filterParamNav, filterValueNav;
   
   
	if(request.getParameter("userName")!= null){
		userName = request.getParameter("userName").toString();
		session.setAttribute("userName", userName);	
			
	}
	
	else if(session.getAttribute("userName") != null){
		userName = session.getAttribute("userName").toString();
	}
	else{
		userName = "User Unknown";
	}
   
	
	if(request.getParameter("email")!= null){
		email = request.getParameter("email").toString();
		session.setAttribute("email", email);	
			
	}  
	else if(session.getAttribute("email") != null){
		email = session.getAttribute("email").toString();
	}
	else{
		email = "Not Specified";
	}
   
   //Check if the user is entering for the first time. 
   
	String checkVar = "algorithm";
   
	if(session.getAttribute(checkVar) != null || request.getParameter(checkVar) != null){
	
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
	   
	   //HARDCODED STUFF HERE
	   	
	   if(filterParam.equals("PC_name")){
		   filterParamNav = "Constituency";
	   }
	   else{
		   filterParamNav = filterParam;
	   }
	   filterValueNav = Arrays.toString(request.getParameterValues("filterValue"));
	}
	
	else{
		   //SET DEFAULTS FOR THE VARIABLE
		   
		   algorithm = "exactSameName";
		   dataset = "ge";
		   filterParam = "State";
		   filterValue = "All Records";
		   filterParamNav = filterParam;
		   filterValueNav = filterValue;
			if(request.getParameter("state") != null){
				filterValueNav = request.getParameter("state").toString().toUpperCase();
			}
	}
	
	if(filterValueNav != null){
		if(filterValueNav.toString().equals("null")){
			filterValueNav = "All Records";
		}
	}
	

   
	   
	//SETTING UP THE DATASET FOR MERGEMANAGER
	
	//paths go here
    String ge = pageContext.getServletConfig().getInitParameter("gePath").toString();
   
	if(isFirst){
		String file="";
		if(dataset.equals("ge")){
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
			mergeManager.updateUserIds(userRows,userName,email);
			shouldSave = true;
		}
		if(!map.isEmpty()){
		mergeManager.updateComments(map);
		shouldSave = true;
		}
		
		//check whether rows have been marked for demerge; if yes,call the demerge method
		String [] rowsToBeDemerged = request.getParameterValues("demerges");
		//testing deMerge; Remove later
		//rowsToBeDemerged = new String[]{"26827","31908", "63686", "70245", "8576", "31906", "26815"};
		if(rowsToBeDemerged!=null){
			mergeManager.deMerge(rowsToBeDemerged);
			shouldSave = true;
		}
		
		if(shouldSave)
			mergeManager.save(ge);
		}
	
		//WORKING WITH FILTER PARAMETERS
		
			if(!filterValue.equals("All Records") && request.getParameterValues("filterValue")!= null){
				incumbentsList = mergeManager.getIncumbents(filterParam, request.getParameterValues("filterValue"));
			}
			else if(request.getParameter("state") != null){
				if(!request.getParameter("state").toString().equals("All Records") && !request.getParameter("state").toString().equals("")){
					incumbentsList = mergeManager.getIncumbents("State", new String[] {request.getParameter("state").toString().toUpperCase()});
				}
				else{
					incumbentsList = mergeManager.getIncumbents();
				}
			}
		
			else{
				incumbentsList = mergeManager.getIncumbents();
			}   
					
		int[] progressData = mergeManager.getListCount(incumbentsList);	

	%>
	


	<!-- Modal -->
	<div class="modal fade" id="filterModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="myModalLabel">Filter Menu</h4>
	      </div>
	      <div class="modal-body">
	       	<div class="filterForm">
				<form class="form" role="filter" method="get" action="incumbency_table.jsp" onsubmit="incumbency_table.jsp">
					<div class="form-group">
							<select class="form-control" name="dataset">
								<option value="ge">General Election Candidate</option>
								<option value="bihar">Bihar Election Candidate</option>
								<option value="rajasthan">Rajasthan Election Candidate</option>
							</select>
					</div>
					<div class="form-group">
						<select class="form-control" name="algorithm">
								<option value="exactSameName">Exact Same Name</option>
								<option value="editDistance1">Approximate Name with Edit Distance 1</option>
								<option value="editDistance2">Approximate Name with Edit Distance 2</option>
						</select>
					</div>
					<div class="form-group">
						<select class="form-control" id="filterParam" name="filterParam" onchange="populateDropdown()">
							<option value="State" id="Primary">State</option>
							<option value="Party" >Party</option>
							<option value="PC_name" >Constituencies</option>
						</select>
					</div>
						<select multiple class="form-control" id="filterValue" name="filterValue">
							<option value="All Records">All Records</option>
						</select>
					</div>
				</div>
				<div class="modal-footer">
					<button type="submit" class="btn btn-default" style="margin:0 auto; display:table;">Submit</button>
	      		</div>
			</div>
   		</form>
    </div>
  </div>
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
					<a class="navbar-brand" href="#">Candidate Mapper</a>
				</div>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<input type="submit" class="btn btn-default navbar-btn navbar-right" name="submit" value="Save" id="saveButton"/>
				<button type="button" style="margin-right:0.9em; height:35px;"class= "btn btn-default navbar-btn navbar-right" data-toggle="modal" data-target="#filterModal">
						<span class="glyphicon glyphicon-filter" aria-hidden="true"></span>
				</button>
				<div class="collapse navbar-collapse navbar-right" id="bs-example-navbar-collapse-1">
					<ul class="nav navbar-nav">
						<ol class="breadcrumb">
						  <li><a data-toggle="modal" data-target="#filterModal"><%= filterParamNav %></a></li>
						  <li><a data-toggle="modal" data-target="#filterModal"><%= filterValueNav %></a></li>
						</ol>
						<li><div class="navbar-text"><%= progressData[0] %> Total Records</div></li>
						<li><div class="navbar-text"><%= progressData[2] %> Records Mapped</div></li>
						<li><div class="navbar-text"><%= userName%></div></li>
<!-- 						<li><div class="navbar-text" id="test">Howdy</div></li> -->
					</ul>
				</div>

				<!-- /.navbar-collapse -->
			</div><!-- /.container-fluid -->
		</nav>
		<div class="table-div table-responsive">
			<table class="table table-hover">
				<tbody class="inside-table">
					<tr class="table-row">
						<th class="cell-table">Merge</th>
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
						<th class="cell-table">Unmerge</th>
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
				String unMerge;
				if(mergeManager.isMappedToAnother(row.get("ID"))){
					tableData = "<mapped dummy tag>";
					unMerge = "<input type=\"checkbox\" class=\"checkBox\" name=\"demerges\" value=\""+row.get("ID")+"\"/>";
				} 
				else {
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("ID")+"\"/>";
					unMerge = "<input type=\"checkbox\" class=\"checkBox\" name=\"demerges\" value=\""+row.get("ID")+"\"/>";
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
				<td class="cell-table mergeCol"><%=tableData %></td>
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
				<td class="cell-table" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=row.get("ID")%>')">
<%-- 				 onmouseover="commentDisplayer('comment-<%=row.get("ID")%>')" --%>
					<%-- <div id=comment-<%=row.get("ID")%> onclick="commentHandler('comment-<%=row.get("ID")%>')"> --%>
					<div class="comment-box"><div style="padding:0.3em"><%=row.get("comments")%></div></div>
					<!-- </div> -->
				</td>
				<td class="cell-table unMergeCol"><%=unMerge%></td>
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
//$("#test").on("click", function(){
<%-- //	document.write("<%=request.getParameterValues("demerges")%>"); --%>
//});


//SCRIPT FOR HIGHLIGHTING AND CHECKING ROWS 

$("document").ready(function(){
	$("tr td:not(:nth-last-child(2)):not(:last-child)").on("click", function(e){
		if($(e.target).closest('input[type="checkbox"]').length > 0){
			$(this).parent().toggleClass("success");
        }
		else{
			$(this).parent().toggleClass("success");
			var checkboxValue = $(this).parent().find("td:first-child input[type]").prop("checked");
			$(this).parent().find("td:first-child input[type]").prop("checked", !checkboxValue);
		}

	});
});



//LOADING SCRIPT
$(window).on("load",function(){
    $('#loading').fadeOut();
});



</script>
</body>
</html>