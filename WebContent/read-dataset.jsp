<%@ page import="in.edu.ashoka.surf.*" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Surf</title>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/style.css">

	<script src="js/jquery-1.12.1.min.js"></script>
	<script type="text/javascript" src="bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="js/selectpicker.js"></script>

</head>
<body>

<div class="logo" style="text-align:center">Surf</div>
    <div class="user-input">

        <form method="get" action="select-op">
        <%
            // Set up dataset in the session
            Dataset dataset = MergeServlet.loadDataset(request);
            session.setAttribute("dataset", dataset);

            if (dataset != null) {
                int nRows = dataset.getRows().size();
                int nCols = 0;
                if (nRows > 0) {
                    nCols = dataset.getRows().iterator().next().nFields();
                }

        %>
                Dataset with <%=nRows%> rows and <%=nCols%> columns loaded.<br/>
                <% if (!dataset.hasIds()) {
                    dataset.initializeIds(); %>
                    Ids have not yet been assigned. <br/>
                    Now assigned new ids.
                 <%} else {
                    int nIds = SurfExcel.split (dataset.getRows(), Config.MERGE_FIELD).size(); %>
                    <%=nIds%> unique ids previously assigned.
                <% } %>
                <br/>
                <br/>
                <label for="columnName">Column to merge:</label>
                <select class="form-control selectpicker" id="columnName" name="columnName">
                    <% for (String col: dataset.getColumnNames()) { %>
                        <option value="<%=col%>"><%=col%></option>
                    <% } %>
                </select>

            <% } else { %>
                Sorry, unable to load file.
            <% } %>

            <p>
            <br/>
            <br/>
            <div style="text-align:center">
                <button class="btn btn-default">Continue</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>