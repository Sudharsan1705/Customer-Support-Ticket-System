<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>MyApp</title>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
</head>
<body>        
      <h2>Login</h2>
      <form >
          <button type="button" onclick="check(this)" value="portal" >Go to portal</button>
        </form>
    <br>
    <form action="login" method="post">
        <label for="username">Username:</label>
        <input type="text" id="username" name="username" required>
        <br><br>
        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>
        <br><br>
        <button type="button" onclick="check(this)" >Login</button>
    </form>
    <br>
    <div id="message"></div>
    <br>

</body>
<script>
    function check(button){
        var name = document.getElementById("username").value;
        var pass = document.getElementById("password").value;
        var val = button.value;
        $.ajax({
            url:'login',
            type:'post',
            data:{username:name,password:pass,value:val},
            success:function(data){
                if(data.redirect){
                    window.location.href = data.redirect;
                }
                else{
                    console.log(val)
                    if(val == "portal")
                    document.getElementById("message").innerHTML = "Cannot go to portal enter data.";
                    else
                    document.getElementById("message").innerHTML = "Invalid username or password.";
                }
            }
        })
    }
</script>
</html>
