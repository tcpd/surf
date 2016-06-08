<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="in.edu.ashoka.lokdhaba.NameData" import="in.edu.ashoka.lokdhaba.NamePair"
    import="in.edu.ashoka.lokdhaba.ConcreteNameData" import="java.util.Iterator"
    import="in.edu.ashoka.lokdhaba.TestNameData"%>
    
    <%@ taglib
    prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Incumbency Checker</title>
</head>


<body>



<h5>Are these candidates the same?</h5><br>
		
		
		<c:out value="${sessionScope.namePair.getName1String()}" /><br>
		<c:out value="${sessionScope.namePair.getName2String()}" />
		
		
		
		<form action="Incumbency" method="post">
			<input type="radio" name="issame" value="same">Same
			<input type="radio" name="issame" value="different">Different
			<input type="radio" name="issame" value="unsure">Unsure
			<input type="submit" name="save" onclick="checkValue" value="Save & Next">
			<input type="submit" name="next" value="Next">
		</form>
	

</body>
</html>
