package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.Map;
import java.io.File;

import java.nio.charset.Charset;
import org.apache.commons.csv.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.*;
import javax.servlet.http.Part;

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
/* Prashanthi (13/01/20): have to remove packages we don't require */

@MultipartConfig
public class customServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.customServlet.class);

    public customServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");

        // create the surf home dir if it doesn't exist
        if (!(new File(Config.SURF_HOME).exists()))
        {
            new File(Config.SURF_HOME).mkdirs();
            new File(Config.SURF_HOME+File.separator+"Temp").mkdirs();
        }
        request.getRequestDispatcher("custom-dataset.jsp").include(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        String description = request.getParameter("desc");
        String IDcolumn = request.getParameter("uid"); /*added by Prashanthi on 09/01/2020*/


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
                //https://stackoverflow.com/questions/11838674/how-to-read-property-name-with-spaces-in-java
                //Config.java line 238 stringPropertyNames takes whitespaces, '=' and ';' as delimiters.
                //So regex replace everything other than alphabets, numbers and dots to be safe
                filename=str.substring(str.indexOf("=")+2,str.length()-1).replaceAll("[^a-zA-Z0-9.]", "_");
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
            request.getRequestDispatcher("warning.jsp").forward(request, response);
            return;
        }

        filePart.write(fileToWrite);

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
                 if(key.equals(IDcolumn))
                {
                    key = Config.ID_FIELD;
                }

                firstLine+=key;
                if (flag < length - 1)
                    firstLine = firstLine + ",";
                flag++;
            }

        Scanner sc = new Scanner(new File(fileToWrite));
        
        StringBuffer lines = new StringBuffer();
        lines.append(firstLine+System.lineSeparator());

        sc.nextLine();
        while (sc.hasNextLine()) {
            lines.append(sc.nextLine()+System.lineSeparator());
        }
        String fileContents = lines.toString();
        fileContents = fileContents.substring(0, fileContents.length() - 1); // Prashanthi (11/01/20): to truncate trailing new line from file. do we need this? 
        sc.close();

        FileWriter writer = new FileWriter(fileToWrite);
        writer.append(fileContents);
        writer.flush();

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
        parse.close();

        if(IDcolumn.equals(""))
            Config.addDatasetToConfig(Config.SURF_HOME + File.separator + filename, description, filename, firstLine, "ID");
        else
            Config.addDatasetToConfig(Config.SURF_HOME + File.separator + filename, description, filename, firstLine, IDcolumn); 
       
        request.getRequestDispatcher("index.jsp").include(request, response);
    }

    public void destroy() {
    }
}