<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Portal</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        .form{
            display: flex;
            flex-direction: row;
            gap: 10px;
        }
    </style>
</head>
<body>
    <div class="form">
        <form action="index.jsp" method="post">
            <button type="submit">Login</button>
        </form>
        <br>
        <form action="logout" method="post">
            <button type="submit">Logout</button>
        </form>
    </div>

    <h3>Portal</h3>
    
    <button type="button" onclick="addportal()">Add Portal</button>
    
    <button type="button" onclick="enterportal()">Portals</button>

    <div id="formContainer"></div>

    <script>
        function enterportal() {
            $.ajax({
                url: 'portal', 
                type: 'GET',
                dataType: 'json',
                success: function(response) {
                    if (response && response.length > 0) {
                        console.log('in')
                        var html = '<ul>';
                        for (var i = 0; i < response.length; i++) {
                            var portalName = response[i].portal_name;
                            html += '<li>' + portalName + 
                                    ' <button type="button" name="portal" onclick="portalButtonClick(this)" value="'+portalName+'"> Go to ' + portalName + '</button></li><br>';
                        }
                        html += '</ul>';
    
                        document.getElementById('formContainer').innerHTML = html;
                    } else {
                        document.getElementById('formContainer').innerHTML = 'No portals found.';
                    }
                }
            });
        }
        
        function portalButtonClick(button) {
            var portalName = button.value;
            console.log(portalName);
            $.ajax({
                url: 'dashboard', 
                type: 'GET',
                data: { portal: portalName }, 
                success: function(response) { 
                    if (response.redirectUrl) {
                     window.location.href = response.redirectUrl; 
                    }
                },
                error: function(error) {
                    console.log('Error sending portal data');
                }
            });
        }
        
        function addportal() {
            const formContainer = document.getElementById('formContainer');
            
            formContainer.innerHTML = ` 
            <form id="form">
                <br>
                <label for="portal">Portal:</label>
                <input type="text" id="portal" name="portal" required><br><br>
                <label for="mail">Portal Mail:</label>
                <input type="text" id="mail" name="mail" required><br><br>
                
                <button type="button" onclick="portals()">Submit</button>
                <div id="message"></div>
                </form>
                `;
            }
            
            function portals() {
                const portal = document.getElementById('portal').value;
                const mail = document.getElementById('mail').value;
                console.log(mail)
                
                $.ajax({
                    url: 'addportal', 
                    type: 'POST',
                    data: { portal: portal,mail:mail },
                    success: function(response) {
                    let res = JSON.parse(response); 
                    console.log(res.message);
                    const messageContainer = document.getElementById('message');
                    messageContainer.innerHTML = res.message;
                }
            });
        }
    </script>
</body>
</html>
