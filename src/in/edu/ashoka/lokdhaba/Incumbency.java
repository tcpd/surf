package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.io.PrintStream;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import com.google.common.collect.Multimap;

import edu.stanford.muse.util.Pair;



/**
 * Servlet implementation class Incumbency
 */
@WebServlet("/Incumbency")
public class Incumbency extends HttpServlet implements HttpSessionActivationListener{
	private static final long serialVersionUID = 1L;
	private boolean sessionStarted;
	private static final String ID_PREFIX = "ID";
	private static PrintStream out = System.out;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//System.out.println(request.getSession().getAttribute("sessionStarted"));
		//Bihar.main(null);
		HttpSession session = request.getSession();
		if(sessionStarted==false) {
			String file = "/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv";
			Row.setToStringFields("Name-Sex-Year-AC_name-Party-Position-Votes");
	        Dataset d = new Dataset(file);
	        d.registerColumnAlias("Candidate_name", "Name");
	        d.registerColumnAlias("Candidate_sex", "Sex");
	        d.registerColumnAlias("Party_abbreviation", "Party");
	        Multimap<String, String> resultMap = Bihar.getExactSamePairs(d.rows);

	        Multimap<String, Row> idToRows = SurfExcel.split (d.rows, ID_PREFIX + "Candidate_name");
	        
	        
	        List<String> rowsTobeDisplayed = new ArrayList<String>();
	        HashMap<Row, Integer> rowToIndexMapper = new HashMap<Row, Integer>();
	        for (String canonicalVal: resultMap.keySet()) {
	             Collection<String> idsForThisCVal = resultMap.get(canonicalVal);
	             
	             // UI should allow for merging between any 2 of these ids.
	             for (String id: idsForThisCVal) {
	            	 //Pair p;(Row) idToRows.get(id)
	            	 
	            	 //String id1 = (String)p.getFirst();
	                 //String id2 = (String)p.getSecond();
	            	 List<Row> rowsForThisId = new ArrayList<Row>();
	            	 rowsForThisId.addAll(idToRows.get(id));
	            	 for(Row row:rowsForThisId) {
	            		 rowsTobeDisplayed.add(row.toString());
	            		 rowToIndexMapper.put(row,rowsTobeDisplayed.size()-1);
	            	 }
	                 // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
	            	 
	                 }
	                 
	                 
	             }
	        request.getSession().setAttribute("rowsToBeDisplayed", rowsTobeDisplayed);
	        sessionStarted = true;
	        }

	        //List<Pair<String, String>> mergedIds = ....
	        //mergeIds(mergedIds); 
			//System.out.println(bufferOut);
			request.getRequestDispatcher("/incumbency_check1.jsp").forward(request, response);
		}
		
		
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	void getNamePair(Iterator iterator, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(iterator.hasNext()){
			session.setAttribute("namePair", iterator.next());
			request.getRequestDispatcher("/incumbency_check1.jsp").forward(request, response);
		}
		else {
			//print out that list has been exhausted
		}
			
	}


	@Override
	public void sessionDidActivate(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		sessionStarted=false;
		
	}


	@Override
	public void sessionWillPassivate(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
