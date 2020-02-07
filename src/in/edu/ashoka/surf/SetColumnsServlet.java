package in.edu.ashoka.surf;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.*;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileWriter;
import java.io.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Scanner;

@MultipartConfig
public class SetColumnsServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(SetColumnsServlet.class);
    public SetColumnsServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.getRequestDispatcher("index.jsp").include(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String colToSortBy = "";
        boolean flag = false;


        String datasetKey = (String) session.getAttribute("datasetKey");
        log.info ("Loading dataset with key " + datasetKey);

        for (String col: Config.actualColumns.get(datasetKey))
        {
            if(request.getParameter(col+"Sort")!=null)
            {
                colToSortBy = colToSortBy + col + ",";
                flag = true;
            }
        }

        if(flag)
        {
            Config.sortColumns = new String[colToSortBy.split(",").length];
            Config.sortColumns = colToSortBy.split(",");
        }
        else
            Config.sortColumns = Config.actualSortColumns.get(datasetKey).toArray(new String[0]);

        flag = false;
        String colsToShow = "";
        for (String col: Config.actualColumns.get(datasetKey))
        {
            if(request.getParameter(col)!=null)
            {
                colsToShow = colsToShow + col + ",";
                flag = true;
            }
        }

        if(flag)
        {
            Config.showCols = new String[colsToShow.split(",").length];
            Config.showCols = colsToShow.split(",");
        }
        else
            Config.showCols = Config.actualColumns.get(datasetKey).subList(0,1).toArray(new String[1]);


/*********** added by Prashanthi for updating SortBy in surf.properties (13/01/20) ***********/

        Properties props = Config.readProperties(); 

        Scanner sc = new Scanner(new File(Config.PROPS_FILE));
        
        StringBuffer lines = new StringBuffer();
        lines.append(System.lineSeparator());

        for (String key: props.stringPropertyNames()) {

            if(key.equalsIgnoreCase(datasetKey+"_SortBy"))
            {
                String sortColList = datasetKey + "_SortBy=" + colToSortBy.substring(0, (colToSortBy.length() - 1)); //P: to remove the trailing ","
                lines.append(sortColList+System.lineSeparator());
                continue; 
            }
            if(key.equalsIgnoreCase(datasetKey+"_Path"))
            {
                String pathLabel = datasetKey + "_Path=" + props.getProperty(key).replaceAll("\\\\", "\\\\\\\\");
                lines.append(pathLabel+System.lineSeparator());
                continue;
            }
            lines.append(key + "=" + props.getProperty(key)+System.lineSeparator());
        }

        String fileContents = lines.toString();
        fileContents = fileContents.substring(0, fileContents.length() - 1); // Prashanthi (13/01/20): to truncate trailing new line from file. do we need this? 
        sc.close();

        FileWriter writer = new FileWriter(Config.PROPS_FILE);
        writer.append(fileContents);
        writer.flush();

/*******************************************************************************************/

        request.getRequestDispatcher("select-op").forward(request, response);
    }

    public void destroy() {
    }
}