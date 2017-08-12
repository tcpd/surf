<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
		 import="in.edu.ashoka.surf.Row"
import="in.edu.ashoka.surf.MergeManager"
import="java.util.*"
%>

<%@ page import="in.edu.ashoka.surf.Filter" %>
<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ page import="edu.stanford.muse.util.Util" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" type="text/css" href="style.css">
    <script src="https://code.jquery.com/ui/1.12.0/jquery-ui.min.js"   integrity="sha256-eGE6blurk5sHj+rmkfsGYeKyZx3M4bG+ZlFyA7Kns7E="   crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
	<script src="helper.js" type="text/javascript"></script>

	<title>Surf</title>
</head>
<body>


<!--
<div id="loading" style="padding-top: 20%">
	<img id="loading-image" src="loading.gif" alt="LOADING.."/>
</div>
-->

   <%
		List<Collection<Row>> groupsList = (List<Collection<Row>>) session.getAttribute("subList");
		// rest of this code renders the groupsList

	   MergeManager mergeManager = (MergeManager)session.getAttribute("mergeManager");
       String[] supplementaryColumns = new String[]{"Year", "Party", "Position", "Sex", "State", "Vote"}; // supplementary columns to display. These are emitted as is, without any special processing

       Filter filter = new Filter ("Position=1,2,3"); // just for testing
       List<List<List<Row>>> rowsToShow = mergeManager.applyFilter (groupsList, filter);
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
				<form class="form" role="filter" method="get" action="merge">
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

            </div>
        </nav>

        <div style="min-width:1200px; width: 100%; height: 100%">

        <!-- main table starts here -->
        <table class="table-header">
            <thead>
            <tr class="table-row">
                <th class="cell-table">Merge</th>
                <th class="cell-table">Name</th>
                <th class="cell-table">Constituency</th>
                <% for (String col: supplementaryColumns) { %>
                    <th class="cell-table"><%=col%></th>
                <% } %>
                <th class="cell-table ">Comments</th>
                <th class="cell-table ">Unmerge</th>
                <th class="cell-table ">Done</th>
            </tr>
            </thead>
					<tbody class="inside-table" id="table-body">
<%
							
    //MAKES THE CSS FOR DISPLAYING RECORDS AS GROUPS
							
	int gid = 0;
	for (List<List<Row>> groupRows: rowsToShow) {

        // render a group of records

        // first print 2 rows -- a separator and a menubar
        %>
        <tr class="table-row-new-group trow"></tr>
        <tr>
            <td colspan="7">
                <button data-groupId="<%=gid%>" class="merge-button" type="button" id="merge-all" onclick="selectAllRowsInGroupForMerge('<%=gid%>')" >Select and merge all</button>
            </td>
            <td colspan="5">
                <button data-groupId="<%=gid%>" class="done-button" style="float:right;" type="button" id="done-all" onclick="selectAllRowsInGroupForDone('<%=gid%>')">Mark as Done</button>
                <button data-groupId="<%=gid%>" class="merge-till-here-button" style="float: right; margin-right: 10px" type="button" id="done-all-uptill" onclick="selectUpTillHereForDone('<%=gid%>')">Merge all groups above</button>
            </td>
        </tr>

        <%
        for (List<Row> rowsForThisId: groupRows) {
            // print out all rows for this id.

            boolean firstRowForThisId = true;
		    for (Row row: rowsForThisId) {

                String unmergeCheckboxHTML = "";
                // if the id has more than 2 rows, the first row will include an option to unmerge it
                if (rowsForThisId.size() > 1 && firstRowForThisId) {
                    unmergeCheckboxHTML = "<input type=\"checkbox\" class=\"checkBox\" name=\"demerges\" value=\"" + row.get(Config.ID_FIELD) + "\"/>"; // provide an option for this id to be broken up
                }

				String mergeCheckboxHTML = "";
                // the first row of this id will always have a checkbox
				if (firstRowForThisId) { // && groupRows.size() > 1){
				    // the first row for every id will have a merge checkbox html
                    mergeCheckboxHTML = "<input type=\"checkbox\" name=\"row\" value=\"" + row.get(Config.ID_FIELD) + "\"/>";
				}

				// now print the actual row
				// compute name and pc hover text
				String hoverText = "ID: " + row.get("ID") + " Person ID: " + row.get("mapped_ID");
				hoverText += " Canonical: " + Util.escapeHTML(row.get ("cname"));
				hoverText += " Tokenized: " + Util.escapeHTML(row.get ("tname"));
				hoverText += " Sorted: " + Util.escapeHTML(row.get ("stname"));

				String pcInfo = "Constituency number: " + row.get("AC_no") + " (Delim " + row.get("DelimId") + ") Subregion: " + row.get("subregion");
				String doneClass =  "yes".equals(row.get("is_done")) ? "row-done" : "row-not-done";
				String href = "http://www.google.com/search?q=" + row.get("Name").replace(" ","+") + "+" + row.get("PC_name").replace(" ","+") + "+" + row.get("Year");
				String pc_href = "https://www.google.co.in/maps/place/" + row.get("PC_name").replace(" ","+") + "," + row.get("State").replace("_","+");

				String id = row.get(Config.ID_FIELD);
			%>

			<tr class="table-row-same-person trow <%=doneClass%>" data-id=<%=id%>>

				<td class="cell-table mergeCol table-cell-merge"><%=mergeCheckboxHTML%></td>

				<td class="cell-table table-cell-name"><a href="<%=href%>" title="<%=hoverText%>" target="_blank"><%=Util.escapeHTML(row.get("Name"))%></a></td>
				<td class="cell-table table-cell-constituency"><a href="<%=pc_href%>" title="<%=pcInfo%>" target="_blank"><%=Util.escapeHTML(row.get("PC_name"))%></a></td>

                <%  for (String col: supplementaryColumns) { %>
                    <td class="cell-table"><%=Util.escapeHTML(row.get(col))%></td>
                <% } %>
                <td class="cell-table" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=id%>')"></td>

				<% if (firstRowForThisId) { %>
					<td class="cell-table"><%=unmergeCheckboxHTML%></td>
				<% } %>
				<td class="cell-table "></td>
			</tr>
			
			<%
				firstRowForThisId = false;
			} // end row for this id
		} // end id
	} // end group

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
		<a class="page-link" href="merge?page=${currentPage - 1}" aria-label="Previous">
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
					<a class="page-link" href="merge?page=${i}">${i}</a>
				</li>
	</c:forEach>
    
    <!-- Listing next page url here -->
    
    <c:if test="${currentPage lt noOfPages}">
				<li class="page-item" onclick="resetScroll(); $('#loading').fadeIn()">
      			<a class="page-link" href="merge?page=${currentPage + 1}" aria-label="Next">
        		<span aria-hidden="true">&raquo;</span>
        		<span class="sr-only">Next</span>
      			</a>
    			</li>
	</c:if>
    
    
  </ul>
</nav>

</div>
</div>

</body>
</html>
