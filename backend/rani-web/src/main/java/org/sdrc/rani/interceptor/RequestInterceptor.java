package org.sdrc.rani.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *  
 */
@Component
public class RequestInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

//		System.out.println("interceptor called");
		OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		
		if (oauth == null){
			return true;
		}
		
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		Account acc = accountRepository.findById(userModel.getUserId());

		if (!acc.isEnabled()) {
			// throw account disable exception
			response.setStatus(412);
			response.getWriter().write(configurableEnvironment.getProperty("account.disable.message"));
			return false;
		}

		if (acc.isLocked()) {
			// throw account locked exception
			response.setStatus(412);
			response.getWriter().write(configurableEnvironment.getProperty("account.locked.message"));
		}

		//412 The pre condition given in the request evaluated to false by the server.
		if (acc.isExpired()) {
			// throw account expired excetion
			response.setStatus(412);
			response.getWriter().write(configurableEnvironment.getProperty("account.expired.message"));
			
		}

		return true;
	}

}
