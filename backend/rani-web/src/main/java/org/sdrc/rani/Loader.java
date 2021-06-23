package org.sdrc.rani;

import org.sdrc.usermgmt.core.annotations.EnableUserManagementWithJWTMongoSecurityConfiguration;
import org.sdrc.usermgmt.core.util.UgmtClientCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Subham Ashish (subham@sdrc.co.in) This class enables user-management
 *         with JWT security configuration
 */
@Component
@EnableUserManagementWithJWTMongoSecurityConfiguration
public class Loader {

	@Bean
	public UgmtClientCredentials ugmtClientCredentials() {
		return new UgmtClientCredentials("rani", "rani@123#!");
	}

}
