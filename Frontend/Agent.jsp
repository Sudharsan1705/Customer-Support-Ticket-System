<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>dashboard</title>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script><style>
        .form{
            display: flex;
            flex-direction: row;
            gap: 10px;
        }
        .form1{
            display: flex;
            flex-direction: row;
            gap: 30px;
        }
        .formContainer {
            display: flex;
            flex-direction: row; 
            gap: 20px; 
        }
        /* .view{
            margin-left: 30px;
        } */
        #ticket, #conversation {
            padding: 10px; 
            box-sizing: border-box; 
        }
        .scroll{
            width: 1000px;
            height: 400px;
            overflow-y: scroll;
            position: relative;
        }

    </style>
</head>
<body>
    <div class="form">
        <form action="index.jsp" method="post">
            <button type="submit">Login</button>
        </form>
        <br>
        <form action="enterportal" method="post">
            <button type="submit">Portals</button>
        </form>
        <br>
        <form action="logout" method="post">
            <button type="submit">Logout</button>
        </form>
    </div>
    <div class="form1">
        <div id="getstatus"></div>
        
        <div id="getfeedback"></div>  
    </div>
    
    <button type="button" onclick="status()">Update Status</button>
    
    <button type="button" onclick="query()">Query Ticket</button>
    
    <button type="button" onclick="viewTicket()">View Ticket</button>

    <div id="formContainer" class="formContainer">
        <div id="ticket"></div>
        <div id="conversation"></div>
    </div>

</body>

<script>
        var tid = 0;
        const eventSource = new EventSource('http://localhost:8080/MyApp/test');

            eventSource.onmessage = function(event) {
                console.log(event);
            if(tid != 0)
            handleTicket(tid);
            };


        function loadPortals() {

            console.log('entered');

            $.ajax({
                url: 'getstatus', 
                type: 'GET',
                dataType: 'json',
                success: function(response) {
                    var html = '<h3>Status</h3><p>';
                    if (response && response.length > 0) {
                            for (var i = 0; i < response.length; i++) {
                                var status = response[i].status;
                                var count  = response[i].count;
                                html += '<p><i>' + status + '    -  '+count+'</i></p>';
                            }
                        } 
                        html += '</p>';    
                        document.getElementById('getstatus').innerHTML = html;
                    }
                });
                
                $.ajax({
                    url: 'getfeedback', 
                    type: 'GET',
                    dataType: 'json',
                    success: function(response) {
                        var html = '<h3>Feedback</h3>';
                        if (response && response.length > 0) {
                            html += '<p>';
                                for (var i = 0; i < response.length; i++) {
                                    var feedback = response[i].feedback;
                                    var count  = response[i].count;
                              html += '<p><i>' + feedback + '    -  '+count+'</i></p>';
                        }
                        html += '</p>';
                    }
                    document.getElementById('getfeedback').innerHTML = html;
                }
            });
        }
        
        $(document).ready(function() {
            loadPortals(); 
        });
            
        function viewTicket() {
                const formContainer = document.getElementById('formContainer');
                formContainer.innerHTML = `
                   <div id="ticket"></div>
                   <div id="conversation"></div>
                    `;
                console.log('in');
                $.ajax({
                    url: 'viewticket', 
                    type: 'GET',
                    dataType: 'json',
                    success: function(response) {
                        if (response && response.length > 0) {
                            var html = '<h4>Tickets</h4>'; 
                            for (var i = 0; i < response.length; i++) {
                                var id = response[i].ticket_id;
                                var subject = response[i].subject;
                                var status = response[i].status;
                                html += '<p><button id="ticketid"  onclick="handleTicket(' + id + ')">'+id+' - ' + subject + ' - ' + status + '</button></p>';
                            }
                            console.log(document.getElementById('ticket'))
                            
                            document.getElementById('ticket').innerHTML = html;
                        } else {
                            document.getElementById('formContainer').innerHTML = 'No ticket found.';
                        }
                    },      
                    error: function(error) {
                        console.log('Error fetching portal data');
                    }
                });
            }
            function handleTicket(ticket){
                console.log("in");
                tid = ticket;
                var id = ticket;
                console.log("ticket"+id);
                $.ajax({
                    url: 'conversation', 
                    type: 'GET',
                    data: { ticket_id: id}, 
                    dataType:'json',
                    success: function(response) {
                        if (response && response.length > 0) {
                            var html = '<h4>Conversation</h4>';
                            html += '<div class="scroll">'
                            for(let i=0;i<response.length;i++){
                                var fromMail = response[i].from_mail;
                                var toMail = response[i].to_mail;
                                var message = response[i].message;
                                var childId = response[i].child_id;
                                var image = response[i].image; 
                                console.log(image);

                            html+='<p><i>From:</i>'+fromMail+'</p>'
                            html+='<p><i>To:</i>'+toMail+'</p>'
                            if (image) {
                                html += '<p><i>Image:</i></p>';
                                html += '<img src="data:image/png;base64,' + image + '" alt="Image" style="max-width: 200px; max-height: 200px;" />';
                            }
                            html+='<p><i>Message:</i>'+message+'</p>'
                            html+='<button onclick="viewMessage('+childId+')" >View</button>'
                            html+='<button onclick="replyMessage('+childId+')" >Reply</button>'
                            html+='<div id="'+childId+'"></div><br>'
                        } 
                        document.getElementById("conversation").innerHTML = html;
                        } 
                    },      
                    error: function(error) {
                        console.log('Error fetching portal data');
                    }
                });
            }
            function viewMessage(childId){
                let conv = document.getElementById(childId);
                console.log(conv.style.display)
                if(conv.style.display === "none"){
                    conv.style.display = "block";
                    $.ajax({
                        url:'getconversation',
                        type:'get',
                        data:{id:childId},
                        dataType:'json',
                        success:function(response){
                            if (response && response.length > 0) {
                                var html = '<hr>';
                                for(let i=0;i<response.length;i++){
                                    var fromMail = response[i].from_mail;
                                    var toMail = response[i].to_mail;
                                    var message = response[i].message;
                                    var image = response[i].image; 
                                    
                                    html += '<div class="view">'
                                        html+='<p><i>From:</i>'+fromMail+'</p>'
                                        html+='<p><i>To:</i>'+toMail+'</p>'
                                        if (image) {
                                            html += '<p><i>Image:</i></p>';
                                            html += '<img src="data:image/png;base64,' + image + '" alt="Image" style="max-width: 200px; max-height: 200px;" />';
                            }
                            html+='<p><i>Message:</i>'+message+'</p><br>'
                            } 
                            html+='<hr>';
                            conv.innerHTML = html;
                        } else {
                            conv.innerHTML = 'No conversation found.';
                        }
                        }
                        
                    });
                }
                else{
                    conv.style.display = "none";
                }
            }

            function replyMessage(childId){
                console.log(childId)
                            var html = '</div><br>';
                            html+= `
                                <br>
                                <div>
                                <label for="to_mail">To:</label>
                                <input type="text" id="to_mail" name="to_mail" required><br><br>
                                
                                <label for="message">Message:</label>
                                <input type="text" id="message" name="message" required><br><br>

                                 <label for="image">Upload Image:</label>
                                 <input type="file" id="image" name="image" accept="image/*" required>
                                 `;
                                
                               html += '<button type="button" onclick=add("'+childId+'")>Submit</button>';
                               html+=` <div id="message"></div>
                                </div>
                            `;

                    console.log(document.getElementById(childId))
               
                document.getElementById(childId).innerHTML = html;
            }

             function add(childId){
                let id = childId
                let message = document.getElementById("message").value;
                let toMail = document.getElementById("to_mail").value;
                let image = document.getElementById("image").files[0];
                console.log(image)
                if(id == undefined){
                    id = 0;
                }
                let formData = new FormData();
                formData.append("id", id);
                formData.append("message", message);
                formData.append("toMail", toMail);
                if (image) {
                    formData.append("image", image);
                }
                console.log(message)
                console.log(toMail)
                console.log(id)
                 $.ajax({
                    url: 'addconversation', 
                    type: 'POST',
                    data: formData, 
                    processData: false,
                    contentType: false,
                    dataType:'json',
                   success:function(response){
                       console.log(response.ticket_id)
                       if(response && response.ticket_id)
                       handleTicket(response.ticket_id);
                   }
                });
            }

            function status(){
                
                const formContainer = document.getElementById('formContainer');
                
                formContainer.innerHTML = `
                <form id="update">
                    <br>
                    <label for="ticket_id">Ticket_id:</label>
                    <input type="text" id="ticket_id" name="ticket_id" required><br><br>
                    
                    <label for="status">Status:</label>
                    <input type="text" id="status" name="status" required><br><br>
                    
                    <button type="button" onclick="update()">Submit</button>
                    <div id="message"></div>
                    </form>
                    `;
                }
                
               async function update(){
                    const id = document.getElementById('ticket_id').value;
                    const status = document.getElementById('status').value;
                    
                   await $.ajax({
                        url: 'updatestatus', 
                        type: 'POST',
                        data: { ticket_id: id , status: status}, 
                        success: function(response) {
                            console.log(response)
                            if(response){
                                const messageContainer = document.getElementById('message');
                                messageContainer.innerHTML =response.message ;
                            }
                            else{
                                const messageContainer = document.getElementById('message');
                                messageContainer.innerHTML = 'Invalid ticket';
                            }
                        }
                    });
                    await loadPortals();
                }
                
                function query(){
                    const formContainer = document.getElementById('formContainer');
                    
                    formContainer.innerHTML = `
                <form id="discuss">
                    <br>
                    <label for="ticket_id">Ticket_id:</label>
                    <input type="text" id="ticket_id" name="ticket_id" required><br><br>
                    
                    <button type="button" onclick="discuss()">Submit</button>
                    <div id="message"></div>
                    </form>
                    `;
                }
                function discuss(){
                    let id = 0;
                    if(document.getElementById('ticket_id') != null)
                    id = document.getElementById('ticket_id').value;
                
                console.log(id)
                
                $.ajax({
                    url: 'query', 
                    type: 'get',
                    data: { ticket_id: id }, 
                    success: function(response) { 
                        var html = '<ul>';
                            if (response && response.length > 0) {
                                for (var i = 0; i < response.length; i++) {
                                    var name = response[i].user_name;
                                    var query = response[i].query;
                                    html += '<li>' + name + '    :  '+query+'</li>';
                                    }
                                }
                                html += '</ul><br>';
                                
                                html +=  `
                                <form >
                                    <br>
                                    <label for="discuss">Enter query:</label>
                                    <input type="text" id="discuss" name="discuss" required><br><br>
                                    
                                    <button type="button" onclick="addquery()">Submit</button>
                                        </form>
                                        `;
                                        document.getElementById('formContainer').innerHTML = html;
                                    },
                                    error:function(response){
                                        console.log(response.responseText);
                                        let res = JSON.parse(response.responseText); 
                                        console.log(res.message);
                                        const messageContainer = document.getElementById('message');
                                        messageContainer.innerHTML =res.message ;
                                    }
                                });
                    
            }

            async function addquery(){
                const dis = document.getElementById('discuss').value;
                  
                await $.ajax({
                   url:'discuss',
                   type:'post',
                   data: {discuss:dis}
                });
                 
                await discuss();
            }



    </script>
</html>
