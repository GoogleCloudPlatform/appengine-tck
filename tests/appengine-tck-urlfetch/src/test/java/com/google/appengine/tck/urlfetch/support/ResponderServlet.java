package com.google.appengine.tck.urlfetch.support;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet used to generate responses for tests.
 */
public class ResponderServlet extends HttpServlet {

    public static final String DEFAULT_CONTENT = "Hello, world";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            PrintWriter writer = response.getWriter();
            writer.print(DEFAULT_CONTENT);
        } else if (action.equals("send404")) {
            response.sendError(404, "Pretend I'm not here");
        } else if (action.equals("sleep10")) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                // ignored
            }
            response.getWriter().println("Slept 10 seconds\n");
        } else {
            response.sendError(500, "unknown action: " + action);
        }
    }
}