import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;


public class GetConversation extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            int id = Integer.parseInt(request.getParameter("id"));
                
        try  {
                 JSONArray jsonArray = new JSONArray();
                 HttpSession session = request.getSession(false);
                 int tid = (int)session.getAttribute("ticket_id");
                 String s = "select parent_id,child_id,from_mail,to_mail,message from thread where ticket_id=?";
                 PreparedStatement p = con.prepareStatement(s);
                 p.setInt(1, tid);
                 ResultSet rs = p.executeQuery();
                 Map<String,List<String>> data = new HashMap<>();
                 while(rs.next()){
                    List<String> list = new ArrayList<>();
                    list.add(Integer.toString(rs.getInt(1)));
                    list.add(Integer.toString(rs.getInt(2)));
                    list.add(rs.getString(3));
                    list.add(rs.getString(4));
                    list.add(rs.getString(5));
                    data.put(Integer.toString(rs.getInt(2)),list);
                 }

                 int pid = 0;

                //  String st = "select parent_id from thread where child_id=?";
                //  PreparedStatement pt = con.prepareStatement(st);
                //  pt.setInt(1, id);
                //  ResultSet rs1 = pt.executeQuery();
                //  if(rs1.next()){
                //     pid = rs1.getInt(1);
                //  }
                 int count = 0;
                 while(true){
                     List<String> l = data.get(Integer.toString(id));
                     pid = Integer.parseInt(l.get(0));
                     if(count != 0){
                         Map<String, String> map = new HashMap<>();
                         map.put("from_mail",l.get(2));
                         map.put("to_mail", l.get(3));
                         map.put("message", l.get(4));

                        String iq = "select image from image where child_id = ?";
                        PreparedStatement im = con.prepareStatement(iq);
                        im.setInt(1, id);
                        ResultSet ir = im.executeQuery();


                        if (ir.next()) {
                            System.out.println("image");
                            byte[] imageBytes = ir.getBytes("image");
                            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                            map.put("image", base64Image); 
                        }
                        JSONObject jsonObject = new JSONObject(map);
                        jsonArray.put(jsonObject);
                     }
                        if(pid == id){
                            break;
                        }
                    id = pid;
                    count++;
                }

                 //System.out.print(jsonArray);
                 response.setContentType("application/json");
                 response.setCharacterEncoding("UTF-8");
         
                 response.getWriter().write(jsonArray.toString());
            }

         catch (Exception e) {
            e.printStackTrace();
        }
    }
}
