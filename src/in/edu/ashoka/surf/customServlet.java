package in.edu.ashoka.surf;

import java.io.IOException;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.*;
import javax.servlet.http.Part;

import in.edu.ashoka.surf.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MultipartConfig
public class customServlet extends HttpServlet {
    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.customServlet.class);

    public customServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        if(!(new File(System.getProperty("user.home")+File.separator+"Surf Data").exists()))
            new File(System.getProperty("user.home")+File.separator+"Surf Data").mkdirs();
        request.getRequestDispatcher("custom-dataset.jsp").include(request, response);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        String descript = request.getParameter("desc");
        boolean headerFlag = Boolean.parseBoolean(request.getParameter("head"));
        Part filePart = request.getPart("myfile");
        if(filePart==null || descript==null)
        {
            log.warn("Filepart or descript is null.");
            doGet(request, response);
            return;
        }
        String name = "";
        String details = filePart.getHeader("content-disposition");
        String values[] = details.split(";");
        for(String str: values)
        {
            if(str.trim().startsWith("filename"))
            {
                name=str.substring(str.indexOf("=")+2,str.length()-1);
            }
        }
        if(!(name.substring(name.lastIndexOf(".")+1).trim().equalsIgnoreCase("csv")))
        {
            log.warn("File type is not csv. File name="+name.substring(name.lastIndexOf(".")+1));
            doGet(request, response);
            return;
        }
        filePart.write(System.getProperty("user.home")+File.separator+"Surf Data"+File.separator+name);
        BufferedReader fileContent = new BufferedReader(new FileReader(System.getProperty("user.home")+File.separator+"Surf Data"+File.separator+name));
        String headers = fileContent.readLine();
        headers = headers.replaceAll("\"", "");
        if(!headerFlag)
        {
            String colNames[] = headers.split(",");
            headers = "";
            for(int i=0; i<colNames.length; i++)
            {
                headers=headers+"col"+Integer.toString(i);
                if(i<colNames.length-1)
                    headers=headers+",";
            } 
        }
        log.warn(headers);
        fileContent.close();
        Config.updateConfig(System.getProperty("user.home")+File.separator+"Surf Data"+File.separator+name, descript, name, headers);
        request.getRequestDispatcher("index.jsp").include(request, response);
	}

	public void destroy() {
	}
}