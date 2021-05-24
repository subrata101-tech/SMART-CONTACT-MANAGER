package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController
{
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	// home controller
	
	@RequestMapping("/")
	public String home( Model m)
	{
		m.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	// about controller
	
	
	@RequestMapping("/about")
	public String about( Model m)
	{
		m.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	// signup controller
	
	@RequestMapping("/signup")
	public String signup( Model m)
	{
		m.addAttribute("title","Register - Smart Contact Manager");
		m.addAttribute("user",new User());
		return "signup";
	}
	
	//signup process
	
	@RequestMapping(value = "/do_register",method =RequestMethod.POST )
	public String registrationUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value = "agreement",defaultValue="false") boolean agreement,Model model,HttpSession session)
	{
		try
		{
			if(!agreement)
			{
				System.out.println("You have not agreed the terms and condition");
				throw new Exception("You have not agreed the terms and condition");
			}
			if(result1.hasErrors())
			{
				System.out.println("ERROR " +result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("my.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("User" +user);
			System.out.println("Agreement" +agreement);
			
			User result=this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Successfully Register","alert-success"));
			return "signup";
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("something went wrong !!" +e.getMessage(),"alert-danger")); 
			
			return "signup";
		}
		
		
	}
	//login handler
	@RequestMapping("/signin")
	public String login( Model m)
	{
		m.addAttribute("title","Login - Smart Contact Manager");
		return "login";
	}
	
	@RequestMapping("/started")
	public String homeStarted()
	{
		return "normal/user_dashboard";
		
	}
	
	
	// error handler
	
//	
//		@RequestMapping("/login-fail")
//		public String fail( Model m)
//		{
//			m.addAttribute("title","Login Error - Smart Contact Manager");
//			return "login-fail";
//		}
	
	
	
}

