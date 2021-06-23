package org.sdrc.datum19.controller;

import org.sdrc.datum19.util.UserDetailsModel;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = {"http://192.168.1.10:8080", "http://aggregation.sdrc.co.in:8080"})
//@CrossOrigin
@RestController
public class LoginController {
	@GetMapping("login")
	public UserDetailsModel login(@RequestParam("username") String username, @RequestParam("password") String password) {
		System.out.println("username :: "+username);
		UserDetailsModel user = new UserDetailsModel();
		user.setUsername(username);
		return user;
	}
}
