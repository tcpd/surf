<%@ page import="in.edu.ashoka.surf.Config" %>
<!--Outside Views-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<% 	session.setMaxInactiveInterval(-1); // never timeout %>

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

<div class="logo" style="text-align:center">Surf</div>

<div class="user-input">
<form action="read-dataset" method="get">
	<div class="form-group">
		<label for="datasetKey">Dataset</label>
		<select id="datasetKey" class="form-control selectpicker" name="datasetKey"> <!-- called state for historical reasons, TOFIX -->
		<% for (String key: Config.keyToDescription.keySet()) { %>
			<option value="<%=key%>"><%=Config.keyToDescription.get(key)%></option>
		<% } %>
		</select>
	</div>

	<div class="submit-button">
  <button type="submit" class="btn btn-default">Submit</button>
</div>
</form>
</div>						
</body>
</html>