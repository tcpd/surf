package in.edu.ashoka.lokdhaba;

import java.io.IOException;
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

/**
 * Servlet implementation class IncumbencyServlet
 */
@WebServlet("/IncumbencyServlet")
public class IncumbencyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	boolean isFirst;
	Dataset d;
	MergeManager mergeManager;
	//filepaths
	String currentFile;
       
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
		
		//SETTING UP THE DATASET FOR MERGEMANAGER
		setUpDataset();
		checkFilterParameters(request);
		setUpMergeManager(request.getSession().getAttribute("algorithm").toString());
		mergeManager.addSimilarCandidates();
		
	    if(saveButtonPressed(request)){
	    	boolean shouldSave = updateTable(request);
	    	if(shouldSave){
	    		mergeManager.save(currentFile);
	    	}
	    	
	    }
	    
	    
	    request.getSession().setAttribute("mergeManager", mergeManager);
	    request.getRequestDispatcher("/incumbency_table.jsp").forward(request, response);
	}
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public void init() throws ServletException{
		isFirst=true;
		
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
		
		if(userRows!=null && userRows.length>0){
			mergeManager.merge(userRows);
			mergeManager.updateMappedIds();
			mergeManager.updateUserIds(userRows,request.getSession().getAttribute("userName").toString(),request.getSession().getAttribute("email").toString());
			//dropdown needs to be updated too on merge
			for(String row:userRows){
				isDoneMap.put(row, "yes");
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
	
	private void setUpDataset(){
		//paths go here
	    String ge = getServletContext().getInitParameter("gePath").toString();
	    
	    //this code mwill need changes
	    currentFile = ge;
	    
	    
		if(isFirst){
			
			try {
				d = new Dataset(ge);
				Bihar.initRowFormat(d.getRows(), d);
			
			}
			
			catch(IOException ioex){
				ioex.printStackTrace();
			}
			isFirst = false;
		}
	}
	
	private void setUpMergeManager(String algorithm){
		//SETs UP mergeManager
		mergeManager = MergeManager.getManager(algorithm, d);

		
		//Initial Mapping by mergeManager
		if(mergeManager.isFirstReading()){
			mergeManager.initializeIds();
			mergeManager.performInitialMapping();
		}
		else{
		    mergeManager.load();
		}
	}
	
	private void checkFilterParameters(HttpServletRequest request){
		HttpSession session = request.getSession();
		/*//assign username
		if(request.getParameter("userName")!= null){
			session.setAttribute("userName", request.getParameter("userName").toString());	
		}
	   
		
		if(request.getParameter("email")!= null){
			session.setAttribute("email", request.getParameter("email").toString());	
		}  
	   
	   //Check if the user is entering for the first time. 
	   
		String checkVar = "algorithm";
	   
		if(session.getAttribute(checkVar) != null || request.getParameter(checkVar) != null){
		
		   if(request.getParameter("algorithm")!= null){
				session.setAttribute("algorithm", request.getParameter("algorithm").toString());	
		   }
		   
		   
		   if(request.getParameter("dataset")!= null){
				session.setAttribute("dataset", request.getParameter("dataset").toString());	
		   }
		   
		   
		   if(request.getParameter("filterParam")!= null){
				
				session.setAttribute("filterParam", request.getParameter("filterParam").toString());	
			}
		   
		   if(request.getParameter("filterValue")!= null){
				
				session.setAttribute("filterValue", request.getParameter("filterValue").toString());
			}
		   
		   
		   	
		   session.setAttribute("filterValueNav", Arrays.toString(request.getParameterValues("filterValue")));
		}
		
		else{
			   //SET DEFAULTS FOR THE VARIABLE
			   
				session.setAttribute("algorithm", "exactSameName");
				session.setAttribute("dataset", "ge");
				session.setAttribute("filterParam", "State");
				session.setAttribute("filterValue", "All Records");
				session.setAttribute("filterParamNav", session.getAttribute("filterParam"));
				session.setAttribute("filterValueNav", session.getAttribute("filterValue"));
			
				if(request.getParameter("state") != null){
					session.setAttribute("filterValueNav", request.getParameter("state").toString().toUpperCase()); 
				}
		}
		
		String filterValueNav = session.getAttribute("filterValueNav").toString();
		if(filterValueNav != null){
			if(filterValueNav.toString().equals("null")){
				filterValueNav = "All Records";
			}
		}*/
		
		assignAttributes(request, session, "userName", "Name Not Specified",false);
		assignAttributes(request, session, "email", "email Not Specified",false);
		assignAttributes(request, session, "algorithm", "exactSameName",false);
		assignAttributes(request, session, "filterParam", "State",false);
		assignAttributes(request, session, "filterValue", new String[]{"All Records"},true);
		session.setAttribute("filterValueNav", Arrays.toString((String[]) session.getAttribute("filterValue")));
		if(request.getParameter("state") != null){
			if(request.getParameter("state").equals(""))
				session.setAttribute("filterValue", new String []{"All Records"});
			else
				session.setAttribute("filterValue", new String []{request.getParameter("state").toString().toUpperCase()});
			session.setAttribute("filterValueNav", Arrays.toString((String[]) session.getAttribute("filterValue"))); 
		}
		
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

}
