

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.*;

public class UserServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String mail = request.getParameter("email");
        String phone_no = request.getParameter("phone");
        String gender = request.getParameter("gender");
        String country = request.getParameter("country");
        PreparedStatement check = null;
        PreparedStatement insert = null;
        Connection con = null;
        
        
        try {
            con = JDBCConnection.getConnection();
            String checkSql = "select count(*) from users where name = ?";
            check = con.prepareStatement(checkSql);
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            
            
            JSONObject jsonResponse = new JSONObject();
            PrintWriter out = response.getWriter();
            
            
            if (rs.next() && rs.getInt(1) > 0) {
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "User name already exists");
                out.print(jsonResponse.toString());
                out.flush();
            }
            else {
                String insertSql = "insert into users (name, mail, password, phone_no, gender, country, created_at) values (?, ?, ?, ?, ?, ?, NOW())";
                insert = con.prepareStatement(insertSql);
                insert.setString(1, name);
                insert.setString(2, mail);
                insert.setString(3, password);
                insert.setString(4, phone_no);
                insert.setString(5, gender);
                insert.setString(6, country);
                insert.executeUpdate();
                
                
                String res = "insert into login (user_name, password) values (?, ?)";
                PreparedStatement p = con.prepareStatement(res);
                p.setString(1, name);
                p.setString(2, password);
                p.executeUpdate();
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "User added successfully.");
                out.print(jsonResponse.toString());
                out.flush();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
}
