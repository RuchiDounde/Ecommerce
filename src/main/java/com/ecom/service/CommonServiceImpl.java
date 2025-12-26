package com.ecom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CommonServiceImpl implements CommonService {

	@Override
	public void removeSessionMessage() {
	 HttpServletRequest request = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest();//here we typcast the attributes in ServletRequestAttributes
	     HttpSession session = request.getSession();//here we get the object of session
	     session.removeAttribute("SuccessMsg");
	     session.removeAttribute("errorMsg");
	}
	

}
