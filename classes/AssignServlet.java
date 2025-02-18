

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class AssignServlet extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 int id =Integer.parseInt(request.getParameter("ticket_id")) ;
		 String agent = request.getParameter("agent");
	     Connection con = null;
	     try {
	            con = JDBCConnection.getConnection();

               HttpSession session = request.getSession(false);
			   String name = (String) session.getAttribute("name");
			   String portal = (String) session.getAttribute("portal");
			   
			   JSONObject jsonResponse = new JSONObject();
			   PrintWriter out = response.getWriter();
			   String st = "select count(*) from tickets where ticket_id=? and portal_name=?";
			   PreparedStatement p = con.prepareStatement(st);
			   p.setInt(1, id);
			   p.setString(2, portal);
			   ResultSet rs = p.executeQuery();
			   
			   if(rs.next() && rs.getInt(1) == 0){
				   jsonResponse.put("status", "success");
				   jsonResponse.put("message", "Ticket Not belong to this portal");
				   out.print(jsonResponse.toString());
				   out.flush();
			   }
			   else{
				String t = "select count(*) from customer_feedback where ticket_id=?";
				PreparedStatement tp = con.prepareStatement(t);
				tp.setInt(1, id);
				ResultSet tr = tp.executeQuery();

				if(tr.next() && tr.getInt(1) == 1){
					jsonResponse.put("status", "success");
					jsonResponse.put("message", "Ticket has been closed");
					out.print(jsonResponse.toString());
					out.flush();
				}
               else{
			   String st1 = "select count(*) from portal_assign where user_name=? and portal_name=? and role_name=?";
			   PreparedStatement p1 = con.prepareStatement(st1);
			   p1.setString(1, agent);
			   p1.setString(2, portal);
			   p1.setString(3, "Agent");
			   ResultSet rs1 = p1.executeQuery();

			   System.out.println(agent+" "+portal);
			   
			   if(rs1.next() && rs1.getInt(1) == 0){
				   jsonResponse.put("status", "success");
				   jsonResponse.put("message", "User Invalid");
				   out.print(jsonResponse.toString());
				   out.flush();
			   }
               else{
				   String st3 = "select count(*) from admin_assign where ticket_id=? and agent_name=?";
				   PreparedStatement stmt = con.prepareStatement(st3);
				   stmt.setInt(1, id);
				   stmt.setString(2, agent);
				   ResultSet rs2 = stmt.executeQuery();

				    if(rs2.next() && rs2.getInt(1) == 1){
						jsonResponse.put("status", "success");
						jsonResponse.put("message", "Ticket already assigned");
						out.print(jsonResponse);
						out.flush();
					}
					else{
						String st4 = "select count(*) from admin_assign where ticket_id=?";
				        PreparedStatement pt = con.prepareStatement(st4);
				        pt.setInt(1, id);
				        ResultSet rs3 = pt.executeQuery();
						
					if(rs3.next() && rs3.getInt(1) == 1){
						    System.out.println("Entered");
						    String up = "update admin_assign set agent_name=?,admin_name=? where ticket_id=?;";
							PreparedStatement update = con.prepareStatement(up);
							update.setString(1, agent);
							update.setString(2, name);
							update.setInt(3, id);
							int count = update.executeUpdate();
							
							System.out.println(count);
							
						    String up1 = "update ticket_time set admin_name=?,finish_time=? where ticket_id=?;";
							PreparedStatement update1 = con.prepareStatement(up1);
							String stt = "select days from configuration where sla_id = 2";
						    Statement sta = con.createStatement	();
						    ResultSet rt = sta.executeQuery(stt);
						    rt.next();
						    int day = rt.getInt(1);
							
							update1.setString(1, name);
							
							LocalDate currentDate = LocalDate.now();
							LocalDate newDate = currentDate.plusDays(day);
							update1.setString(2, newDate.toString());
							update1.setInt(3, id);

							int count1 = update1.executeUpdate();
							System.out.println(count1);
							
							
							jsonResponse.put("status", "success");
							jsonResponse.put("message", "Ticket Assigned Successfully");
							out.print(jsonResponse);
							out.flush();
					}
					else{

						
						String s = "insert into admin_assign (ticket_id,admin_name,agent_name) values (?,?,?)";
						PreparedStatement statement = con.prepareStatement(s);
						statement.setInt(1, id);
						
						statement.setString(2, name);
						statement.setString(3, agent);
						statement.executeUpdate();
						
						String str = "update tickets set status_name = ? where ticket_id = ?";
						PreparedStatement stm = con.prepareStatement(str);
						stm.setString(1, "Onhold");
						stm.setInt(2, id);
						stm.executeUpdate();
						
						String res = "insert into ticket_time (ticket_id,admin_name,finish_time) values (?,?,?)";
						PreparedStatement stmt1 = con.prepareStatement(res);
						stmt1.setInt(1, id);
						stmt1.setString(2, name);
						
						
						
						
						String stt = "select days from configuration where sla_id = 2";
						Statement sta = con.createStatement	();
						ResultSet rt = sta.executeQuery(stt);
						rt.next();
						int day = rt.getInt(1);
						
						
						LocalDate currentDate = LocalDate.now();
						LocalDate newDate = currentDate.plusDays(day);
						stmt1.setString(3, newDate.toString());
						stmt1.executeUpdate();
						
						
						jsonResponse.put("status", "success");
						jsonResponse.put("message", "Ticket Assigned Successfully");
						out.print(jsonResponse);
						out.flush();
					}
			}
			}
		}
		}
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
