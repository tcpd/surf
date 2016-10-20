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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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

//name attribute gets created only when the dropdown is clicked; this is for efficiency
function createNameParameter(id){
	var node=document.getElementById("isDone-"+id);
	node.setAttribute("name", "isDone-"+id);
}

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
	
	function saveFilterSettings(){
		var expdate = new Date ()
	    expdate.setTime (expdate.getTime() + (expdays*24*60*60*1000)); // expiry date
	    setCookie("algorithm",document.getElementById("algorithm").value,expdate);
	    setCookie("dataset",document.getElementById("dataset").value,expdate);
	    setCookie("onlyWinners",document.getElementById("onlyWinners").value,expdate);
	}
	
	function loadFilterSettings(){
		if(getCookie("algorithm")!="")
			document.getElementById("algorithm").value = getCookie("algorithm");
		if(getCookie("dataset")!="")
			document.getElementById("dataset").value = getCookie("dataset");
		if(getCookie("onlyWinners")!="")
			document.getElementById("onlyWinners").value = getCookie("onlyWinners");
	}

</script>
<title>Candidate Mapper</title>
</head>
<body onload="loadScroll()" onunload="saveScroll()">

	<%!
   //SETTING UP RREQUIRED VARIABLES

	//boolean isFirst;
	//Dataset d;
	//MergeManager mergeManager;

	

    //add other csv here or eventually take the file input from user
    //static final String ge="/Users/Kshitij/Documents/CS/Incumbency Project/lokdhaba/GE/candidates/csv/candidates_info_updated.csv";
	//static final String bihar="";
	//static final String rajasthan="";
	%>



	
   
   <!--
   
   //setUpParameter sets up a parameter which can be used across same page reloads and different pages.
	
   final String setUpParameter(String param){
	   if(request.getParameter(param)!= null){
			param = request.getParameter(param).toString();
			session.setAttribute(param, param);
			return param;
		}
	   p
	   
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
							Dataset:
							<select class="form-control" name="dataset" id="dataset">
							<!--MODIFY HERE -->
							
							<c:forEach var="name" items="${datasetName}">
								<option value="${name}"><c:out value="${datasetDescription.get(name)}"></c:out></option>
							</c:forEach>
							</select>
					</div>
					<div class="form-group">
						Algorithm:
						<select class="form-control" name="algorithm" id="algorithm">
								<option value="exactSameName">Exact Same Name</option>
								<option value="editDistance1">Approximate Name with Edit Distance 1</option>
								<option value="editDistance2">Approximate Name with Edit Distance 2</option>
								<option value="dummyAllName">All names</option>
						</select>
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
				<div class="modal-footer">
					<button type="submit" onclick="saveFilterSettings()" class="btn btn-default" style="margin:0 auto; display:table;">Submit</button>
	      		</div>
   		</form>
    </div>
  </div>
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
					<a class="navbar-brand" href="#">Neta Analytics - Candidate Mapper</a>
				</div>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<input type="submit" class="btn btn-default navbar-btn navbar-right" name="submit" value="Save" id="saveButton"/>
				<button type="button" onclick="loadFilterSettings()" style="margin-right:0.9em; height:35px;"class= "btn btn-default navbar-btn navbar-right" data-toggle="modal" data-target="#filterModal">
						<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
						Settings
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
			<table class="table table-hover header-fixed">
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
				<tbody class="inside-table">
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
				String rowCompletionColor;
				if(mergeManager.isMappedToAnother(row.get("ID"))){
					tableData = "<mapped dummy tag>";
					pageContext.setAttribute("tableData","<mapped dummy tag>");	//attribute is used by jstl; couldnt find a better way to do this
					
				} 
				else {
					tableData = "<input type=\"checkbox\" name=\"row\" value=\""+row.get("ID")+"\"/>";
					pageContext.setAttribute("tableData","");	//same as above; used ny jstl
				}
				
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
					newGroup=false;
					newPerson=false;
					rowStyleData = "class=\"table-row-new-person trow "+rowCompletionColor+" \"";
				}
				else if(newPerson==true){
					newPerson=false;
					rowStyleData = "class=\"table-row-same-person trow "+rowCompletionColor+" \"";
				}
				else{rowStyleData = "class=\""+rowCompletionColor+" \"";}
			
			
%>
			<tr <%=rowStyleData %> title="ID- <%=row.get("ID")%>, Person ID- <%=row.get("mapped_ID")%>">
				<td class="cell-table mergeCol table-cell-merge"><%=tableData %></td>
				<td class="cell-table table-cell-name">
					<%=row.get("Name")%>
				</td>
				<td class="cell-table table-cell-sex">
					<%=row.get("Sex")%>
				</td>
				<td class="cell-table table-cell-year">
					<%=row.get("Year")%>
				</td>
				<td class="cell-table table-cell-constituency">
					<%=row.get("PC_name")%>
				</td>
				<td class="cell-table table-cell-party">
					<%=row.get("Party")%>
				</td>
				<td class="cell-table table-cell-state">
					<%=row.get("State")%>
				</td>
				<td class="cell-table table-cell-position">
					<%=row.get("Position")%>
				</td>
				<td class="cell-table table-cell-votes">
					<%=row.get("Votes1")%>
				</td>
				
				
				<%-- <td class="cell-table">
					<%=row.get("ID")%>
				</td>
				<td class="cell-table">
					<%=row.get("mapped_ID")%>
				</td> --%>
				
				
				
				<%-- <c:set var="tableData" scope="page" value="lolo"></c:set> --%>
				<c:choose>
					<c:when test="${tableData eq '<mapped dummy tag>' }">
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
						<select id="isDone-<%=row.get("ID")%>" onclick="createNameParameter('<%=row.get("ID")%>')">
							<%
							String selected = row.get("is_done");
							String selectedNo,selectedYes;
							if(selected==null||selected.equals("no")){
								selectedNo="selected";
								selectedYes="";
							}
							else if(selected.equals("yes")){
								selectedNo="";
								selectedYes="selected";
							}
							else{
								selectedNo="selected";
								selectedYes="";
							}
							%>
							
							<option value="no" <%=selectedNo %>>no</option>
							<option value="yes"<%=selectedYes %>>yes</option>
						</select>
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
</form>

<div id="page-block">
	
	<!-- Page Navigation bar here -->
	<nav aria-label="Page navigation">
  	<ul class="pagination pre-margin">
  	
  	<!-- Listing previous page url here-->
  	<c:if test="${currentPage != 1}">
	    <li class="page-item">
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
				<li class="${pageIsActive}"><a class="page-link" href="IncumbencyServlet?page=${i}">${i}</a></li>
	</c:forEach>
    
    <!-- Listing next page url here -->
    
    <c:if test="${currentPage lt noOfPages}">
				<li class="page-item">
      			<a class="page-link" href="IncumbencyServlet?page=${currentPage + 1}" aria-label="Next">
        		<span aria-hidden="true">&raquo;</span>
        		<span class="sr-only">Next</span>
      			</a>
    			</li>
	</c:if>
    
    
  </ul>
</nav>

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

<!-- //SCRIPT FOR HOVER POP OVER
<script>
$(document).ready(function(){
    $('[data-toggle="popover"]').popover();   
});
</script> -->

</body>
</html>