<!--Outside Views-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Candidate Mapper</title>
	<base href="${pageContext.request.contextPath}">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<h1 class="text-center" style="padding-top:20px;">Candidate Mapper</h1>

<div class="user-input">
<form action="${pageContext.request.contextPath}/IncumbencyServlet" method="get" onsubmit="${pageContext.request.contextPath}/IncumbencyServlet">
	<div class="form-group">
		<label for="userName">Username:</label>
		<input type="text" class="form-control" id="userName" name="userName">
	</div>
	<div class="form-group">
		<label for="email">Email:</label>
		<input type="email" class="form-control" id="email" name="email">
	</div>
	<div class="form-group">
		<label for="state">Default State (Optional):</label>
		<input type="state" class="form-control" id="state" name="state">
		</label>
	</div>
	<div class="submit-button">
  <button type="submit" class="btn btn-default">Submit</button>
</div>
</form>
</div>						
</body>
</html>