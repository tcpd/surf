<%@ page contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
         import="java.util.*"
%>
<%@ page import="in.edu.ashoka.surf.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <link rel="icon" type="image/png" href="images/surf-favicon.png">
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
    <%String key = (String) session.getAttribute("datasetKey");%>
    
    <script type="text/javascript">
    function checkBox()
    {
        <% for (String col: Config.actualColumns.get(key)) {%>
        var check = document.getElementById("<%=col%>"+"Table");
        if(check)
        {
            if(document.getElementById("<%=col%>"))
            {
                document.getElementById("<%=col%>").setAttribute("checked", "true");
            }   
        }
        <% } %>
    }
    </script>
    <script src="js/selectpicker.js"></script>
    <style>
        .warn-dup { color: red; }
        .warn { background-color: red; padding: 3px; color: white; }
        .warn-gender { color: red; }
        .consecutive::after { content: '\02191'; /* darr */ }
        .special { background-color: limegreen; color:white;}
        .special, .unspecial { padding: 3px; }
        .insignificant-row { opacity: 0.7; color: gray;} /* specify opacity separately, so that if it is faded even if some other color overrides */
        .special-row { background-color: lightblue;} /* specify opacity separately, so that if it is faded even if some other color overrides */
        td, th { padding: 0px 5px;}
    </style>
	<title>Surf</title>
</head>
<body>
<%
    Dataset dataset = (Dataset) session.getAttribute("dataset");

    int currentPage = 1;
    try { currentPage = Integer.parseInt (request.getParameter("page")); } catch (Exception ignored) { }
    MergeManager.View view = (MergeManager.View) session.getAttribute("view");

    if (view == null) {
        out.println ("Sorry, no view has been set up in the session");
        return;
    }
    List<List<List<Row>>> groupsToShow = view.viewGroups;
    MergeManager mergeManager = view.getMergeManager();

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
<div class="modal fade" id="colSelectModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Columns to Show</h4>
            </div>
            <div class="modal-body">
            <form method="post" action="columnViewUpdate">
                <label>Columns to show:</label>
                <% for (String col: Config.actualColumns.get(key)) { %>
                    <div style="margin: 5px;"><input type="checkbox" name="<%=col%>" id="<%=col%>" value="<%=col%>"> <%=col%> </div>
                <% } %>
                <div class="modal-footer">
                    <button class="btn btn-default" style="margin:0 auto; display:table;">OK</button>
                </div>
            </form>
            </div>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="downloadModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Download Dataset</h4>
            </div>
            <div class="modal-body">
            <form method="get" action="downloadServlet">
                <label style="margin: auto; display: table;">Are you sure you want to download the dataset?</label>
                <br>
                <div style="margin: auto; text-align: center">
                <button class="btn btn-default" style="margin: auto; display: inline-block;">YES</button>
                <button class="btn btn-default" data-dismiss="modal" aria-label="Close" style="margin: auto; display: inline-block;">NO</button>
                </div>
            </form>
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
                    <% String gitId = Config.gitProps.getProperty ("git.commit.id.describe");
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
                    Built <%= gitCommitTime%> by <%=gitBuildUser%> from branch <%=gitBranch%><br/>
                    <br/>
                    New to Surf? See the <a target="_blank" href="user-manual.jsp">Surf user manual</a>.
                    <br/>
                    Find a bug in Surf? Got a suggestion for improvement? Please file it <a target="_blank" href="https://github.com/tcpd/surf/issues">here</a>.
                </p>
            </div>
        </div>
    </div>
</div>


    <!-- Brand and toggle get grouped for better mobile display -->

<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="logo navbar-brand" style="font-size:30px;margin-left:20px;" href="/surf">Surf</a>
        </div>

        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <li><button style="margin-left:40px; margin-top: 7px;" onclick="checkBox();" class="btn btn-default colSelect-button" type="button">Column Select <i style="display:none" class="fa fa-spin fa-spinner"></i></button></li>
                <li><button style="margin-top: 7px;" class="btn btn-default filter-button" type="button">Filter <i style="display:none" class="filter-spinner fa fa-spin fa-spinner"></i></button></li>

                <li><button style="margin-top: 7px;" class="btn btn-default merge-button" type="button">Merge <i style="display:none" class="merge-spinner fa fa-spin fa-spinner"></i></button> <span style="position:relative; top:5px;margin-left: 15px;">Across Groups <input class="across-groups" type="Checkbox"></span></li>
                <li><button style="margin-top: 7px;" class="btn btn-default unmerge-button" type="button">Unmerge <i style="display:none" class="unmerge-spinner fa fa-spin fa-spinner"></i></button></li>
                <li><button style="margin-top: 7px;" class="btn btn-default tbr-button" type="button">Flag for review<i style="display:none" class="tbr-spinner fa fa-spin fa-spinner"></i></button></li>

                <li><button style="margin-top: 7px;" class="btn btn-default help-button" type="button">Help</button></li>
                <li><button style="margin-top: 7px; margin-right: 10px;" class="btn btn-default dwnld-button" type="button">Download Dataset</button></li>
            </ul>
        </div>
    </div>
</nav>
<br/>
<br/>

        <!-- main table starts here -->
        <table class="table-header" style="border-collapse: collapse">
            <thead>
            <tr style="z-index: 200; background-color: white; height: 30px;" class="table-row">
                <th class="cell-table"></th>
                <%-- <th class="cell-table">Name</th> 
                <%-- ask prof about this. why is this hardcoded? 
                <th class="cell-table">Constituency</th> --%>
                <%String mcol = Config.MERGE_FIELD;%>
                <th style="min-width:300px" class="cell-table" id="<%=mcol%>Table" name="<%=mcol%>Table"><%=mcol%></th>
                <% for (String col: Config.showCols) { %>
                <% if(col.equalsIgnoreCase(Config.MERGE_FIELD))
                        continue;%>
                    <th class="cell-table" id="<%=col%>Table" name="<%=col%>Table"><%=col%></th>
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

        boolean isReviewed = groupRows.stream().flatMap(List::stream).allMatch(r -> mergeManager.hasLabel (r, "reviewed"));
        String isReviewedClass = isReviewed ? "reviewed" : "";
        String tooltip = "Cluster with " + Util.pluralize(groupRows.size(), " id");
        %>
        <tbody title="<%=tooltip%>" data-groupId="<%=gid%>" class="inside-table <%=isReviewedClass%>" id="table-body">
        <tr class="toolbar-row">
            <td colspan="20"> <!-- 20 just to be safe -- we just want it at extreme right -->
                <button data-groupId="<%=gid%>" class="select-button" type="button" id="select-all" >Select all</button>
                <button data-groupId="<%=gid%>" class="reviewed-button" type="button" id="done-all">Mark as <%=isReviewed ? "unreviewed" : "reviewed"%></button>

                <% if (!firstGroup) { %> <%-- don't show "till above" buttons for first group --%>
                    <button data-groupId="<%=gid%>" class="reviewed-till-here-button" style="float: right; margin-right: 10px" type="button">Mark reviewed till here</button>
                    <button data-groupId="<%=gid%>" class="select-till-here-button" style="float: right; margin-right: 10px" type="button">Select till here</button>
                <% } %>

            </td>
        </tr>

        <%

            // compute vals that occur more than once
            Set<String> duplicatedColVals = new LinkedHashSet<>();
            boolean inconsistentGender;
            String dupColName = "Assembly_No"; // expected to be different for all rows of a merged record, if not different, year is given a warning class
            String YEAR_COL = "Year";
            String GENDER_COL = "Sex"; // expected to be consistent for a merged record, otherwise warning class added
            String POSITION_COL = "Position"; // decorated with .special for special values
            String POLL_NO_COL = "Poll_No";

            {
                Set<String> seenVals = new LinkedHashSet<>();
                Set<String> seenGenderVals = new LinkedHashSet<>();

                for (List<Row> rowsForThisId : groupRows) {
                    for (Row row : rowsForThisId) {
                        String colVal = row.get(dupColName);
                        if (seenVals.contains(colVal))
                            duplicatedColVals.add(colVal);
                        seenVals.add(colVal);

                        colVal = row.get(GENDER_COL);
                        seenGenderVals.add(colVal);
                    }
                }
                inconsistentGender = (seenGenderVals.size() > 1);
            }

            int prev_ae = -1;
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
				String pcInfo = "Constituency number: " + row.get("Constituency_No") + " (Delim " + row.get("DelimId") + ") Subregion: " + row.get("Sub_Region");
				String href = "http://www.google.com/search?q=" + row.get(Config.MERGE_FIELD).replace(" ","+") + "+" + row.get("Constituency_Name").replace(" ","+") + "+" + row.get("Year");
				String pc_href = "https://www.google.co.in/maps/place/" + row.get("Constituency_Name").replace(" ","+") + "," + row.get("statename").replace("_","+");
                hoverText = Util.escapeHTML(hoverText);
                pcInfo = Util.escapeHTML(pcInfo);

				String id = row.get(Config.ID_FIELD);
				String tr_class = "";
				if (!lastRowForThisid)
				    tr_class = "merged-row";
				if (firstRowForThisId)
				    tr_class += " first-row-for-id";
                boolean rowInsignificant = Util1.parseInt(row.get("Position"), -1) > 5 && "IND".equals(row.get("Party"));
                if (rowInsignificant)
                    tr_class += " insignificant-row ";
                boolean rowisSpecial = "true".equals(row.get("__is_special"));
                if (rowisSpecial)
                    tr_class += " special-row ";

                String mergeFieldVal = row.get(Config.MERGE_FIELD).toUpperCase();
                if (Util.nullOrEmpty(mergeFieldVal))
                    mergeFieldVal = "(blank)";
        %>

			<tr class="<%=tr_class%> trow" data-id=<%=id%>>

                <td class="cell-table table-cell-merge"><%=mergeCheckboxHTML%></td>

                <!-- need word-break: break-all to break very long names -->
				<td style="min-width:300px;word-break:break-all" class="cell-table table-cell-name"><a href="<%=href%>" title="<%=hoverText%>" target="_blank"><%=Util.escapeHTML(mergeFieldVal)%></a></td>
				<%-- <td class="cell-table table-cell-constituency"><a href="<%=pc_href%>" title="<%=pcInfo%>" target="_blank"><%=Util.escapeHTML(row.get("Constituency_Name").toUpperCase())%></a></td> --%>

                <%
                    for (String col : Config.showCols) {
                        if(col.equalsIgnoreCase(Config.MERGE_FIELD))
                            continue;
                        String classStr = "", textClass = "unspecial";
                        // compute decorations (optional), for LD dataset only
                        {
                            boolean warn = false, warnGender = false, consecutive_aes = false;
                            String colVal = row.get(col);
                            if (col.equals(YEAR_COL)) {
                                warn = duplicatedColVals.contains(row.get(dupColName));
                                int ae = Util1.parseInt(row.get(dupColName), -1);
                                consecutive_aes = (ae == prev_ae + 1);
                                prev_ae = ae;
                            }
                            if (col.equals(GENDER_COL) && inconsistentGender) {
                                warnGender = true;
                            }
                            classStr += warn ? " warn-dup " : "";
                            classStr += warnGender ? " warn-gender " : "";
                            classStr += consecutive_aes ? " consecutive " : "";

                            // special case for elections dataset
                            if (col.equals(POSITION_COL)) {
                                int pos = Util1.parseInt(colVal, -1);
                                if (pos > 0 && pos <= 3)
                                    textClass = "special";
                            }

                            // special case for elections dataset
                            if (col.equals(POLL_NO_COL)) {
                                int x = 0; // warn by default
                                try { x = Integer.parseInt(row.get(col)); } catch (Exception ignored) { }
                                if (x > 0)
                                    textClass = "warn";
                            }
                        }
                        %>

                        <% if (col.equals("Constituency_Name")) { %>
                            <td class="cell-table <%=classStr%>"> <a target="_blank" href="<%=pc_href%>"><span class="<%=textClass%>"><%=Util.escapeHTML(row.get(col))%></span></a>   </td>
                        <% } else { %>
                                <td class="cell-table <%=classStr%>"> <span class="<%=textClass%>"><%=Util.escapeHTML(row.get(col))%></span></td>
                        <% } %>
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
            $group.addClass('reviewed');
            $('.reviewed-button', $group).text ('Mark as unreviewed');
        } else {
            $('input.select-checkbox', $group).prop('checked', false);
            $('.trow', $group).removeClass('selected-trow');
            $target.text(text1);
            $group.removeClass('reviewed');
            $('.reviewed-button', $group).text ('Mark as reviewed');
        }
        window.last_name = ($($('td', $group)[2]).text());
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

        // get the actual row which has the checkbox on it (which is the first row of the id)
        var $row_with_checkbox = $row.hasClass('first-row-for-id') ? $row : $($row.prev('.first-row-for-id'));

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

        var op, $spinner;

        if ($(e.target).hasClass('merge-button')) {
            op = 'merge';
            $spinner = $('.merge-spinner');
        } else if ($(e.target).hasClass('tbr-button')) {
            op = 'tbr';
            $spinner = $('.tbr-spinner');
        } else {
            op = 'unmerge';
            $spinner = $('.unmerge-spinner');
        }

        $groups = $('tbody'); // find the nearest tbody, which corresponds to a group
        var commands = [];

        // gather un/merge commands
        {
            var across_groups = $('.across-groups').is(':checked');

            if (across_groups || op === 'unmerge') {
                // if across_groups there will be a single command, merging all the id's regardless of group
                // if unmerge also, there will be a single command with all the ids to be broken up
                var command = {op: op, groupId: 'none', ids: []}; // if merging across groups, groupId doesn't matter.
                $checked = $('input.select-checkbox:checked'); // gather checked checkboxes anywhere on the page
                for (var j = 0; j < $checked.length; j++) {
                    command.ids.push($($checked[j]).attr('data-id'));
                }
                commands[0] = command;
                $('.across-groups').prop('checked', false); // deliberately set it to false immediately after, we're worried about accidental merges across groups. across-groups should be the exception rather than the rule.
            } else {
                // merge or tbr op. within clusters
                for (var i = 0; i < $groups.length; i++) {
                    var $group = $($groups[i]);
                    var commandForThisGroup = {op: op, groupId: $group.attr('data-groupId'), ids: []}; // groupId is not directly used but we keep it anyway for future use

                    $checked = $('input.select-checkbox:checked', $group);
                    if ($checked.length == 0)
                        continue; // nothing to do

                    if ($checked.length < 2 && op === 'merge') // for merge only (not for tbr), we need to have at least 2 ids checked, otherwise nothing to do
                        continue;

                    for (var j = 0; j < $checked.length; j++) {
                        commandForThisGroup.ids.push($($checked[j]).attr('data-id'));
                    }
                    commands.push(commandForThisGroup);
                }
            }
        }

        // gather un/reviewed status for all groups and add it to the commands
        {
            var reviewedGroupIds = [], unreviewedGroupIds = [];
            for (var i = 0; i < $groups.length; i++) {
                var $group = $($groups[i]);
                // gather all group ids into 2 arrays, reviewed and unreviewed
                if ($group.hasClass('reviewed')) {
                    reviewedGroupIds.push($group.attr('data-groupid'));
                } else {
                    unreviewedGroupIds.push($group.attr('data-groupid'));
                }
            }

            var commandForReviewedGroups = {op: 'add-label', label: 'reviewed', ids: reviewedGroupIds}; // groupId is not directly used but we keep it anyway for future use
            var commandForUnreviewedGroups = {op: 'remove-label', label: 'reviewed', ids: unreviewedGroupIds}; // groupId is not directly used but we keep it anyway for future use
            commands.push(commandForReviewedGroups);
            commands.push(commandForUnreviewedGroups);
        }

        // now send the commands to the backend
        {
            var post_data = {json: JSON.stringify(commands)};

            $spinner.fadeIn();

            $.ajax({
                type: 'POST',
                url: 'ajax/do-commands',
                datatype: 'json',
                data: post_data,
                success: function (o) {
                    $spinner.fadeOut();
                    if (o && o.status == 0) {
                        // could perhaps display a toast here
                        window.location = 'table?page=' + getParameterByName('page', window.location.href) + '&scrollTo=' + escape(window.last_name);
                    } else {
                        alert('Save failed!');
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    $spinner.fadeOut();
                    alert('Warning: save failed! ' + textStatus + ' ' + jqXHR.responseText);
                }
            });
        }
    }

    function filter_submit_handler (e) {
        var post_data = {
            filterOnly: true,
            filterSpec: $('#filterSpec').val(),
            sortOrder: $('#sortOrder').val(),
            groupViewControlSpec: $('#groupViewControlSpec').val(),
            secondaryFilterFieldName: $('#secondaryFilterFieldName').val(),
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
                    alert('Merge failed!');
                }
                window.location = 'table?page=1&scrollTo=' + escape(window.last_name);
            },
            error: function (jqXHR, textStatus, errorThrown) { $spinner.fadeOut(); alert ('Warning: Merge failed! ' + textStatus + ' ' + jqXHR.responseText);}
        });
    }

    // from https://stackoverflow.com/questions/123999/how-to-tell-if-a-dom-element-is-visible-in-the-current-viewport/7557433#7557433
    function isElementInViewport (el) {

        //special bonus for those using jQuery
        if (typeof jQuery === "function" && el instanceof jQuery) {
            el = el[0];
        }

        var rect = el.getBoundingClientRect();

        return (
            rect.top >= 0 &&
            rect.left >= 0 &&
            rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
            rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
        );
    }

    function getParameterByName(name, url) {
        if (!url) url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    $('.select-button').click (select_all_handler);
    $('.select-till-here-button').click (select_till_here_handler);
    $('.trow').click (row_click_handler);
    $('.trow input.select-checkbox').change (checkbox_change_handler);

    $('.reviewed-button').click (group_reviewed_handler);
    $('.reviewed-till-here-button').click (reviewed_till_here_handler);

    $('.merge-button, .unmerge-button, .tbr-button').click (save_handler); // all these 3 buttons have the same handler
//    $('.unmerge-button').click (save_handler);
 //   $('.tbr-button').click (save_handler);

    $('.filter-button').click (function() { $('#filterModal').modal();});
    $('.filter-submit-button').click (filter_submit_handler);
    $('.colSelect-button').click (function() { $('#colSelectModal').modal();});
    $('.help-button').click (function() { $('#helpModal').modal()});
    $('.dwnld-button').click (function() { $('#downloadModal').modal();});

    // try to scroll to area that was last clicked on the merge page
    {
        window.last_name = '';// this will track the name of the cell val next to the checkbox, in the last checkbox clicked
        $('input.select-checkbox').click(function (e) {
            var $target = $(event.target);
            window.last_name = $target.closest('td').next().text()
        });

        var scrollToText = getParameterByName('scrollTo', window.location.href);

        if (scrollToText) {
            scrollToText = scrollToText.toLowerCase();
            $('td').each(function (i, elem) {
                var text = $(elem).text().toLowerCase();
                if (text.indexOf(scrollToText) >= 0) {
                    elem.scrollIntoView();
                   // alert ('scrolling to ' + scrollToText);
                    return false;
                }
                return true;
            });
        }
        ;
    }


</script>
</body>
</html>
