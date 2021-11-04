<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>
<c:if test="${sessionScope.mvo eq null }">
	<form action="" method="post">
		ID:<input type="text" name="id"/><br/>
		PW:<input type="password" name="pw"/><br/>
		<input type="button" value="LOGIN"/>
	</form>
	
<a href="https://kauth.kakao.com/oauth/authorize?client_id=a5b66323922803723c655f41824d1a38&redirect_uri=http://localhost:9090/kakao/login&response_type=code">	
	<img src="resources/img/kakao_login.png"/>
</a>	
</c:if>	

</body>
</html>
