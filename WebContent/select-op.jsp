<%@ page import="in.edu.ashoka.surf.Config" %>
<%@ page import="in.edu.ashoka.surf.*" %>
<%@ page import="edu.stanford.muse.util.Util" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Surf</title>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="bootstrap/dist/css/bootstrap.min.css">
	<link rel="stylesheet" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="style.css">

	<script src="https://code.jquery.com/jquery-3.1.0.min.js"   integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s="   crossorigin="anonymous"></script>
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
<form action="merge" method="get">

	<div class="form-group">
		<label for="algorithm">Algorithm for clustering <%=mergeCol%></label>
		<select class="form-control selectpicker" id="algorithm" name="algorithm">
            <option value="editDistance">Edit distance</option>
			<option value="compatibleNames">Compatible names</option>
            <option value="dummyAllName">All names</option>
		</select>
        <br/>
        <label for="algo-arg">Arguments for Algorithm (Advanced)</label>
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
		<label for="comparatorType">Results Order</label>
		<select id="comparatorType" class="form-control selectpicker" name="comparatorType">
			<option value="confidence">By Confidence</option>
			<option value="alphabetical">Alphabetical</option>
		</select>
	</div>



	<div class="submit-button">
  <button type="submit" class="btn btn-default">Continue</button>
</div>
</form>
</div>						
</body>
</html>