package in.edu.ashoka.surf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class LoggingFilter implements javax.servlet.Filter {
	private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.LoggingFilter.class);

	public static String getRequestDescription(HttpServletRequest request)
	{
		return getRequestDescription(request, true);
	}

	private static String ellipsize(String s, int maxChars)
	{
		if (s == null)
			return null;

		if (maxChars < 4)
			return (s.substring(0, maxChars));

		if (s.length() > maxChars)
			return s.substring(0, maxChars - 3) + "...";
		else
			return s;
	}

	/** also sets current thread name to the path of the request */
	private static String getRequestDescription(HttpServletRequest request, boolean includingParams)
	{
		HttpSession session = request.getSession();
		String page = request.getServletPath();
		Thread.currentThread().setName(page);
		String userKey = (String) session.getAttribute("userKey");
		StringBuilder sb = new StringBuilder("Request[" + userKey + "@" + request.getRemoteAddr() + "]: " + request.getRequestURL());
		// return here if params are not to be included
		if (!includingParams)
			return sb.toString();

		String link = request.getRequestURL() + "?";

		Map<String, String[]> rpMap = request.getParameterMap();
		if (rpMap.size() > 0)
			sb.append(" params: ");
		for (Object o : rpMap.keySet())
		{
			String str1 = (String) o;
			sb.append(str1 + " -> ");
			if (str1.startsWith("password"))
			{
				sb.append("*** ");
				continue;
			}

			String[] vals = rpMap.get(str1);
			if (vals.length == 1)
				sb.append(ellipsize(vals[0], 100));
			else
			{
				sb.append("{");
				for (String x : vals)
					sb.append(ellipsize(x, 100) + ",");
				sb.append("}");
			}
			sb.append(" ");

			for (String val : vals)
				link += (str1 + "=" + vals[0] + "&");
		}

		sb.append(" link: " + link);
		return sb.toString();
	}

	public static void logRequest(HttpServletRequest request, boolean includingParams)
	{
		log.info("NEW " + getRequestDescription(request, includingParams));
	}

	private static void logRequest(HttpServletRequest request)
	{
		log.info("NEW " + getRequestDescription(request, true));
	}

	private static void logRequestComplete(HttpServletRequest request)
	{
		log.info("COMPLETED " + getRequestDescription(request, true));
		String page = request.getServletPath();
		Thread.currentThread().setName("done-" + page);
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws java.io.IOException, ServletException  {
		String requestURL = ((HttpServletRequest) request).getRequestURL().toString();
		// we want to log only pages, not every little resource
		boolean logRequest = !requestURL.endsWith(".gif") && !requestURL.endsWith(".svg") && !requestURL.endsWith(".png") && !requestURL.endsWith(".jpg") && !requestURL.endsWith(".js") && !requestURL.endsWith(".css");
		if (requestURL.endsWith("muselog.jsp") || requestURL.endsWith("status") || requestURL.contains("serveImage") || requestURL.contains("serveAttachment") || requestURL.contains("/fonts/"))
			logRequest = false;
		
		if (logRequest)
			logRequest((HttpServletRequest) request);
		chain.doFilter(request,response);
		if (logRequest)
			logRequestComplete((HttpServletRequest) request);
	} 

	@Override
	public void destroy() {
		log.info("Filter LoggingFilter destroyed");
		// TODO Auto-generated method stub

	}

	@Override
	public void init(FilterConfig arg0) {
		log.info("Filter LoggingFilter initialized");
		// TODO Auto-generated method stub		
	}
}
