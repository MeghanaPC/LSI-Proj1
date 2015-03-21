<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<script type="text/javascript">
function setButtonVal(id)
{
	clickedbutton=document.getElementById(id).value;
	document.getElementById("hiddenbutton").value=clickedbutton;
}
</script>
<body>
${message}<br><br>
<form action="ServletForSession" method="post">
<input type="submit" name="replace" id="replace" value="replace" onclick="setButtonVal(this.id)">
<input type="text" name="replacemessage" id="replacemessage" value=""> <br>
<input type="submit" name="refresh" id="refresh" value="refresh" onclick="setButtonVal(this.id)"><br>
<input type="submit" name="logout" id="logout" value="logout" onclick="setButtonVal(this.id)"><br>


<input type="hidden" name="hiddenbutton" id="hiddenbutton" value="default">
</form>
<br>
<br>
<p>Cookie Value(sessionID_versionNumber_locationMetadata): ${cookievalue}</p>
<p>Session will expire on : ${expirationtimestamp}</p>
</body>
</html>