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
	<link rel="icon" type="image/png" href="images/surf-favicon.png">
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="bootstrap/dist/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/surf.css">

	<script src="js/jquery-1.12.1.min.js"></script>
	<script type="text/javascript" src="bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="js/selectpicker.js"></script>

</head>
<body>

<div class="logo" style="text-align:center">Surf</div>

<div class="user-input">
<form class="read-dataset-form" action="read-dataset" method="get">
	<div class="form-group">
		<label for="datasetKey">Select Dataset</label>
		<select id="datasetKey" class="form-control selectpicker" name="datasetKey"> <!-- called state for historical reasons, TOFIX -->
		<% for (String key: Config.keyToDescription.keySet()) { %>
			<option value="<%=key%>"><%=Config.keyToDescription.get(key)%></option>
		<% } %>
		</select>
	</div>
	<br/>
	<div class="submit-button">
		<button type="submit" class="read-dataset-btn btn btn-default">Load dataset</button>
	</div>
	<br/>
</form>

    <script>
        $('.read-dataset-btn').click(function() {
            if (!$('#datasetKey').val())
                alert ("Please select a dataset");
            else {
                $('.read-dataset-form').submit();
            }
            return false;
        });
    </script>

    <hr/>
<form action="custom-dataset" method="get">
	<div class="submit-button">
		<button type="submit" class="btn btn-default">Upload Dataset</button>
	</div>
</form>

	<hr>
	<form class="delete-dataset-form" action="delete-dataset" method="post">
		<div class="form-group">
			<label for="dataset">Delete Dataset</label>
			<select id="dataset" class="form-control selectpicker" name="dataset"> <!-- called state for historical reasons, TOFIX -->
			<% for (String key: Config.keyToDescription.keySet()) { %>
				<option value="<%=key%>"><%=Config.keyToDescription.get(key)%></option>
			<% } %>
			</select>
		</div>
		<br/>
		<div class="submit-button">
			<button type="submit" class="delete-dataset-button btn btn-default">Delete Selected Dataset</button>
		</div>
	</form>

	<script>

        function LOG(s) {
            if (console) console.log(s);
        }

		$('.delete-dataset-button').click(function() {
			if (confirm("Are you sure?")) {
				$('.delete-dataset-form').submit();
			} else {
				LOG ('delete dataset cancelled');
			}
			return false;
		});
	</script>

</div>
</body>
</html>