

import javax.servlet.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.http.*;


public class Index extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String role = request.getParameter("role");
        HttpSession ss = request.getSession();
        ss.setAttribute("role",role);
        System.out.println(role);
        response.sendRedirect("Login.jsp");
    }
}
