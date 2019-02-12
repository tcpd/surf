<%@ page import="in.edu.ashoka.surf.Config" %>
<!--Outside Views-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Surf</title>
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="/bootstrap/dist/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="/css/main.css">
	<link rel="stylesheet" type="text/css" href="/css/surf.css">

	<script src="/js/jquery-1.12.1.min.js"></script>
	<script type="text/javascript" src="/bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="/js/selectpicker.js"></script>
	<script src="/js/sanityCheck.js"></script>
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
		<input type="radio" id="head" name="head" value="true"> Yes<br>
		<input type="radio" id="head" name="head" value="false"> No<br>
		<br>
        <label for="desc">Description</label>
        <input class="form-control" type="text" placeholder="Description" id="desc" name="desc">
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