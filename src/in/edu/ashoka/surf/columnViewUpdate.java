package in.edu.ashoka.surf;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.*;

import in.edu.ashoka.surf.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@MultipartConfig
public class columnViewUpdate extends HttpServlet {

    private static Log log = LogFactory.getLog(in.edu.ashoka.surf.columnViewUpdate.class);
    public columnViewUpdate() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        // String colToSortBy = "";
        boolean flag = false;
        String datasetKey = (String) session.getAttribute("datasetKey");
        // for (String col: Config.actualColumns.get(datasetKey))
        // {
        //     if(request.getParameter(col+"Sort")!=null)
        //     {
        //         colToSortBy = colToSortBy + col + ",";
        //         flag = true;
        //     }
        // }
        // if(flag)
        // {
        //     Config.sortColumns = new String[colToSortBy.split(",").length];
        //     Config.sortColumns = colToSortBy.split(",");
        // }
        // else
        //     Config.sortColumns = Config.actualSortColumns.get(datasetKey).toArray(new String[Config.actualSortColumns.get(datasetKey).size()]);
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
        request.getRequestDispatcher("table.jsp").include(request, response);
	}

	public void destroy() {
	}
}