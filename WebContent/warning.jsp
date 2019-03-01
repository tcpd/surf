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
	<script src="js/sanityCheck.js"></script>
</head>
<body>

<div class="logo" style="text-align:center">Surf</div>

<div class="user-input">
	<form name="myform" id="myform" action="overwrite" method="post" enctype="multipart/form-data">
		<div class="form-group">
			<div>
				<p style="margin: 0px; font-weight: 600; padding-bottom: 4px">Warning: You are overwriting an existing dataset!<br>Do you wish to continue?</p>	
			</div>
        	<br>
			<input id="overwrite" type="radio" name="overwrite" value="true" checked="checked"> Yes<br>
			<input id="overwrite" type="radio" name="overwrite" value="false"> No<br>
			<input style="visibility: hidden" id="filename" name="filename" value="<%=request.getAttribute("filename")%>">
			<input style="visibility: hidden" id="head" name="head" value="<%=request.getAttribute("head")%>">
    		<input style="visibility: hidden" id="desc" name="desc" value="<%=request.getAttribute("desc")%>">
		</div>
		<div class="submit-button">
			<button type="submit" class="btn btn-default" style="margin: 5px">Submit</button>
		</div>
		<br>
	</form>
</div>						
</body>
</html>