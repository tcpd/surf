package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * Servlet implementation class Incumbency
 */
@WebServlet("/Incumbency")
public class Incumbency extends HttpServlet implements HttpSessionActivationListener{
	private static final long serialVersionUID = 1L;
	private boolean sessionStarted;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//System.out.println(request.getSession().getAttribute("sessionStarted"));
		
		HttpSession session = request.getSession();
		if(sessionStarted==false) {
			
			NameData nameData = new TestNameData();
			nameData.initialize();
			nameData.iterateNameData();
			Iterator iterator = nameData.iterator();
			
			session.setAttribute("nameData", nameData);
			session.setAttribute("iterator", iterator);
			
			session.setAttribute("servlet", this);
			sessionStarted=true;
			//write a wait message for html
			
		}
		getNamePair((Iterator)session.getAttribute("iterator"), session, request, response);
		return;
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
