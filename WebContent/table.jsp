<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
		 import="in.edu.ashoka.surf.Row"
import="in.edu.ashoka.surf.MergeManager"
import="java.util.*"
%>
<%@ page import="edu.stanford.muse.util.Util" %>
<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome-4.7.min.css" rel="stylesheet">

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!--	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous"> -->

    <link rel="stylesheet" href="css/bootstrap.min.css">
    <!-- Optional theme -->
    <link rel="stylesheet" href="css/bootstrap-theme.min.css">

    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/surf.css">

    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.12.1/jquery.min.js"></script>
    <script type="text/javascript"> if (!window.jQuery) {document.write('<script type="text/javascript" src="js/jquery-1.12.1.min.js"><\/script>');}</script>

    <script type="text/javascript" src="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
    <script type="text/javascript"> if (!(typeof $().modal == 'function')) { document.write('<script type="text/javascript" src="js/bootstrap-3.1.1.min.js"><\/script>'); }</script>

    <script src="js/selectpicker.js"></script>

	<title>Surf</title>
</head>
<body>
<%
    int currentPage = 1;
    try { currentPage = Integer.parseInt (request.getParameter("page")); } catch (Exception e) { }
    MergeManager.View view = (MergeManager.View) session.getAttribute("view");
    if (view == null) {
        out.println ("Sorry, no view has been set up in the session");
        return;
    }
    List<List<List<Row>>> groupsToShow = (List<List<List<Row>>>) view.viewGroups;

    int numPages = (int) Math.ceil(((double) groupsToShow.size()) / Config.groupsPerPage);

    String description = view.description();

%>


<!-- Modal -->
<div class="modal fade" id="filterModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Filter and Sort</h4>
            </div>
            <div class="modal-body">
                <div class="filterForm">
                    <input type="hidden" name="filterOnly"/>
                    <%@include file="filter-controls.jspf" %>

                    <div class="modal-footer">
                        <button class="btn btn-default filter-submit-button" style="margin:0 auto; display:table;">OK</button>
                    </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="helpModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" >Surf Help</h4>
            </div>
            <div class="modal-body">

                <p>

                    <p>
                    Current dataset:
                    <p>
                    <%= Util.escapeHTML(description).replace("\n", "<br/>")%>
                <p>
                <hr/>
                <b>About Surf</b>
                <p></p>
                Surf is a data cleaning tool for messy data. It enables a user to quickly scan a dataset for rows that have approximately similar values in a column,
                and confirm when the values are actually the same. Rows confirmed to have the same value in the selected column are assigned the same ID.
                <p>
                    <% String gitId = Config.gitProps.getProperty ("git.commit.id.describe-short");
                        if (gitId == null)
                            gitId = "(Unavailable)";
                        String gitCommitTime = Config.gitProps.getProperty ("git.commit.time");
                        if (gitCommitTime == null)
                            gitCommitTime = "(Unavailable)";
                        String gitBuildUser = Config.gitProps.getProperty ("git.build.user.name");
                        if (gitBuildUser == null)
                            gitBuildUser = "(Unavailable)";
                        String gitBranch = Config.gitProps.getProperty ("git.branch");
                        if (gitBranch == null)
                            gitBranch = "(Unavailable)";
                    %>
                    Surf version: <%= gitId%> <br/>
                    Built <%= gitCommitTime%> by <%=gitBuildUser%> from branch <%=gitBranch%>
                </p>
            </div>
        </div>
    </div>
</div>


    <!-- Brand and toggle get grouped for better mobile display -->

<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="logo navbar-brand" style="font-size:30px;margin-left:20px;" href="#">Surf</a>
        </div>

        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <li><button style="margin-left:40px;" class="btn btn-default filter-button" type="button">Filter <i style="display:none" class="filter-spinner fa fa-spin fa-spinner"></i></button></li>
                <li><button class="btn btn-default merge-button" type="button">Merge <i style="display:none" class="merge-spinner fa fa-spin fa-spinner"></i></button> <span>Across Groups <input class="across-groups" type="Checkbox"></span></li>
                <li><button class="btn btn-default unmerge-button" type="button">Unmerge <i style="display:none" class="unmerge-spinner fa fa-spin fa-spinner"></i></button></li>
                <li><button class="btn btn-default help-button" type="button">Help</button></li>
            </ul>
        </div>
    </div>
</nav>
<br/>
<br/>

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
<!--                <th class="cell-table ">Comments</th> -->
            </tr>
            </thead>
<%
							
    //MAKES THE CSS FOR DISPLAYING RECORDS AS GROUPS
	boolean firstGroup = true;
	int startGid = (currentPage-1) * Config.groupsPerPage; // the currentPage as shown to the user in the URL and the bottom nav always starts from 1, not 0. so we adjust for it.
	int endGid = Math.min (((currentPage) * Config.groupsPerPage), groupsToShow.size()); // endgid is not inclusive

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
				String href = "http://www.google.com/search?q=" + row.get(Config.MERGE_FIELD).replace(" ","+") + "+" + row.get("Constituency").replace(" ","+") + "+" + row.get("Year");
				String pc_href = "https://www.google.co.in/maps/place/" + row.get("Constituency").replace(" ","+") + "," + row.get("statename").replace("_","+");
                hoverText = Util.escapeHTML(hoverText);
                pcInfo = Util.escapeHTML(pcInfo);

				String id = row.get(Config.ID_FIELD);
				String tr_class = "";
				if (!lastRowForThisid)
				    tr_class = "merged-row";
				if (firstRowForThisId)
				    tr_class += " first-row-for-id";
			%>

			<tr class="<%=tr_class%> trow" data-id=<%=id%>>

                <td class="cell-table table-cell-merge"><%=mergeCheckboxHTML%></td>

				<td class="cell-table table-cell-name"><a href="<%=href%>" title="<%=hoverText%>" target="_blank"><%=Util.escapeHTML(row.get(Config.MERGE_FIELD))%></a></td>
				<td class="cell-table table-cell-constituency"><a href="<%=pc_href%>" title="<%=pcInfo%>" target="_blank"><%=Util.escapeHTML(row.get("Constituency"))%></a></td>

                <%  for (String col: Config.supplementaryColumns) { %>
                    <td class="cell-table"><%=Util.escapeHTML(row.get(col))%></td>
                <% } %>
<!--                <td class="cell-table" id="comment-<%=row.get("ID")%>" style="height:2em;" onclick="commentHandler('comment-<%=id%>')"></td> -->

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

    <% if (currentPage > 1) { %>
        <li class="page-item">
            <a class="page-link" href="table?page=<%=currentPage-1%>" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
                <span class="sr-only">Previous</span>
            </a>
        </li>
    <% } %>

    <!-- Listing page numbers here -->
    <% for (int i = 1 ; i <= numPages; i++) {
            String pageClass = (currentPage == i) ? "page-item active" : "page-item"; %>
            <li class="<%=pageClass%>">
                <a class="page-link" href="table?page=<%=(i)%>"><%=i%></a>
            </li>
    <% } %>

    <% if (currentPage < numPages) { %>
        <li class="page-item">
            <a class="page-link" href="table?page=<%=currentPage+1%>" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
                <span class="sr-only">Next</span>
            </a>
        </li>
    <% } %>

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
            $('input.select-checkbox', $group).prop('checked', true);
            $('.trow', $group).addClass ('selected-trow');
            $target.text(text2);
        } else {
            $('input.select-checkbox', $group).prop('checked', false);
            $('.trow', $group).removeClass('selected-trow');
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
        var text1 = 'Select till here', text2 = 'Unselect till here';

        var $target = $(e.target);
        var $group = $target.closest('tbody'); // find the nearest tbody, which corresponds to a group
        var $groups = $group.prevAll('tbody'); // find all prev tbody's on page
        var $all = $group.add($groups);
        if ($target.text() == text1) {
            $('input.select-checkbox', $all).prop('checked', true);
            $('.trow', $all).addClass ('selected-trow');
            $target.text(text2);
        } else {
            $('input.select-checkbox', $all).prop('checked', false);
            $('.trow', $all).removeClass('selected-trow');
            $target.text(text1);
        }
    }

    /* click handler for rows, to make it so that clicking anywhere in the row gets the corresponding id checkbox selected */
    function row_click_handler (e) {
        $row = $(e.target).closest('.trow');
        if ($(e.target).is('a') || $(e.target).is('input')) {
            // return for links and checkbox, we don't want to interfere with that
            return;
        }

        // get the actual row which has the checkbox on it (which is the first row of the group
        var $row_with_checkbox = $row.hasClass('first-row-for-id') ? $row : $($row.siblings('.first-row-for-id')[0]);

        var $checkbox = $('input.select-checkbox', $row_with_checkbox);
        // toggle the checkbox
        if ($checkbox.prop ('checked')) {
            $checkbox.prop('checked', false);
            $row_with_checkbox.removeClass ("selected-trow");
        } else {
            $checkbox.prop('checked', true);
            $row_with_checkbox.addClass ("selected-trow");
        }

        e.stopPropagation();
        return false;
    };

    function checkbox_change_handler(e) {
        var $checkbox = $(e.target);
       //  alert ($checkbox);
        var $row_with_checkbox = $checkbox.closest ('.trow'); // row for this checkbox
        if ($checkbox.prop ('checked')) {
            $row_with_checkbox.addClass ("selected-trow");
        } else {
            $row_with_checkbox.removeClass ("selected-trow");
        }
        return false; // propagate further
    }

    function reviewed_till_here_handler (e) {
        var text1 = 'Mark reviewed till here', text2 = 'Mark unreviewed till here';

        var $target = $(e.target);
        var $group = $target.closest ('tbody'); // find the nearest tbody, which corresponds to a group
        var $groups = $group.prevAll ('tbody'); // find all prev tbody's on page

        // remember to set each group's button. if we're marking a group as reviewed, then it's .reviewed-button should provide the opposite option, i.e. to unreview
        if ($target.text() == text1) {
            $group.addClass('reviewed');
            $('.reviewed-button', $group).text ('Mark as unreviewed');
            $groups.addClass('reviewed');
            $('.reviewed-button', $groups).text ('Mark as unreviewed');
            $target.text(text2);
        } else {
            $group.removeClass('reviewed');
            $('.reviewed-button', $group).text ('Mark as reviewed');
            $groups.removeClass('reviewed');
            $('.reviewed-button', $groups).text ('Mark as reviewed');
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
                success: function(o) {
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

    function filter_submit_handler (e) {
        var post_data = {
            filterOnly: true,
            filterSpec: $('#filterSpec').val(),
            sortOrder: $('#sortOrder').val(),
            groupViewControlSpec: $('#groupViewControlSpec').val(),
            rowViewControlSpec: $('#rowViewControlSpec').val()
        };

        var $spinner = $('.filter-spinner');
        $spinner.fadeIn();

        $.ajax ({
            type: 'POST',
            url: 'ajax/run-merge',
            datatype: 'json',
            data: post_data,
            success: function(o) {
                $spinner.fadeOut();
                if (o && o.status == 0) {
                    // could perhaps display a toast here
                } else {
                    alert('Filter failed!');
                }
                window.location = 'table?page=1';
            },
            error: function (jqXHR, textStatus, errorThrown) { $spinner.fadeOut(); alert ('Warning: filter failed! ' + textStatus + ' ' + jqXHR.responseText);}
        });
    }



    $('.select-button').click (select_all_handler);
    $('.select-till-here-button').click (select_till_here_handler);
    $('.trow').click (row_click_handler);
    $('.trow input.select-checkbox').change (checkbox_change_handler);

    $('.reviewed-button').click (group_reviewed_handler);
    $('.reviewed-till-here-button').click (reviewed_till_here_handler);

    $('.merge-button').click (save_handler);
    $('.unmerge-button').click (save_handler);
    $('.filter-button').click (function() { $('#filterModal').modal();});
    $('.filter-submit-button').click (filter_submit_handler);
    $('.help-button').click (function() { $('#helpModal').modal()});

</script>
</body>
</html>
