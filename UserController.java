package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.hibernate.graph.internal.parse.HEGLTokenTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController 
{
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void commonData(Model model,Principal principal)
	{
		String userName=principal.getName();
		System.out.println("Username" +userName);
		User user=userRepository.getUserByUserName(userName);
		System.out.println("User" +user);
		model.addAttribute("user",user);
	
	}
	
	@RequestMapping("/index")
	public String dashBoard(Model model,Principal principal)
	{
		
		model.addAttribute("title","User DashBoard - Smart Contact Management");
		
		
		return "normal/user_dashboard";
	}
	
	// adding form in dashboard
	
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact - Smart Contact Management");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	
	// form process controller
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file1,
			Principal principal,HttpSession session)
	{
		try 
		{
			String name=principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			
			
			if(file1.isEmpty())
			{
				System.out.println("File is Empty !!");
				contact.setImage("contact.png");
			}
			else
			{
				contact.setImage(file1.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file1 .getOriginalFilename());
				Files.copy(file1.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded Successfully");
			}
			
			
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			System.out.println("Data" +contact);
			System.out.println("Added to database");
			
			session.setAttribute("message",new Message("Your contact is added !! Add more...","success"));
		}
		catch (Exception e)  
		{
			System.out.println("Error" +e.getMessage());
			e.printStackTrace();
			session.setAttribute("message",new Message("something went wrong !! Try again...","danger"));
		}
		return "normal/add_contact_form";
	}
	
	
	@RequestMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		model.addAttribute("title","View Contacts - Smart Contact Management");
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		Pageable pageable=PageRequest.of(page,5);
		Page<Contact> contacts=this.contactRepository.findByContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";  
	}
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principle)
	{
		System.out.println("Cid " +cId);
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String userName=principle.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
			
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
		
		
	}
		@GetMapping("/delete/{cid}")
		public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal)
		{
			System.out.println("CID" +cId);
			Contact contact=this.contactRepository.findById(cId).get();
			System.out.println("Contact" +contact.getcId());
			
			
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			user.getContacts().remove(contact);
			
			this.userRepository.save(user);
			
			System.out.println("Deleted");
			session.setAttribute("message",new Message("Successfully deleted...!!","success"));
			return "redirect:/user/show-contact/0";
		}
		//open update form handler
		
		@PostMapping("/update-contact/{cid}")
		public String updateForm(@PathVariable("cid") Integer cid,Model model)
		{
			model.addAttribute("title","Update Contact - Smart Contact Manager ");
			Contact contact=this.contactRepository.findById(cid).get();
			model.addAttribute("contact",contact);
			
			return "normal/updateform";
		}
		
		//update contact handler
		
		@RequestMapping(value="/process-update",method=RequestMethod.POST)
		public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal)
		{
			try
			{
				// old contact details
				Contact oldContactDetail=this.contactRepository.findById(contact.getcId()).get();
				
				if (!file.isEmpty())
				{
					// delete old photo
					
					File deleteFile=new ClassPathResource("static/img").getFile();
					File file1=new File(deleteFile,oldContactDetail.getImage());
					file1.delete();
					//update new photo
					
					File saveFile=new ClassPathResource("static/img").getFile();
					Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file .getOriginalFilename());
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					contact.setImage(file.getOriginalFilename());
					
				}
				else
				{
					contact.setImage(oldContactDetail.getImage());
				}
				User user=this.userRepository.getUserByUserName(principal.getName());
				contact.setUser(user);
				this.contactRepository.save(contact);
				session.setAttribute("message",new Message("Your contact is updated...!!","success"));
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			
			
			
			System.out.println("Contact name" +contact.getName());
			System.out.println("Contact Id" +contact.getcId());
			
			return "redirect:/user/"+contact.getcId()+"/contact";
		}
		
		@GetMapping("/profile")
		public String yourProfile(Model model)
		{
			model.addAttribute("title","Profile - Smart Contact Manager");
			return "normal/profile";
		}
		
		
	
		@GetMapping("/settings")
		public String openSettings()
		{
			return "normal/settings";
		}
		
//		 change password handler
		
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
		{
			System.out.println("Old Password" +oldPassword);
			System.out.println("New Password" +newPassword);
			
			String userName=principal.getName();
			User currentUser=this.userRepository.getUserByUserName(userName);
			System.out.println(currentUser.getPassword());
			if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword()))
			{
				currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
				this.userRepository.save(currentUser);
				session.setAttribute("message",new Message("Your Password is successfully change","success"));
				
			}
			else
			{
				session.setAttribute("message",new Message("Please Enter correct password","danger"));
				return "redirect:/user/settings";
			}
			
			return "redirect:/user/index";
		}
		
		
		
		
		
}
