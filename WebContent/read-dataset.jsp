<%@ page import="in.edu.ashoka.surf.*" %>
<%@ page import="in.edu.ashoka.surf.util.Util" %>
<%-- <%@ page import="edu.stanford.muse.util.Util" %>--%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Surf</title>
    <link rel="icon" type="image/png" href="images/surf-favicon.png">
    <link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/surf.css">

	<script src="js/jquery-1.12.1.min.js"></script>
	<script type="text/javascript" src="bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="js/selectpicker.js"></script>
    <script type="text/javascript">if(performance.navigation.type == 2 || performance.navigation.type == 255){
   location.reload(true);
    }</script>

</head>
<body>

<div class="logo" style="text-align:center">Surf</div>
    <div class="user-input">

        <%-- <form method="get" action="select-op"> --%>
        <form method="post" action="set-columns">
        <%
            // Set up dataset in the session
            Dataset dataset = MergeServlet.loadDataset(request);
            String datasetKey = (String) session.getAttribute("datasetKey");
            Config.refreshCols(datasetKey);
            session.setAttribute("dataset", dataset);

            if (dataset != null) {
                int nRows = dataset.getRows().size();
                int nCols = 0;
                if (nRows > 0) {
                    Row sampleRow = dataset.getRows().iterator().next();
                    nCols = sampleRow.nFields();

                    // HACK ALERT!!! specific hacks for election datasets - remove this when possible
                    {
                        if (sampleRow.getAllFieldNames().contains("Cand1"))
                            dataset.registerColumnAlias("Cand1", "Name");
                        else if (sampleRow.getAllFieldNames().contains("Cand"))
                            dataset.registerColumnAlias("Cand", "Name");

                        if (sampleRow.getAllFieldNames().contains("acname"))
                            dataset.registerColumnAlias("acname", "Constituency");
                        else if (sampleRow.getAllFieldNames().contains("pcname"))
                            dataset.registerColumnAlias("pcname", "Constituency");

                        if (sampleRow.getAllFieldNames().contains("Votes1"))
                            dataset.registerColumnAlias("Votes1", "Votes");
                    }
                }
        %>
                Dataset with <%=Util.commatize(nRows)%> rows and <%=Util.commatize(nCols)%> columns loaded.<br/>
                <% if (!dataset.hasIds()) {
                    dataset.initializeIds(); %>
                    Ids have not yet been assigned. <br/>
                    Now assigned new ids.
                 <%} else {
                    int nIds = SurfExcel.split (dataset.getRows(), Config.MERGE_FIELD).size(); %>
                    <%=Util.commatize(nIds)%> unique ids previously assigned.
                <% } %>
                <br/>
                <br/>
                <label for="columnName">Column for IDs:</label>
                <select class="form-control selectpicker" id="columnName" name="columnName">
                    <% for (String col: Config.actualColumns.get(datasetKey)) { %>
                        <option value="<%=col%>"><%=col%></option>
                    <% } %>
                </select>
                <br>
                <span class="help">This is the column to which you want to assign IDs.</span>
                <br>
                <br>
                <label>Columns to show during merge:</label>
                <% for (String col: Config.actualColumns.get(datasetKey)) { %>
                    <div style="margin: 5px;"><input checked type="checkbox" name="<%=col%>" value="<%=col%>"> <%=col%> </div>
                <% } %>

            <span class="help">Check the columns that will help you resolve merges. You can also select them later, on the merge screen.</span>
            <br/>
            <br/>

            <label>Within a cluster, sort rows by:</label>
                <% for (String col: Config.actualColumns.get(datasetKey)) { %>
                    <div style="margin: 5px;"><input type="checkbox" name="<%=col%>Sort" value="<%=col%>Sort"> <%=col%> </div>
                <% } %>

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