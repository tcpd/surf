<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ page import="in.edu.ashoka.surf.*" %>
<%@ page import="edu.stanford.muse.util.Util" %>
<%@ page import="javax.management.RuntimeMBeanException" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Surf</title>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome-4.7.min.css" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/style.css">

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
			<option value="compatibleNames">Compatible names</option>
            <option value="dummyAllName">All names</option>
		</select>
        <br/>
        <label for="algo-arg">Arguments for Algorithm</label>
        <input type="text" class="form-control" id="algo-arg" name="algo-arg">
    </div>

    <div class="form-group">
        <label for="splitColumn">Further split by column (optional)</label>
        <select class="form-control selectpicker" id="splitColumn" name="splitColumn">
            <option value="">None</option>
            <%
                for (String col: dataset.getColumnNames()) {
                    if (col.equalsIgnoreCase(mergeCol))
                        continue;
            %>
            <option value="<%=col%>"><%=col%></option>
            <% } %>
        </select>
    </div>

    <div class=form-group>
        <label for="filterSpec">Filter</label>
        <input id="filterSpec" name="filterSpec" type="text" class="form-control">
    </div>

    <div class="form-group">
        <label for="sortOrder">Sort order for groups</label>
        <select class="form-control selectpicker" id="sortOrder" name="sortOrder">
            <option value="stringLength">Long strings first</option>
            <option value="groupSize">Largest group first</option>
            <option value="approxAlpha">Approximately alphabetical</option>
        </select>
    </div>

	<div class="submit-button">
        <button type="submit" class="btn btn-default run-button">Run algorithm <i style="display:none" class="spinner fa fa-spin fa-spinner"></i></button>
</div>
<script>
    /** collects all input fields on the page, and makes an object out of them with the field names as property names */
    var collect_input_fields = function() {
        var result = {};
        $('input,select').each (function() {  // select field is needed for accounttype
            if ($(this).attr('type') == 'button') { return; } // ignore buttons (usually #gobutton)
            if ($(this).attr('type') == 'checkbox') {
                if ($(this).is(':checked'))
                {
                    result[this.name] = 'on';
                    epadd.log ('checkbox ' + this.name + ' is on');
                }
                else
                    epadd.log ('checkbox ignored');
            }
            else {
                result[this.name] = this.value;
            }
        });

        return result;
    };

    $('.run-button').click (function() {
        var $spinner = $('.spinner');
        $spinner.fadeIn();

        $.ajax ({
            type: 'POST',
            url: 'ajax/run-merge',
            datatype: 'json',
            data: collect_input_fields(),
            success: function (o) { if (o && o.status == 0) { window.location = 'table?page=1'; } else { alert ('Warning: error ' + o);} $spinner.fadeOut();},
            error: function () { $spinner.fadeOut(); alert ('Warning: Run algorithm failed!');}
        });
    })
</script>

</div>
</body>
</html>