package in.edu.ashoka.surf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.gson.JsonObject;

/* servlet called when a new algorithm is first run */
public class MergeServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeServlet.class);

    public MergeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * 2 modes:
     * 1) runs the algorithm and puts up a new mergemanager in the session. assumes dataset is already in the session.
	 * 2) just refreshes filter without rerunning the algorithm
     * uses the request params algorithm, algo-arg, splitColumn, filterSpec, sortOrder.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject result = new JsonObject();
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        try {
            if (request.getParameter ("filterOnly") == null) {
                // set up a new merge manager first
                Dataset dataset = (Dataset) session.getAttribute("dataset");
                MergeManager mergeManager = new MergeManager(dataset, request.getParameter("algorithm"), request.getParameter("algo-arg"), request.getParameter("splitColumn"));
                session.setAttribute("mergeManager", mergeManager);

                // call this last, because we should ALWAYS merge based on id's even if it doesn't honor splitColumn
                mergeManager.updateMergesBasedOnIds();
            }

            MergeManager mergeManager = (MergeManager) session.getAttribute("mergeManager");

            // we have to merge based on ids to honor existing merges that may already have been done on the dataset
            MergeManager.View view = mergeManager.getView(request.getParameter("filterSpec"), request.getParameter("sortOrder"));
            session.setAttribute("view", view);

            result.addProperty ("status", 0);
        } catch (Exception e){
            result.addProperty ("status", 1);
            result.addProperty ("message", e.getClass().getName() + "}"); // TODO: add detailed error message
        }
        response.getOutputStream().print(result.toString());
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
            d.description = Config.keyToDescription.get(datasetKey);
            log.info ("Dataset read from path " + path + " with " + d.getRows().size() + " rows");
            HttpSession session = request.getSession();
            session.setAttribute("dataset", d);
            session.setAttribute ("currentFile", path);
            return d;
        }
        return null;
	}
}
