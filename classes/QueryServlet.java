

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import javax.swing.plaf.synth.SynthStyle;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.*;
public class QueryServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection con = null;
	     try {
	            con = JDBCConnection.getConnection();

				HttpSession session = request.getSession(false);
				String portal = (String)session.getAttribute("portal");
				String role = (String)session.getAttribute("role");
				String name = (String)session.getAttribute("name");
                
				int id = 0;
				if(session.getAttribute("id") == null || Integer.parseInt(request.getParameter("ticket_id")) != 0){
					id = Integer.parseInt(request.getParameter("ticket_id"));
                    session.setAttribute("id",id);
				}
				else{
                    id = (int)(session.getAttribute("id"));
				}
				
				JSONObject jsonResponse = new JSONObject();
				PrintWriter out = response.getWriter();

				String t = "select count(*) from customer_feedback where ticket_id=?";
				PreparedStatement tp = con.prepareStatement(t);
				tp.setInt(1, id);
				ResultSet tr = tp.executeQuery();

				if(tr.next() && tr.getInt(1) == 1){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					jsonResponse.put("status", "error");
					jsonResponse.put("message", "Ticket has been closed");
					out.print(jsonResponse.toString());
					out.flush();
				}
                else{
				if(role.equals("Admin") || role.equals("super_admin")){

					String st = "select count(*) from tickets where ticket_id=? and portal_name=?";
					PreparedStatement p = con.prepareStatement(st);
					p.setInt(1, id);
					p.setString(2, portal);
					ResultSet rs1 = p.executeQuery();
					
					if(rs1.next() && rs1.getInt(1) == 0){
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						jsonResponse.put("status", "error");
						jsonResponse.put("message", "Ticket Not belong to this portal");
						out.print(jsonResponse.toString());
						out.flush();
						System.out.println(jsonResponse);
					}
					else{
						
						String s = "select user_name,query from disscussion where ticket_id = ? order by query_time asc";
						PreparedStatement statement = con.prepareStatement(s);
						statement.setInt(1, id);
						
						
						
						ResultSet rs = statement.executeQuery();
						JSONArray jsonArray = new JSONArray();
						while(rs.next()){
							Map<String, String> map = new HashMap<>();
							map.put("user_name", rs.getString(1));
							map.put("query", rs.getString(2));
							JSONObject jsonObject = new JSONObject(map);
							jsonArray.put(jsonObject);
						}
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						
						System.out.println(id);
						if (jsonArray.length() == 0) {
							response.getWriter().write("[]"); 
						}
						else{
							response.getWriter().write(jsonArray.toString());
						}
					}
				}
				else if(role.equals("Agent")){
					
					String st = "select count(*) from tickets where ticket_id in (select ticket_id from admin_assign where agent_name=? and ticket_id=?) and portal_name=?";
					PreparedStatement p = con.prepareStatement(st);
					p.setString(1, name);
					p.setInt(2, id);
					p.setString(3, portal);
					ResultSet rs1 = p.executeQuery();
					
					if(rs1.next() && rs1.getInt(1) == 0){
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						jsonResponse.put("status", "error");
						jsonResponse.put("message", "Ticket Not belong to this user");
						out.print(jsonResponse.toString());
						out.flush();
						System.out.println(jsonResponse);
					}
					else{
						String s = "select user_name,query from disscussion where ticket_id = ? order by query_time asc";
						PreparedStatement statement = con.prepareStatement(s);
						statement.setInt(1, id);
						
						ResultSet rs = statement.executeQuery();
						JSONArray jsonArray = new JSONArray();
						while(rs.next()){
							Map<String, String> map = new HashMap<>();
							map.put("user_name", rs.getString(1));
							map.put("query", rs.getString(2));
							JSONObject jsonObject = new JSONObject(map);
							jsonArray.put(jsonObject);
						}
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						
						System.out.println(id);
						if (jsonArray.length() == 0) {
							response.getWriter().write("[]"); 
						}
						else{
							response.getWriter().write(jsonArray.toString());
						}
					}
				}
				else{
					String st = "select count(*) from tickets where ticket_id=? and portal_name=? and customer_name=?";
					PreparedStatement p = con.prepareStatement(st);
					p.setInt(1, id);
					p.setString(2, portal);
					p.setString(3, name);
					ResultSet rs1 = p.executeQuery();
					
					if(rs1.next() && rs1.getInt(1) == 0){
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						jsonResponse.put("status", "error");
						jsonResponse.put("message", "Ticket Not is invalid");
						out.print(jsonResponse.toString());
						out.flush();
						System.out.println(jsonResponse);
					}
					else{
						String s = "select user_name,query from disscussion where ticket_id = ? order by query_time asc";
						PreparedStatement statement = con.prepareStatement(s);
						statement.setInt(1, id);
						
						ResultSet rs = statement.executeQuery();
						JSONArray jsonArray = new JSONArray();
						while(rs.next()){
							Map<String, String> map = new HashMap<>();
							map.put("user_name", rs.getString(1));
							map.put("query", rs.getString(2));
							JSONObject jsonObject = new JSONObject(map);
							jsonArray.put(jsonObject);
						}
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						
						System.out.println(id);
						if (jsonArray.length() == 0) {
							response.getWriter().write("[]"); 
						}
						else{
							response.getWriter().write(jsonArray.toString());
						}
					}
				}
			}
					
				} catch (Exception e) {
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
