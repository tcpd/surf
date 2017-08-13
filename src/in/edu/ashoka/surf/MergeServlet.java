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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet implementation class MergeServlet
 */
@WebServlet("/merge")
public class MergeServlet extends HttpServlet {
	public static Log log = LogFactory.getLog(in.edu.ashoka.surf.Config.class);

	private static final long serialVersionUID = 1L;
	public final static long START_TIME = System.currentTimeMillis();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MergeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try{
            HttpSession session = request.getSession();
            MergeManager mergeManager = (MergeManager) session.getAttribute("mergeManager");
            Dataset dataset = (Dataset) session.getAttribute("dataset");
			if (mergeManager == null) {
				mergeManager = MergeManager.getManager(dataset, request.getParameter("algorithm"), request.getParameter("algo-arg"));
				session.setAttribute("mergeManager", mergeManager);
				mergeManager.addSimilarCandidates();

				// further split by split column if specified
				String splitColumn = request.getParameter("splitColumn");
				if (splitColumn.length() > 0) {
					List<Collection<Row>> result = new ArrayList<>();
					List<Collection<Row>> groupsList = mergeManager.listOfSimilarCandidates;
					for (Collection<Row> group : groupsList) {
						Multimap<String, Row> splitGroups = SurfExcel.split (group, splitColumn);
						for (String key: splitGroups.keySet())
							result.add (splitGroups.get(key));
					}
					mergeManager.listOfSimilarCandidates = result;
				}
			}

            //set defaults
            request.getRequestDispatcher("/table.jsp").forward(request, response);
        } catch (IOException e){
            request.getSession().invalidate();
            throw e;
        }
	}

	public void save (MergeManager manager, String file) {
        //Handle csv write after the page has been redirected to save waiting time
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public void destroy() {
		// Finalization code...
		Dataset.destroyTimer();
	}

	//Handles button pressed action for both save as well as force merge button
	private boolean saveButtonPressed(HttpServletRequest request){
		return (request.getParameter("submit")!=null && (request.getParameter("submit").equals("Save")||(request.getParameter("submit").equals("Force MergeServlet"))));
	}

	private boolean resetButtonPressed(HttpServletRequest request){
		return (request.getParameter("submit")!=null && request.getParameter("submit").equals("Reset"));
	}
	
	private boolean updateTable(HttpServletRequest request){
		boolean shouldSave=false;
		String [] userRows = request.getParameterValues("row");
		
		//Collect comment related information & Collect completion related information
		
		Map<String,String[]> parameterMap = request.getParameterMap();
		
		Map<String,String> commentMap = new HashMap<String,String>();
		Map<String,String> isDoneMap = new HashMap<String,String>();
		for(String name:parameterMap.keySet()){
			if(name.contains("commentParam")){
					commentMap.put(name.substring(12),parameterMap.get(name)[0]);	//strip the key value before storing
				}
			
			if(name.contains("isDone")){
				isDoneMap.put(name.substring(7),parameterMap.get(name)[0]);	//strip the key value before storing
			}
		}
		
		MergeManager mergeManager = (MergeManager)request.getSession().getAttribute("mergeManager");
		
		if(userRows!=null && userRows.length>0){
			//isdone needs to be updated too on merge
			for(String row:userRows){
				isDoneMap.put(row, "on");
			}
			shouldSave = true;
		}
		
		//check whether rows have been marked for demerge; if yes,call the demerge method
		String [] rowsToBeDemerged = request.getParameterValues("demerges");
		//testing deMerge; Remove later
		//rowsToBeDemerged = new String[]{"26827","31908", "63686", "70245", "8576", "31906", "26815"};
		if(rowsToBeDemerged!=null){
			shouldSave = true;
		}
		
		//returns true if save needs to be done
		return shouldSave;
	}

	/* if the request param datasetKey is present, loads the dataset and puts it in the session, otherwise does nothing. */
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
