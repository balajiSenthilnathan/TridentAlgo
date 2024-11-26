package com.trident.trident_algo.api.service;

import com.trident.trident_algo.api.model.BinanceUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {

    private static final String USER_SESSION_KEY = "user_keys";

    public void saveUserSession(HttpServletRequest request, BinanceUser user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_KEY, user);
    }

    public void removeUserSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public String getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute(USER_SESSION_KEY) : null;
    }

    public boolean isUserLoggedIn(HttpServletRequest request) {
        return getLoggedInUser(request) != null;
    }
}
