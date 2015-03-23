<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>LSI project 1a</title>
</head>
<body>
<%
String message = (String) request.getAttribute("message");
out.println("<h1> "+ message + " </h1>"); %>

<form action=EnterServlet method="get">
<br/>
<br/>
	<input type="submit" name="replace" value="Replace"/>
	<input type="text" name="textbox" value="" maxlength="512"/>
	<br/>
	<input type="submit" name="refresh" value="Refresh"/>
	<br/>
	<input type="submit" name="logout" value="Logout"/>
<br/>
<br/>
<%
String cookie = (String) request.getAttribute("cookie");
String expirationTime = (String) request.getAttribute("expiration");

out.println(cookie); 
out.println("<br/>");
out.println(expirationTime);
%>	

</form>
</body>
</html>