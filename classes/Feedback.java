

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class Feedback extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  Connection con = null;
	     try {
	            con = JDBCConnection.getConnection();
				int id = Integer.parseInt(request.getParameter("id"));
                String str = request.getParameter("feedback");

				System.out.println(id+" "+str);
				String st = "select count(*) from feedback where rating_name=?";
	            PreparedStatement stmt = con.prepareStatement(st);
				stmt.setString(1, str);
				ResultSet rs = stmt.executeQuery();
				rs.next();

                int cnt = rs.getInt(1);
				if(cnt == 0){
				String s = "insert into feedback (rating_name) values (?)";
	            PreparedStatement statement = con.prepareStatement(s);
				statement.setString(1, str);
				statement.executeUpdate();
			    }
			
			String res = "insert into customer_feedback (ticket_id,rating_name) values (?,?)";
			PreparedStatement p = con.prepareStatement(res);
			p.setInt(1, id);
			p.setString(2, str);
			p.executeUpdate();

				
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                con.close();
	            } catch (SQLException e) {
	            	e.printStackTrace();
	            }
	        }
	}

}
