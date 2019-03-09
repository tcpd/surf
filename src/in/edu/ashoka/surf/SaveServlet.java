package in.edu.ashoka.surf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/* servlet to implement merges */
public class SaveServlet extends HttpServlet {
	public static Log log = LogFactory.getLog(SaveServlet.class);

	private static final long serialVersionUID = 1L;

    public SaveServlet() {
        super();
    }

    /** on receiving merge commands, this servlet calls mergemanager with the commands, and updates the view in the session accordingly */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();
		MergeManager mergeManager = (MergeManager) session.getAttribute("mergeManager");
		try {
			// parse the merge commands in json format
			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();
			MergeManager.Command[] commands = gson.fromJson(request.getParameter("json"), MergeManager.Command[].class);

			// update the groups and view
			mergeManager.applyUpdatesAndSave(commands);
			MergeManager.View view = mergeManager.getView (mergeManager.lastView.filterSpec, mergeManager.lastView.groupViewControl.name(), mergeManager.lastView.secondaryFilterFieldName, mergeManager.lastView.rowViewControl.name(), mergeManager.lastView.sortOrder);
			session.setAttribute("view", view);

			response.getOutputStream().print("{status: 0}");
		} catch (Exception e) {
			response.getOutputStream().print("{status: 1, message: " + e.getClass().getName() + "}"); // TODO: add detailed error message
		}
	}
}
