package in.edu.ashoka.surf;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.FileInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MultipartConfig
public class DeleteDatasetServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(DeleteDatasetServlet.class);

    public DeleteDatasetServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("index.jsp").include(request, response);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        String toDel = request.getParameter("dataset");
        Properties props = new Properties();
        String propsFile = Config.PROPS_FILE;
        File f = new File(propsFile);
        if (f.exists() && f.canRead()) {
            try {
                InputStream is = new FileInputStream(propsFile);
                props.load(is);
                is.close();
            } catch (Exception e) {
                log.warn("Error reading Surf properties file " + propsFile + " " + e);
            }
            String delPath = "";
            boolean flag = false;
            for (String key: props.stringPropertyNames()) {
                if(key.substring(0, key.lastIndexOf("_")).equals(toDel))
                {
                    if(key.endsWith("_Path"))
                    {
                        delPath = props.getProperty(key);
                    }
                    flag = true;
                    props.remove(key);
                }
            }
            if(flag)
            {
                try
                {
                    PrintWriter out = new PrintWriter(new FileWriter(propsFile), true);
                    Files.lines(f.toPath()).filter(line -> !line.contains(toDel)).forEach(out::println);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    log.warn("Error deleting from props file\nException: "+e);
                }
            }
            File oldfile = new File(delPath);
            log.warn("Deleted? "+ oldfile.delete());
        }
        Config.createDatasets();
        request.getRequestDispatcher("index.jsp").include(request, response);
	}

	public void destroy() {
	}
}