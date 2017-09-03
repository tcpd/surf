<%@page isErrorPage="true" %>
<HTML>
<HEAD>
<head>
    <title>Surf Error!</title>
    <link rel="stylesheet" type="text/css" href="css/main.css">
    <link rel="stylesheet" type="text/css" href="css/surf.css">

</head>
<body>
<p>
    Sorry, Surf encountered an error!
<p>
Details:<br/>
<%= exception.getClass().getName() %> at <br/>
</p>
<pre>
    <%exception.printStackTrace (new java.io.PrintWriter(out));%>
</pre>

</body>
</html>