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
        response.setContentType("text/html; charset=UTF-8");

        // create the surf home dir if it doesn't exist
        if (!(new File(Config.SURF_HOME).exists()))
            new File(Config.SURF_HOME).mkdirs();

        request.getRequestDispatcher("custom-dataset.jsp").include(request, response);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        String description = request.getParameter("desc");

        Part filePart = request.getPart("myfile");
        if(filePart==null || description==null)
        {
            log.warn("Filepart or descript is null.");
            doGet(request, response);
            return;
        }

        String filename = "";
        String details = filePart.getHeader("content-disposition");
        String values[] = details.split(";");
        for(String str: values)
        {
            if(str.trim().startsWith("filename"))
            {
                filename=str.substring(str.indexOf("=")+2,str.length()-1);
            }
        }

        // check that it is CSV, otherwise bail out
        if (!filename.toLowerCase().endsWith(".csv"))
        {
            log.warn("File type is not csv. File name="+filename.substring(filename.lastIndexOf(".")+1));
            doGet(request, response);
            return;
        }

        // write the file, checking first if we're overwriting
        String fileToWrite = Config.SURF_HOME + File.separator + filename;
        if (new File(fileToWrite).exists()) {
            log.warn ("Warning: overwriting existing file: " + fileToWrite);
        }

        filePart.write(fileToWrite);

        BufferedReader fileContent = new BufferedReader(new FileReader(Config.SURF_HOME + File.separator + filename));
        String firstLine = fileContent.readLine();
        firstLine = firstLine.replaceAll("\"", ""); // isn't this unsafe?
        fileContent.close();

        {
            boolean hasHeaders = Boolean.parseBoolean(request.getParameter("head"));
            if (!hasHeaders) {
                log.warn("Warning: no headers, creating dummy ones for file " + filename);
                String colNames[] = firstLine.split(",");
                firstLine = "";
                for (int i = 0; i < colNames.length; i++) {
                    firstLine = firstLine + "col" + Integer.toString(i);
                    if (i < colNames.length - 1)
                        firstLine = firstLine + ",";
                }
            }
        }

        Config.addDatasetToConfig(Config.SURF_HOME + File.separator + filename, description, filename, firstLine);
        request.getRequestDispatcher("index.jsp").include(request, response);
	}

	public void destroy() {
	}
}