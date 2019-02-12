package in.edu.ashoka.surf;

import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.*;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MultipartConfig
public class downloadServlet extends HttpServlet {
    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.downloadServlet.class);

    public downloadServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        HttpSession session = request.getSession();
        String key = (String) session.getAttribute("datasetKey");
        response.setHeader("Content-disposition", "attachment; filename="+key+"_surf.csv");
        if(!(new File(System.getProperty("user.home")+File.separator+"Surf Data"+File.separator+key+".csv").exists()))
            request.getRequestDispatcher("table.jsp").include(request, response);
        else
        {
            //https://www.baeldung.com/servlet-download-file
            FileInputStream file = new FileInputStream(System.getProperty("user.home")+File.separator+"Surf Data"+File.separator+key+".csv");
            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[512];
            int numBytesRead;
            while ((numBytesRead = file.read(buffer)) > 0) 
                out.write(buffer, 0, numBytesRead);
            file.close();
            out.close();
        }
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
	}

	public void destroy() {
	}
}
