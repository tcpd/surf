<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ page import="in.edu.ashoka.surf.*" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Surf</title>
    <link rel="icon" type="image/png" href="images/surf-favicon.png">

    <link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome-4.7.min.css" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/surf.css">

	<script src="js/jquery-1.12.1.min.js"></script>
	<script type="text/javascript" src="bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="js/selectpicker.js"></script>

</head>
<body>

<%
    Dataset dataset = (Dataset) session.getAttribute("dataset");
	String mergeCol = request.getParameter("columnName");
	if (Util.nullOrEmpty(mergeCol))
        mergeCol = "Name";
	Config.MERGE_FIELD = mergeCol;
%>

<div class="logo" style="text-align:center">Surf</div>

<div class="user-input">

	<div class="form-group">
		<label for="algorithm">Algorithm for clustering <%=mergeCol%></label>
		<select class="form-control selectpicker" id="algorithm" name="algorithm">
            <option value="editDistance">Edit distance</option>
<!--             <option value="cosinesimilarity">Cosine Similarity</option> -->
            <option value="reviewalgo">Review Algorithm</option>
			<option value="compatibleNames">Compatible names</option>
            <option value="streaks">Streaks</option>
            <option value="allNames">All IDs in a single cluster</option>
            <option value="oneClusterPerID">One cluster per ID (for review)</option>
		</select>
        <br/>
        <div style="display:none" class="div-edit-distance">
            <label for="edit-distance">Maximum edit distance</label>
            <input type="text" class="form-control" id="edit-distance" name="edit-distance" placeholder="<%=Config.DEFAULT_EDIT_DISTANCE%>">
            <span id="edit-distance-0-help" class="help">Edit distance 0 not included</span>
        </div>
        
        <div style="display:none" class="div-review-algo">
        </div>
        
        
<%--         <div style="display:none" class="div-cosine-similarity">
            <label for="cosine-similarity">Input value</label>
            <input type="text" class="form-control" id="cosine-similarity" name="cosine-similarity" placeholder="<%=Config.DEFAULT_EDIT_DISTANCE%>">
            <span id="cosine-similarity-0-help" class="help">Cosine similarity AlGo does not require any user input</span>
        </div> --%>
        
        
        <div class="div-compat-alg-controls">
            <div class="div-min-token-overlap">
                <label for="min-token-overlap">Token overlap</label>
                <input type="text" class="form-control" id="min-token-overlap" name="min-token-overlap" placeholder="<%=Config.DEFAULT_MIN_TOKEN_OVERLAP%>">
                <span class="help">Group rows together if this number of tokens are common</span>
            </div>
            <div class="div-ignore-token-frequency">
                <label for="ignore-token-frequency">Ignore token frequency</label>
                <input type="text" class="form-control" id="ignore-token-frequency" name="ignore-token-frequency" placeholder="<%=Config.DEFAULT_IGNORE_TOKEN_FREQUENCY%>">
                <span class="help">Frequency threshold in this dataset beyond which a token will be ignored</span>
            </div>
            <br/>

            <div style="display:block" class="div-substringAllowed">
                <label for="substringAllowed">Cluster substrings together</label>
                    <input type="checkbox" class="form-control" id="substringAllowed" name="substringAllowed" checked>
            </div>
            <span class="help">If checked, a name is clustered with a longer name of which it is a part.<br/>
                e.g., <i>RAM GOPAL</i> is clustered with <i>RAM GOPAL VARMA</i><br/>
            The number of overlapping tokens does not matter. </span>
            <br/>
            <br/>

            <div class="div-initialMapping">
            <label for="initialMapping">Allow initials</label>
            <input type="checkbox" class="form-control" id="initialMapping" name="initialMapping" checked>
                <span class="help">If checked, allows partial words or initials from one name to full words in the other. <br/>e.g., <i>M. GANDHI</i> is clustered with <i>MOHANDAS GANDHI</i></span>
        </div>
            <hr/>
        </div>

        <div class="div-streak-alg-controls" style="display:none">
            <div class="div-streakColumn">
                <label for="streakFieldName">Streak column</label>
                <select  class="form-control selectpicker" id="streakFieldName" name="streakFieldName">
                    <option value="">None</option>
                    <%
                        for (String col: dataset.getColumnDisplayNames()) {
                            if (col.equalsIgnoreCase(mergeCol) || col.equals(Config.ID_FIELD))
                                continue;
                    %>
                    <option value="<%=col%>"><%=col%></option>
                    <% } %>
                </select>
                <span class="help">Streak column</span>
            </div>

            <div class="div-streakLength">
                <label for="streakLength">Streak length</label>
                <input type="text" class="form-control" id="streakLength" name="streakLength" value="<%=Config.DEFAULT_STREAK_LENGTH%>">
                <span class="help">Try to find streaks of this length</span>
            </div>

            <div class="div-maxHoles">
                <label for="maxHoles">Max. holes in streak</label>
                <input type="text" class="form-control" id="maxHoles" name="maxHoles" value="<%=Config.DEFAULT_MAX_HOLES%>">
                <span class="help">Allow these many holes in the streak</span>
            </div>

            <div class="div-secondaryColumn">
                <label for="secondaryFieldName">Secondary column</label>
                <select  class="form-control selectpicker" id="secondaryFieldName" name="secondaryFieldName">
                    <option value="">None</option>
                    <%
                        for (String col: dataset.getColumnDisplayNames()) {
                            if (col.equalsIgnoreCase(mergeCol) || col.equals(Config.ID_FIELD))
                                continue;
                    %>
                    <option value="<%=col%>"> <%=col%> </option>
                    <% } %>
                </select>
                <span class="help">Secondary field</span>
            </div>
            <br/>

            <hr/>
        </div>

    </div>

    <div class="form-group">
        <label for="splitColumn">Further split clusters by value of column (optional)</label>
        <select  class="form-control selectpicker" id="splitColumn" name="splitColumn">
            <option value="">None</option>
            <%
                for (String col: dataset.getColumnDisplayNames()) {
                    if (col.equalsIgnoreCase(mergeCol) || col.equals(Config.ID_FIELD))
                        continue;
            %>
            <option value="<%=col%>"><%=col%></option>
            <% } %>
        </select>
    </div>

    <hr/>

    <%@include file="filter-controls.jspf" %>
	<div class="submit-button">
        <button type="submit" class="btn btn-default run-button">Run algorithm <i style="display:none" class="spinner fa fa-spin fa-spinner"></i></button>
</div>
<script>
    $(document).ready(function(){
        $('[data-toggle="tooltip"]').tooltip();
    });
    /** collects all input fields on the page, and makes an object out of them with the field names as property names */
    var collect_input_fields = function() {
        var result = {};
        $('input,select').each (function() {  // select field is needed for accounttype
            if ($(this).attr('type') === 'button') { return; } // ignore buttons (usually #gobutton)
            if ($(this).attr('type') === 'checkbox') {
                if ($(this).is(':checked'))
                {
                    result[this.name] = 'on';
                }
            }
            else {
                result[this.name] = this.value;
            }
        });

        return result;
    };

    $('.run-button').click (function() {

        var data = collect_input_fields();
        if (('streaks' === data.algorithm) && (!data.streakFieldName || !data.secondaryFieldName)) {
            alert ('Please specify both the streak field name and the secondary field name');
            return;
        }

        var $spinner = $('.spinner');
        $spinner.fadeIn();

        $.ajax ({
            type: 'POST',
            url: 'ajax/run-merge',
            datatype: 'json',
            data: data,
            success: function (o) { if (o && o.status === 0) { window.location = 'table?page=1'; } else { alert ('Warning: error ' + o);} $spinner.fadeOut();},
            error: function () { $spinner.fadeOut(); alert ('Warning: Run algorithm failed!');}
        });
    });

    function set_options_for_algorithm() {
        var alg = $('#algorithm').val();
        if (alg === 'editDistance') {
            $('.div-edit-distance').show();
        } else {
            $('.div-edit-distance').hide();
        }

        if (alg === 'compatibleNames') {
            $('.div-compat-alg-controls').show();
        } else {
            $('.div-compat-alg-controls').hide();
        }

        if (alg === 'streaks') {
            $('.div-streak-alg-controls').show();
        } else {
            $('.div-streak-alg-controls').hide();
        }
        
/*         if (alg === 'cosinesimilarity') {
            $('.div-cosine-similarity').show();
        } else {
            $('.div-cosine-similarity').hide();
        } */
        
        if (alg === 'reviewalgo') {
            $('.div-review-algo').show();
        } else {
            $('.div-review-algo').hide();
        }
        
    }

    $('#algorithm').change(set_options_for_algorithm);
    $(document).ready(set_options_for_algorithm);

</script>

</div>
</body>
</html>