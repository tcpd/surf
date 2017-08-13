<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
		 import="in.edu.ashoka.surf.Row"
import="in.edu.ashoka.surf.MergeManager"
import="java.util.*"
%>
<%@ page import="edu.stanford.muse.util.Util" %>
<%@ page import="in.edu.ashoka.surf.Filter" %>
<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome-4.7.min.css" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome.css" rel="stylesheet">

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" type="text/css" href="style.css">
    <script src="js/jquery-1.12.1.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>

	<title>Surf</title>
</head>
<body>


    <div id="loading" style="padding-top: 20%">
        <img src="loading.gif" alt="LOADING.."/>
    </div>

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
						Show only Winners:
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


<%
    MergeManager mergeManager = (MergeManager)session.getAttribute("mergeManager");

    Filter filter = new Filter ("Position=1,2,3"); // just for testing
    List<List<List<Row>>> rowsToShow = mergeManager.applyFilter (mergeManager.listOfSimilarCandidates, filter);
    int currentPage = 0;
    try { currentPage = Integer.parseInt (request.getParameter("page")); } catch (Exception e) { }

%>

    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="top-bar">
        <span class="logo" style="font-size:30px;margin-left:20px;">Surf</span>
        <button class="btn btn-default" type="button">Filter</button>
        <button class="btn btn-default" type="button">Save <i class="fa fa-spin fa-spinner"></i></button>
        <span>Across Groups <input type="Checkbox"></span>
        <div style="float:right; display:inline; margin-right:20px;margin-top:5px">
            <button class="btn btn-default" type="button">Help</button>
        </div>
    </div>

        <!-- main table starts here -->
        <table class="table-header">
            <thead>
            <tr class="table-row">
                <th class="cell-table"></th>
                <th class="cell-table">Name</th>
                <th class="cell-table">Constituency</th>
                <% for (String col: Config.supplementaryColumns) { %>
                    <th class="cell-table"><%=col%></th>
                <% } %>
                <th class="cell-table ">Comments</th>
                <th class="cell-table ">Unmerge</th>
                <th class="cell-table ">Done</th>
            </tr>
            </thead>
<%
							
    //MAKES THE CSS FOR DISPLAYING RECORDS AS GROUPS
							
	int gid = currentPage * Config.groupsPerPage;
	boolean firstGroup = true;
	for (List<List<Row>> groupRows: rowsToShow) {

        // render a group of records in a tbody (tables can have multiple tbody's)

        %>
        <tbody data-groupId="<%=gid%>" class="inside-table" id="table-body">
        <tr class="toolbar-row">
            <td colspan="20">
                <button data-groupId="<%=gid%>" class="merge-button" type="button" id="merge-all" >Merge all</button>
                <button data-groupId="<%=gid%>" class="reviewed-button" type="button" id="done-all">Mark as reviewed</button>

                <% if (!firstGroup) { // don't show "till above" buttons for first group %>

                    <button data-groupId="<%=gid%>" class="reviewed-till-here-button" style="float: right; margin-right: 10px" type="button">Mark reviewed till here</button>
                    <button data-groupId="<%=gid%>" class="merge-till-here-button" style="float: right; margin-right: 10px" type="button">Merge till here</button>
                <% } %>

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
                    unmergeCheckboxHTML = "<input type=\"checkbox\" class=\"checkBox unmerge-checkbox\" name=\"demerges\" value=\"" + row.get(Config.ID_FIELD) + "\"/>"; // provide an option for this id to be broken up
                }

				String mergeCheckboxHTML = "";
                // the first row of this id will always have a checkbox
				if (firstRowForThisId) { // && groupRows.size() > 1){
				    // the first row for every id will have a merge checkbox html
                    mergeCheckboxHTML = "<input data-id=\"" + row.get(Config.ID_FIELD) + "\" type=\"checkbox\" class=\"checkBox merge-checkbox\" name=\"row\" value=\"" + row.get(Config.ID_FIELD) + "\"/>";
				}

				// now print the actual row
				// compute name and pc hover text
				String hoverText = "ID: " + row.get(Config.ID_FIELD);
				hoverText += " Canonical: " + Util.escapeHTML(row.get ("cname"));
				hoverText += " Tokenized: " + Util.escapeHTML(row.get ("tname"));
				hoverText += " Sorted: " + Util.escapeHTML(row.get ("stname"));

				String pcInfo = "Constituency number: " + row.get("AC_no") + " (Delim " + row.get("DelimId") + ") Subregion: " + row.get("subregion");
				String doneClass =  "yes".equals(row.get("is_done")) ? "row-done" : "row-not-done";
				String href = "http://www.google.com/search?q=" + row.get("Name").replace(" ","+") + "+" + row.get("PC_name").replace(" ","+") + "+" + row.get("Year");
				String pc_href = "https://www.google.co.in/maps/place/" + row.get("PC_name").replace(" ","+") + "," + row.get("State").replace("_","+");

				String id = row.get(Config.ID_FIELD);
				String tr_class = "";
				if (!firstRowForThisId)
				    tr_class = "merged-row";
			%>

			<tr class="<%=tr_class%> trow <%=doneClass%>" data-id=<%=id%>>

				<td class="cell-table table-cell-merge"><%=mergeCheckboxHTML%></td>

				<td class="cell-table table-cell-name"><a href="<%=href%>" title="<%=hoverText%>" target="_blank"><%=Util.escapeHTML(row.get("Name"))%></a></td>
				<td class="cell-table table-cell-constituency"><a href="<%=pc_href%>" title="<%=pcInfo%>" target="_blank"><%=Util.escapeHTML(row.get("PC_name"))%></a></td>

                <%  for (String col: Config.supplementaryColumns) { %>
                    <td class="cell-table"><%=Util.escapeHTML(row.get(col))%></td>
                <% } %>
                <td class="cell-table" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=id%>')"></td>

				<% if (firstRowForThisId) { %>
					<td class="cell-table unmerge-checkbox"><%=unmergeCheckboxHTML%></td>
				<% } %>
				<td class="cell-table "></td>
			</tr>
			
			<%
				firstRowForThisId = false;
			} // end row for this id
		} // end id
        gid++;
        firstGroup = false;
        %>
        </tbody>
        <%
	} // end group

%>

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

<script>
    $(document).ready(function() { $('#loading').hide();});
    function merge_all_handler (e) {
        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        if ($target.text() == 'Merge all') {
            $('input.merge-checkbox', $group).prop('checked', true); // set all checkboxes to true
            $target.text('Unmerge all');
        } else {
            $('input.merge-checkbox', $group).prop('checked', false); // set all checkboxes to true
            $target.text('Merge all');
        }
    }

    function group_reviewed_handler (e) {
        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        $group.toggleClass ('reviewed'); // set all checkboxes to true
        if ($target.text() == 'Mark as reviewed') {
            $target.text('Mark as unreviewed');
        } else {
            $target.text('Mark as reviewed');
        }
    }

    function merge_till_here_handler (e) {
        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        var $groups = $group.prevAll ('tbody'); // find all prev tbody's on page
        $('input.merge-checkbox', $group).prop('checked', true); // set all checkboxes to true
        $('input.merge-checkbox', $groups).prop('checked', true); // set all checkboxes to true
    }

    function reviewed_till_here_handler (e) {
        var text1 = 'Mark reviewed till here', text2 = 'Mark unreviewed till here';

        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        var $groups = $group.prevAll ('tbody'); // find all prev tbody's on page
        if ($target.text() == text1) {
            $group.addClass('reviewed'); // set all checkboxes to true
            $groups.addClass('reviewed'); // set all checkboxes to true
            $target.text(text2);
        } else {
            $group.removeClass('reviewed'); // set all checkboxes to true
            $groups.removeClass('reviewed'); // set all checkboxes to true
            $target.text(text1);
        }
    }

    function save_handler () {
        $groups = $('tbody'); // find the nearest tbody, which corresponds to a group
        var result = [];
        for (var i = 0; i < $groups.length; i++) {
            var $group = $($groups[i]);
            $checked = $('input.merge-checkbox:checked', $group);
            if ($checked.length < 2) // 0 or 1 means the group was not touched, or only 1 box was checked
                continue;
            var resultForThisGroup = {groupId: $group.attr('data-groupId'), ids: []};
            for (var j = 0; j < $checked.length; j++) {
                resultForThisGroup.ids.push ($($checked[i]).attr('data-id'));
            }
            result.push (resultForThisGroup);
        }

        var post_data = {json: toJson(result)};

        $('.save-spinner').show();

        $.ajax ({
            type: 'POST',
            url: 'ajax/save-dataset.jsp',
            datatype: 'json',
            data: post_data,
            success: function () { $('.save-spinner').fadeOut();},
            error: function () {}
        });
    }

    $('.merge-button').click (merge_all_handler);
    $('.reviewed-button').click (group_reviewed_handler);
    $('.merge-till-here-button').click (merge_till_here_handler);
    $('.reviewed-till-here-button').click (reviewed_till_here_handler);
    $('.save-button').click (save_handler);
    $('body').click (reviewed_till_here_handler);


</script>
</body>
</html>
