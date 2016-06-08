<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" 
    import="in.edu.ashoka.lokdhaba.NameData" import="in.edu.ashoka.lokdhaba.NamePair"
    import="in.edu.ashoka.lokdhaba.ConcreteNameData" import="in.edu.ashoka.lokdhaba.Row"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Incumbency Checker</title>
</head>
<body>

<%!
public void checkValue(){
	//do nothing
	
}
%>

<%
NameData nameData = new ConcreteNameData();
nameData.initialize();
nameData.iterateNameData();
NamePair np = nameData.getNamePair();
while(np!=null){
	
	
	%>
	<h5>Are these candidates the same?</h5><br>
	<%=np.getName1String()%><br>
	<%=np.getName2String() %>
	
	<form action=<%=getServletName()%> method=goToNext>
		<input type="radio" name="issame" value="same">Same</input>
		<input type="radio" name="issame" value="different">Different</input>
		<input type="button" name="save" onclick="checkValue">Save</input>
		<input type="submit" name="next">Next</input>
	</form>

<% 	
	np = nameData.getNamePair();
	continue;
}
%>

</body>
</html>
