<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
		 import="in.edu.ashoka.surf.Row"
import="in.edu.ashoka.surf.MergeManager"
import="java.util.*"
%>
<%@ page import="edu.stanford.muse.util.Util" %>
<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    int currentPage = 0;
    try { currentPage = Integer.parseInt (request.getParameter("page")); } catch (Exception e) { }
    MergeManager.View view = (MergeManager.View) session.getAttribute("view");
    List<List<List<Row>>> groupsToShow = (List<List<List<Row>>>) view.viewGroups;
%>

<!DOCTYPE html>
<html>
<head>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome-4.7.min.css" rel="stylesheet">

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/style.css">

    <script src="js/jquery-1.12.1.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <script src="js/selectpicker.js"></script>

	<title>Surf</title>
</head>
<body>
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
                        <label for="algo-arg">Filter</label>
                        <input type="text" class="form-control" id="algo-arg" name="algo-arg">
                        <br/>
                        <div class="form-group">
                            <label for="sortOrder">Sort order for groups</label>
                            <select class="form-control selectpicker" id="sortOrder" name="sortOrder">
                                <option value="nameLength">Long names first</option>
                                <option value="largestGroupFirst">Largest group first</option>
                                <option value="alpha">Alphabetical</option>
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



    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="top-bar">
        <span class="logo" style="font-size:30px;margin-left:20px;">Surf</span>

        <button class="btn btn-default" type="button">Filter</button>

        <button class="btn btn-default" type="button">Sort</button>

        <button class="btn btn-default merge-button" type="button">Merge <i style="display:none" class="merge-spinner fa fa-spin fa-spinner"></i></button> <span>Across Groups <input class="across-groups" type="Checkbox"></span>

        <div style="float:right; display:inline; margin-right:20px;margin-top:5px">
            <button class="btn btn-default unmerge-button" type="button">Unmerge <i style="display:none" class="unmerge-spinner fa fa-spin fa-spinner"></i></button>
            <button class="btn btn-default" type="button">Help</button>
        </div>
    </div>

        <!-- main table starts here -->
        <table class="table-header" style="border-collapse: collapse">
            <thead>
            <tr class="table-row">
                <th class="cell-table"></th>
                <th class="cell-table">Name</th>
                <th class="cell-table">Constituency</th>
                <% for (String col: Config.supplementaryColumns) { %>
                    <th class="cell-table"><%=col%></th>
                <% } %>
                <th class="cell-table ">Comments</th>
            </tr>
            </thead>
<%
							
    //MAKES THE CSS FOR DISPLAYING RECORDS AS GROUPS
	boolean firstGroup = true;
	int startGid = currentPage * Config.groupsPerPage;
	int endGid = Math.max (((currentPage+1) * Config.groupsPerPage), groupsToShow.size()); // endgid is not inclusive

    // we'll show groups from startGid to endGid
	for (int gid = startGid; gid < endGid; gid++) {
        List<List<Row>> groupRows = groupsToShow.get(gid);
        // render a group of records in a tbody (tables can have multiple tbody's)

        %>
        <tbody data-groupId="<%=gid%>" class="inside-table" id="table-body">
        <tr class="toolbar-row">
            <td colspan="20"> <!-- 20 just to be safe -- we just want it at extreme right -->
                <button data-groupId="<%=gid%>" class="select-button" type="button" id="select-all" >Select all</button>
                <button data-groupId="<%=gid%>" class="reviewed-button" type="button" id="done-all">Mark as reviewed</button>

                <% if (!firstGroup) { // don't show "till above" buttons for first group %>

                    <button data-groupId="<%=gid%>" class="reviewed-till-here-button" style="float: right; margin-right: 10px" type="button">Mark reviewed till here</button>
                    <button data-groupId="<%=gid%>" class="select-till-here-button" style="float: right; margin-right: 10px" type="button">Select till here</button>
                <% } %>

            </td>
        </tr>

        <%
        for (List<Row> rowsForThisId: groupRows) {
            // print out all rows for this id.

		    for (int i = 0; i < rowsForThisId.size(); i++) {
                boolean firstRowForThisId = (i == 0), lastRowForThisid = (i == rowsForThisId.size() - 1);
                Row row = rowsForThisId.get(i);
				String mergeCheckboxHTML = "";
                // the first row of this id will always have a checkbox
				if (firstRowForThisId) { // && groupRows.size() > 1){
				    // the first row for every id will have a merge checkbox html
                    mergeCheckboxHTML = "<input data-id=\"" + row.get(Config.ID_FIELD) + "\" type=\"checkbox\" class=\"checkBox select-checkbox\" name=\"row\" value=\"" + row.get(Config.ID_FIELD) + "\"/>";
				}

				// now print the actual row
				// compute name and pc hover text
				String hoverText = "ID: " + row.get(Config.ID_FIELD);
                hoverText += " Effective: " + Util.escapeHTML(row.get ("st" + Config.MERGE_FIELD));
				hoverText += " (Indianized: " + Util.escapeHTML(row.get ("c" + Config.MERGE_FIELD));
				hoverText += " Tokenized: " + Util.escapeHTML(row.get ("t" + Config.MERGE_FIELD)) + ")";
				String pcInfo = "Constituency number: " + row.get("AC_no") + " (Delim " + row.get("DelimId") + ") Subregion: " + row.get("subregion");
				String href = "http://www.google.com/search?q=" + row.get("Name").replace(" ","+") + "+" + row.get("PC_name").replace(" ","+") + "+" + row.get("Year");
				String pc_href = "https://www.google.co.in/maps/place/" + row.get("acname").replace(" ","+") + "," + row.get("statename").replace("_","+");
                hoverText = Util.escapeHTML(hoverText);
                pcInfo = Util.escapeHTML(pcInfo);

				String id = row.get(Config.ID_FIELD);
				String tr_class = "";
				if (!lastRowForThisid)
				    tr_class = "merged-row";
			%>

			<tr class="<%=tr_class%> trow" data-id=<%=id%>>

                <td class="cell-table table-cell-merge"><%=mergeCheckboxHTML%></td>

				<td class="cell-table table-cell-name"><a href="<%=href%>" title="<%=hoverText%>" target="_blank"><%=Util.escapeHTML(row.get(Config.MERGE_FIELD))%></a></td>
				<td class="cell-table table-cell-constituency"><a href="<%=pc_href%>" title="<%=pcInfo%>" target="_blank"><%=Util.escapeHTML(row.get("acname"))%></a></td>

                <%  for (String col: Config.supplementaryColumns) { %>
                    <td class="cell-table"><%=Util.escapeHTML(row.get(col))%></td>
                <% } %>
                <td class="cell-table" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=id%>')"></td>

				<td class="cell-table "></td>
			</tr>
			
			<%
				firstRowForThisId = false;
			} // end row for this id
		} // end id
        firstGroup = false;
        %>
        </tbody>
        <%
	} // end group

%>

</table>

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

<script>
    $(document).ready(function() { $('#loading').hide();});
    function select_all_handler (e) {
        var text1 = 'Select all', text2 = 'Unselect all';

        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        if ($target.text() == text1) {
            $('input.select-checkbox', $group).prop('checked', true); // set all checkboxes to true
            $target.text(text2);
        } else {
            $('input.select-checkbox', $group).prop('checked', false); // set all checkboxes to true
            $target.text(text1);
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

    function select_till_here_handler (e) {
        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        var $groups = $group.prevAll ('tbody'); // find all prev tbody's on page
        $('input.select-checkbox', $group).prop('checked', true); // set all checkboxes to true
        $('input.select-checkbox', $groups).prop('checked', true); // set all checkboxes to true
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

    function save_handler (e) {
        var op = ($(e.target).hasClass('merge-button')) ? 'merge' : 'unmerge';
        var $spinner = ($(e.target).hasClass('merge-button')) ? $('.merge-spinner') : $('.unmerge-spinner');

        $groups = $('tbody'); // find the nearest tbody, which corresponds to a group
        var commands = [];
        var across_groups =  $('.across-groups').is(':checked');

        if (across_groups || op === 'unmerge') {
            // if across_groups there will be a single command, merging all the id's regardless of group
            // if unmerge also, there will be a single command with all the ids to be broken up
            var command = {op:op, groupId: 'none', ids: []}; // if merging across groups, groupId doesn't matter.
            $checked = $('input.select-checkbox:checked'); // gather checked checkboxes anywhere on the page
            for (var j = 0; j < $checked.length; j++) {
                command.ids.push($($checked[j]).attr('data-id'));
            }
            commands[0] = command;
            $('.across-groups').prop ('checked', false); // deliberately set it to false immediately after, we're worried about accidental merges across groups. across-groups should be the exception rather than the rule.
        } else {
            for (var i = 0; i < $groups.length; i++) {
                var $group = $($groups[i]);
                var commandForThisGroup = {op: op, groupId: $group.attr('data-groupId'), ids: []}; // groupId is not directly used but we keep it anyway for future use

                $checked = $('input.select-checkbox:checked', $group);
                if ($checked.length < 2)
                    continue; // no id, or 1 id checked, in either case it doesn't matter.

                for (var j = 0; j < $checked.length; j++) {
                    commandForThisGroup.ids.push($($checked[j]).attr('data-id'));
                }
                commands.push(commandForThisGroup);
            }
        }

        var post_data = {json: JSON.stringify(commands)};

        $spinner.fadeIn();

        $.ajax ({
                type: 'POST',
                url: 'ajax/do-commands',
                datatype: 'json',
                data: post_data,
                success: function() {
                    $spinner.fadeOut();
                    if (o && o.status == 0) {
                        // could perhaps display a toast here
                    } else {
                        alert('Save failed!');
                    }
                },
            error: function (jqXHR, textStatus, errorThrown) { $spinner.fadeOut(); alert ('Warning: save failed! ' + textStatus + ' ' + jqXHR.responseText);}
        });
    }

    $('.select-button').click (select_all_handler);
    $('.reviewed-button').click (group_reviewed_handler);
    $('.select-till-here-button').click (select_till_here_handler);
    $('.reviewed-till-here-button').click (reviewed_till_here_handler);
    $('.merge-button').click (save_handler);
    $('.unmerge-button').click (save_handler);
    $('.filter-button').click (function() { $('#filterModal').modal();});

</script>
</body>
</html>
