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
<form onsubmit="return sanityCheck();" name="myform" id="myform" action="custom-dataset" method="post" enctype = "multipart/form-data">
	<div class="form-group">
		<div>
			<p style="margin: 0px; font-weight: 600; padding-bottom: 4px">Upload your dataset or Drag and Drop</p>
			<label class="btn btn-primary" for="myfile" style="background-color: white; color: #a59faf; border-color: #a59faf;">
			<input onchange="return sanityCheck2();" id="myfile" name= "myfile" type="file">
			</label>
		</div>
        <br/>
		<label for="head">Has Headers?</label><br>
		<input id="head" type="radio" name="head" value="true" checked="checked"> Yes<br>
		<input type="radio" name="head" value="false"> No<br>
		<br>
        <label for="desc">Description (required)</label>
        <input class="form-control" type="text" placeholder="Description" id="desc" name="desc">
        <br>
        <label for="uid">Enter the name of your ID column, if exists</label> 
        <input class="form-control" type="text" placeholder="ID column" id="uid" name="uid">
        <span class="help">You can skip this if your ID column is called "ID".</span>

	</div>
	<br/>
	<div class="submit-button">
		<button type="submit" class="btn btn-default">Submit</button>
	</div>
	<br/>
</form>
</div>						
</body>
</html>