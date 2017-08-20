<%@page language="java" contentType="application/json;charset=UTF-8"%>
<%@page trimDirectiveWhitespaces="true"%>
<%@ page import="in.edu.ashoka.surf.MergeManager"%><%@ page import="com.google.gson.GsonBuilder"%><%@ page import="com.google.gson.Gson"%><%@ page import="com.google.gson.JsonObject"%>
<%
MergeManager mergeManager = (MergeManager) session.getAttribute("mergeManager");
    JsonObject result = new JsonObject();

try {
    // parse the merge commands in json format
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    MergeManager.MergeCommand[] commands = gson.fromJson(request.getParameter("json"), MergeManager.MergeCommand[].class);

    // update the groups and view
    mergeManager.applyUpdatesAndSave(commands);
    MergeManager.View view = mergeManager.getView (mergeManager.lastView.filterSpec, mergeManager.lastView.sortOrder);
    session.setAttribute("view", view);
    result.addProperty ("status", 0);
} catch (Exception e) {
    result.addProperty ("status", 1);
    result.addProperty ("message", e.getClass().getName() + "}"); // TODO: add detailed error message
}
out.println (result.toString());
%>