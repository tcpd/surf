package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Multimap;
import com.sun.scenario.effect.Merge;
import edu.stanford.muse.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.gson.JsonObject;

/* servlet called when a new algorithm is first run */
public class MergeServlet extends HttpServlet {
	public static Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeServlet.class);
	private static final long serialVersionUID = 1L;

    public MergeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * runs the algorithm and puts up a new mergemanager in the session. assumes dataset is already in the session.
	 * uses the request params algorithm, algo-arg, splitColumn, filterSpec, sortOrder.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject result = new JsonObject();
        try {
        	// set up a new merge manager first
            HttpSession session = request.getSession();
            Dataset dataset = (Dataset) session.getAttribute("dataset");
			MergeManager mergeManager = new MergeManager(dataset, request.getParameter("algorithm"), request.getParameter("algo-arg"));
			session.setAttribute("mergeManager", mergeManager);

			// split by column
			mergeManager.splitByColumn(request.getParameter ("splitColumn"));

            // we have to merge based on ids to honor existing merges that may already have been done on the dataset
			MergeManager.View view = mergeManager.getView(request.getParameter ("filterSpec"), request.getParameter ("sortOrder"));
			session.setAttribute ("view", view);

            result.addProperty ("status", 0);
        } catch (Exception e){
            result.addProperty ("status", 1);
            result.addProperty ("message", e.getClass().getName() + "}"); // TODO: add detailed error message
        }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public void destroy() {
		// Finalization code...
		Dataset.destroyTimer();
	}

	/* if the request param datasetKey is present, loads the dataset and puts it in the session, otherwise does nothing.
	 * no reason this method is in this class, it could be anywhere */
	public static Dataset loadDataset(HttpServletRequest request) throws IOException{
	    String datasetKey = request.getParameter ("datasetKey");
	    if (datasetKey != null) {
			String path = Config.keyToPath.get(datasetKey);
            Dataset d = Dataset.getDataset(path);
            log.info ("Dataset read from path " + path + " with " + d.getRows().size() + " rows");
            HttpSession session = request.getSession();
            session.setAttribute("dataset", d);
            session.setAttribute ("currentFile", path);
            return d;
        }
        return null;
	}
}
