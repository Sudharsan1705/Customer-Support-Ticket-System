<!DOCTYPE html>
<html>
<head>
    <title>Add User</title>
</head>
<body>
    <h2>User</h2>
    <form action="adduser" method="post">
        Name: <input type="text" name="name" required><br>
        Email: <input type="text" name="email" required><br>
        Password: <input type="password" name="password" required><br>
        Phone No: <input type="text" name="phone_no"><br>
        Gender:
        <input type="radio" name="gender" value="Male" required> Male
        <input type="radio" name="gender" value="Female"> Female
        <input type="radio" name="gender" value="Not Interested"> Not Interested
        <br>
        Country: <input type="text" name="country"><br>
        <input type="submit" value="Add User">
    </form>
</body>
</html>
