package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Multimap;
import com.sun.scenario.effect.Merge;

/**
 * Servlet implementation class IncumbencyServlet
 */
@WebServlet("/IncumbencyServlet")
public class IncumbencyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//static boolean isFirst;
	//Dataset d;
	//MergeManager mergeManager;
	//filepaths
	//String currentFile;
	static Map<String, String> pathMap;
	static Map<String, String> descriptionMap;
	public final static long START_TIME = System.currentTimeMillis();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public IncumbencyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try{
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(60*60);
            //SETTING UP THE DATASET FOR MERGEMANAGER
            setUpDataset(request);
			setUpAlgorithm(request);

            //set up important parameters
            assignAttributes(request, session, "userName", "Name Not Specified",false);
            assignAttributes(request, session, "email", "email Not Specified",false);
            assignAttributes(request, session, "onlyWinners", "false", false);

            setUpMergeManager(request, request.getSession().getAttribute("algorithm").toString());

            MergeManager mergeManager = (MergeManager)session.getAttribute("mergeManager");
            String currentFile = session.getAttribute("currentFile").toString();

            mergeManager.addSimilarCandidates();

            boolean shouldSave=false;
			if(saveButtonPressed(request)){
				shouldSave = updateTable(request);
			}

            checkFilterParameters(request);
            generateIncumbents(request.getSession());
            generateIncumbentsView(request);

            request.getSession().setAttribute("mergeManager", mergeManager);
            request.getRequestDispatcher("/incumbency_table.jsp").forward(request, response);

            //Handle csv write after the page has been redirected to save waiting time
			if(shouldSave){
				mergeManager.save(currentFile);
			}

        } catch (IOException e){
            request.getSession().invalidate();
            throw e;
        }

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
	
	public void init() throws ServletException{
		//isFirst=true;
		
		//paths added here
		
		Enumeration parameterNames = getServletContext().getInitParameterNames();
		pathMap = new HashMap<>();
		descriptionMap = new HashMap<>();
		while(parameterNames.hasMoreElements()){
			String name = parameterNames.nextElement().toString();
			if(name.contains("Path"))
				pathMap.put(name.replace("Path", ""), getServletContext().getInitParameter(name));
			if(name.contains("Description"))
				descriptionMap.put(name.replace("Description", ""), getServletContext().getInitParameter(name));
		}
	}
	
	private boolean saveButtonPressed(HttpServletRequest request){
		return (request.getParameter("submit")!=null && request.getParameter("submit").equals("Save"));
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
            //TESTING STUFF: REMOVE WHEN DONE
            //mergeManager.forceMerge(new String [] {"18284","8661"});
            //shouldSave = true;
            //TILL HERE
			mergeManager.merge(userRows);
			mergeManager.updateMappedIds();
			mergeManager.updateUserIds(userRows,request.getSession().getAttribute("userName").toString(),request.getSession().getAttribute("email").toString());
			//isdone needs to be updated too on merge
			for(String row:userRows){
				isDoneMap.put(row, "on");
			}
			shouldSave = true;
		}

		if(!commentMap.isEmpty()){
			mergeManager.updateComments(commentMap);
			shouldSave = true;
		}
		
		if(!isDoneMap.isEmpty()){
			mergeManager.updateIsDone(isDoneMap);
			shouldSave = true;
			}
		
		//check whether rows have been marked for demerge; if yes,call the demerge method
		String [] rowsToBeDemerged = request.getParameterValues("demerges");
		//testing deMerge; Remove later
		//rowsToBeDemerged = new String[]{"26827","31908", "63686", "70245", "8576", "31906", "26815"};
		if(rowsToBeDemerged!=null){
			mergeManager.deMerge(rowsToBeDemerged);
			shouldSave = true;
		}
		
		//returns true if save needs to be done
		return shouldSave;
	}
	
	private void setUpDataset(HttpServletRequest request) throws IOException{
		
	    //this code mwill need changes
		
		//set defaults
		if(request.getSession().getAttribute("d")==null){
			String key = pathMap.keySet().iterator().next();
			request.getSession().setAttribute("currentFile", pathMap.get(key));
			request.getSession().setAttribute("dataset", key);
		}
	    
		if(request.getSession().getAttribute("d")==null||request.getParameter("dataset")!=null){
			
			if(request.getParameter("dataset")!=null){
				
				//This must be done before setting the dataset attribute
				if(!request.getParameter("dataset").equals(request.getSession().getAttribute("dataset")))
					request.getSession().setAttribute("datasetChanged", true);
				else
					request.getSession().setAttribute("datasetChanged", false);
				
				request.getSession().setAttribute("currentFile",pathMap.get(request.getParameter("dataset")));
				request.getSession().setAttribute("dataset", request.getParameter("dataset"));
				
			}

			Dataset d = Dataset.getDataset(request.getSession().getAttribute("currentFile").toString());
			request.getSession().setAttribute("d", d);
			Bihar.initRowFormat(d.getRows(), d);
			
			request.getSession().setAttribute("datasetName", pathMap.keySet());
			request.getSession().setAttribute("datasetDescription", descriptionMap);
			request.getSession().setAttribute("datasetPath", pathMap);
			
			
		}
		//if dataset changed flag is still null
		if(request.getSession().getAttribute("datasetChanged")==null){
			request.getSession().setAttribute("datasetChanged", false);
		}
	}

	private void setUpAlgorithm(HttpServletRequest request){
		//Check whether algo changed
		if(request.getParameter("algorithm")!= null){
			if(request.getSession().getAttribute("algorithm")!=null && request.getParameter("algorithm").equals(request.getSession().getAttribute("algorithm").toString())){
				request.getSession().setAttribute("algorithmChanged",false);
			}else{
				request.getSession().setAttribute("algorithmChanged",true);
			}
		}

		if(request.getSession().getAttribute("algorithmChanged")==null)
			request.getSession().setAttribute("algorithmChanged",false);

		assignAttributes(request, request.getSession(), "algorithm", "exactSameName",false);
	}
	
	private void setUpMergeManager(HttpServletRequest request, String algorithm){

		//IF algorithm's argument changes. Algo needs to be reloaded
		if(request.getParameter("algo-arg")!=null){
			request.getSession().setAttribute("algorithmChanged",true);
		}

		//SETs UP mergeManager
		MergeManager mergeManager;
		//if the dataset is same, no need to refresh merge manager; refresh otherwise
		if(request.getSession().getAttribute("mergeManager")==null || (Boolean)request.getSession().getAttribute("datasetChanged") || (Boolean)request.getSession().getAttribute("algorithmChanged")){
			mergeManager = MergeManager.getManager(algorithm, (Dataset)request.getSession().getAttribute("d"));
		}
		else
		{
			mergeManager = (MergeManager)request.getSession().getAttribute("mergeManager");
		}
		
		//Initial Mapping by mergeManager
		if(mergeManager.isFirstReading()){
			mergeManager.initializeIds();
			mergeManager.performInitialMapping();
		}
		else{
		    mergeManager.load();
		}

		//LOAD LATEST ARGUMENTS
		if(request.getParameter("algo-arg")!=null){
			mergeManager.setArguments(request.getParameter("algo-arg"));
		}

		//change back the status of datasetChanged & algorithmChanged
		request.getSession().setAttribute("datasetChanged", false);
		request.getSession().setAttribute("algorithmChanged", false);

		request.getSession().setAttribute("mergeManager", mergeManager);
	}
	
	private void checkFilterParameters(HttpServletRequest request){
		HttpSession session = request.getSession();
		
		assignAttributes(request, session, "filterParam", "State",false);
		assignAttributes(request, session, "filterValue", new String[]{"All Records"},true);
		session.setAttribute("filterValueNav", Arrays.toString((String[]) session.getAttribute("filterValue")));
		if(request.getParameter("state") != null){
			//if no state parameter specified, use all records by default
			if(request.getParameter("state").equals(""))
				session.setAttribute("filterValue", new String []{"All Records"});
			else
				session.setAttribute("filterValue", new String []{request.getParameter("state").toString().toUpperCase()});
			session.setAttribute("filterValueNav", Arrays.toString((String[]) session.getAttribute("filterValue"))); 
		}
		
		//Setting up nav bar attribute
		String filterParam, filterParamNav, filterValueNav;
	   	
	    filterParam = session.getAttribute("filterParam").toString();
	 	
	    //HARDCODED STUFF HERE
	    if(filterParam.equals("PC_name")){
	 	   filterParamNav = "Constituency";
	    }
	    else{
	 	   filterParamNav = filterParam;
	    }
	    filterValueNav = session.getAttribute("filterValueNav").toString();
		
		//SETTING UP REMAINING ATTRIBUTES
		session.setAttribute("filterParamNav", filterParamNav);
		session.setAttribute("filterValueNav", filterValueNav);
		
		
	}
	
	private void assignAttributes(HttpServletRequest request, HttpSession session, String attributeName, Object defaultValue, boolean isList){
		//CHECK DEFAULTS
		if(session.getAttribute(attributeName)==null)
			session.setAttribute(attributeName, defaultValue);
		//CHECK PARAMETERS
		if(request.getParameter(attributeName)!=null){
			if(isList)
				session.setAttribute(attributeName, request.getParameterValues(attributeName));
			else
				session.setAttribute(attributeName, request.getParameter(attributeName));
		}
			
	}
	
	private void generateIncumbents(HttpSession session){
		ArrayList<Multimap<String, Row>> incumbentsList;
		String filterParam = session.getAttribute("filterParam").toString();
	    String [] filterValue = (String [])session.getAttribute("filterValue");
	    boolean onlyWinners = session.getAttribute("onlyWinners").toString().equals("true");
	    
	    MergeManager mergeManager = (MergeManager)session.getAttribute("mergeManager");
	    
	    //WORKING WITH FILTER PARAMETERS & GENERATING INCUMBENTS LIST
		
  		if(filterValue!= null && filterParam!=null){
  			boolean isAllRecords=false;
  			for(String value:filterValue){
  				if(value.equals("All Records")){
  					isAllRecords=true;
  				}
  			}
  			if(isAllRecords)
  				incumbentsList = mergeManager.getIncumbents(onlyWinners);
  			else{
  				incumbentsList = mergeManager.getIncumbents(filterParam,filterValue, onlyWinners);
  			}
  			
  		}else{
  			incumbentsList = mergeManager.getIncumbents(onlyWinners);
  		}
  				
  		int[] progressData = mergeManager.getListCount(incumbentsList);
  		
  		//Setting up Incumbents list and progress related data
  		session.setAttribute("progressData", progressData);
		session.setAttribute("incumbentsList", incumbentsList);
		
	}
	
	public void generateIncumbentsView(HttpServletRequest request){
		
		HttpSession session = request.getSession();
		//set defaults
		int page=1;
		int recordsPerPage = 100;
		
		if(request.getParameter("page")!=null){
			page = Integer.parseInt(request.getParameter("page"));
		}
		
		
		ArrayList<Multimap<String, Row>> incumbentsList = (ArrayList<Multimap<String, Row>>) session.getAttribute("incumbentsList");
		ArrayList<Multimap<String, Row>> subList;
		if(incumbentsList.size()>recordsPerPage){
			int high = page*recordsPerPage;
			if(incumbentsList.size()<high){
				high=incumbentsList.size();
			}
			subList = new ArrayList<>(incumbentsList.subList((page-1)*recordsPerPage, high));
		}
		else{
			subList = incumbentsList;
		}
		
		
		int noOfRecords = incumbentsList.size();
				
		int noOfPages = (int) Math.ceil(noOfRecords*1.0/recordsPerPage);
		session.setAttribute("subList", subList);
		session.setAttribute("noOfPages", noOfPages);
		session.setAttribute("currentPage", page);
		
	}

}
