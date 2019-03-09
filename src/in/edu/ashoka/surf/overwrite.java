package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.Map;
import java.io.File;

import java.nio.charset.Charset;
import org.apache.commons.csv.*;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MultipartConfig
public class overwrite extends HttpServlet {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.overwrite.class);

    public overwrite() {
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
        String filename = request.getParameter("filename");
        if(description==null)
        {
            log.warn("Descript is null.");
            doGet(request, response);
            return;
        }
        boolean overwrite = Boolean.parseBoolean(request.getParameter("overwrite"));
        String fileToWrite = Config.SURF_HOME + File.separator + "Temp" + File.separator + filename;
        File newfile = new File(fileToWrite);
        // write the file, checking first if we're overwriting
        if(overwrite)
        {
            if (newfile.exists()) {
                log.warn ("Warning: overwriting existing file: " + fileToWrite);
            }
            // File oldfile = new File(Config.SURF_HOME + File.separator + filename);
            // log.warn(oldfile.DeleteDatasetServlet());
            File destDir = new File(Config.SURF_HOME);
            FileUtils.copyFileToDirectory(newfile, destDir);
            newfile.delete();
            // BufferedReader fileContent = new BufferedReader(new FileReader(Config.SURF_HOME + File.separator + filename));
            String firstLine = "";
                CSVParser parse = CSVParser.parse(new File(Config.SURF_HOME + File.separator + filename), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
                Map<String, Integer> headers = parse.getHeaderMap();
                int length=headers.size();
                if(length==0)
                {
                    log.warn("0 columns!");
                    return;
                }
                int flag=0;
                for(String key : headers.keySet())
                {
                    firstLine+=key;
                    if (flag < length - 1)
                        firstLine = firstLine + ",";
                    flag++;
                }

            //firstLine = firstLine.replaceAll("\"", ""); // isn't this unsafe?
            //fileContent.close();
            {
                boolean hasHeaders = Boolean.parseBoolean(request.getParameter("head"));
                if (!hasHeaders) {
                    log.warn("Warning: no headers, creating dummy ones for file " + filename);
                    firstLine = "";
                    for (int i = 0; i < length; i++) {
                        firstLine = firstLine + "col" + Integer.toString(i);
                        if (i < length - 1)
                            firstLine = firstLine + ",";
                    }
                }
            }

            Config.addDatasetToConfig(Config.SURF_HOME + File.separator + filename, description, filename, firstLine);
            request.getRequestDispatcher("index.jsp").include(request, response);
        }
        else
        {
            newfile.delete();
            request.getRequestDispatcher("index.jsp").include(request, response);
        }
	}

	public void destroy() {
	}
}