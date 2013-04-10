package com.google.appengine.tck.users.support;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserServiceFailureException;
import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO: http://go/java-style#javadoc
 */
public class UserServiceServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        String env = SystemProperty.environment.value().toString();
        String responseMsg = env + ",";

        String method = req.getParameter("method");

        if (method == null || method.equals("env")) {
            responseMsg += env;
            resp.getWriter().print(responseMsg);
            return;
        }

        User user = userService.getCurrentUser();
        try {
            if (method.equals("getEmail")) {
                responseMsg += user.getEmail();
            } else if (method.equals("isUserLoggedIn")) {
                responseMsg += userService.isUserLoggedIn();
            } else if (method.equals("isUserAdmin")) {
                responseMsg += userService.isUserAdmin();
            } else if (method.equals("UserServiceFailureException")) {
                throw new UserServiceFailureException("Test!");
            } else {
                responseMsg += "UNRECOGNIZED METHOD:" + method;
            }
        } catch (Exception e) {
            responseMsg += e.getMessage();
        }
        resp.getWriter().print(responseMsg);
    }
}
