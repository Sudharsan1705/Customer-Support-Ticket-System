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
    <h2>Portals</h2>
    


    <div id="portal-list"></div>

    <script>
        function loadPortals() {
            $.ajax({
                url: 'portal', 
                type: 'GET',
                dataType: 'json',
                success: function(response) {
                    if (response && response.length > 0) {
                        var html = '<ul>';
                        for (var i = 0; i < response.length; i++) {
                            var portalName = response[i].portal_name;
                            html += '<li>' + portalName + 
                                    ' <button type="button" name="portal" onclick="portalButtonClick(this)" value="'+portalName+'"> Go to ' + portalName + '</button></li><br>';
                        }
                        html += '</ul>';
    
                        document.getElementById('portal-list').innerHTML = html;
                    } else {
                        document.getElementById('portal-list').innerHTML = 'No portals found.';
                    }
                }
            });
        }
        
        $(document).ready(function() {
            loadPortals(); 
        });
        
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
    </script>
    
</body>
</html>
