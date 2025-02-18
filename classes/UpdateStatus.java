

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;


import javax.servlet.ServletException;
import javax.servlet.http.*;
public class UpdateStatus extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection con = null;
		PrintWriter out = response.getWriter();
		JSONObject jsonResponse = new JSONObject();
	
		try {
			con = JDBCConnection.getConnection();
	
			int id = Integer.parseInt(request.getParameter("ticket_id"));
			String status = request.getParameter("status");
	
			HttpSession session = request.getSession(false);
			String portal = (String) session.getAttribute("portal");
			String name = (String) session.getAttribute("name");
			String role = (String) session.getAttribute("role");
	
			String s = "select count(*) from tickets where ticket_id=? and portal_name=?";
			PreparedStatement p = con.prepareStatement(s);
			p.setInt(1, id);
			p.setString(2, portal);
			ResultSet rs1 = p.executeQuery();
	
			if (rs1.next() && rs1.getInt(1) == 0) {
				jsonResponse.put("status", "success");
				jsonResponse.put("message", "Ticket does not belong to this portal");
			} else {
				String t = "select count(*) from customer_feedback where ticket_id=?";
				PreparedStatement tp = con.prepareStatement(t);
				tp.setInt(1, id);
				ResultSet tr = tp.executeQuery();
	
				if (tr.next() && tr.getInt(1) == 1) {
					jsonResponse.put("status", "success");
					jsonResponse.put("message", "Ticket has already been closed");
				} else {
					String s1 = "select count(*) from admin_assign where ticket_id=? and agent_name=?";
					PreparedStatement p1 = con.prepareStatement(s1);
					p1.setInt(1, id);
					p1.setString(2, name);
					ResultSet rs2 = p1.executeQuery();
	
					if (rs2.next() && rs2.getInt(1) == 0 && !role.equals("Admin") && !role.equals("super_admin")) {
						jsonResponse.put("status", "success");
						jsonResponse.put("message", "Ticket not assigned to this user");
					} else {
						String st = "select count(*) from status where status_name=?";
						PreparedStatement stmt = con.prepareStatement(st);
						stmt.setString(1, status);
						ResultSet rs = stmt.executeQuery();
	
						if (!rs.next() || rs.getInt(1) == 0) {
							String str = "insert into status (status_name) values (?)";
							PreparedStatement pt = con.prepareStatement(str);
							pt.setString(1, status);
							pt.executeUpdate();
						}
	
						String str = "update tickets set status_name=? where ticket_id=?";
						PreparedStatement statement = con.prepareStatement(str);
						statement.setString(1, status);
						statement.setInt(2, id);
						statement.executeUpdate();
	
						jsonResponse.put("status", "success");
						jsonResponse.put("message", "Status updated successfully");
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			System.out.println(jsonResponse.toString());
			out.print(jsonResponse.toString());
			out.flush();
		}
	}
}	