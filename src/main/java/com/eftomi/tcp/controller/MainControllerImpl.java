package com.eftomi.tcp.controller;

import com.eftomi.tcp.dto.*;
import com.eftomi.tcp.entity.CargoItem;
import com.eftomi.tcp.entity.User;
import com.eftomi.tcp.service.CargoServiceImpl;
import com.eftomi.tcp.service.UserServiceImpl;
import com.eftomi.tcp.service.exception.UserNotFoundException;
import com.sun.el.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class MainControllerImpl implements MainController {

	@Autowired
	private CargoServiceImpl cargoService;

	@Autowired
	private UserServiceImpl userService;

	@Override
	public String index(Model model) {
		model.addAttribute("loginDTO", new LoginDTO());
		model.addAttribute("registrationDTO", new RegistrationDTO());
		return "index";
	}

	@Override
	public String login(LoginDTO loginDTO, HttpSession session, Model model) {
		boolean loggedIn = userService.login(loginDTO.getEmail(), loginDTO.getPassword());
		if (loggedIn) {
			Optional<User> optUser = userService.getUser(loginDTO.getEmail());
			if (optUser.isPresent()) {
				session.setAttribute("username", optUser.get().getName());
			} else {
				throw new UserNotFoundException("N/A");
			}
			return "redirect:/mainPage";
		} else {
			model.addAttribute("loginError",
					String.format("Email address %s or password is incorrect!", loginDTO.getEmail()));
			model.addAttribute("loginDTO", new LoginDTO());
			model.addAttribute("registrationDTO", new RegistrationDTO());
			return "index";
		}
	}

	@Override
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	@Override
	public String registration(RegistrationDTO registrationDTO, HttpSession session, Model model) {
		boolean registeredIn = userService.registration(registrationDTO.getName(), registrationDTO.getEmail(),
				registrationDTO.getPassword());
		if (registeredIn) {
			session.setAttribute("username", registrationDTO.getName());
			return "redirect:/mainPage";
		} else {
			model.addAttribute("registrationError",
					String.format("Email address %s already exists in the database or it does not meet the email "
									+ "address format requirements or the name or the password field length shorter than 3 character!",
							registrationDTO.getEmail()));
			model.addAttribute("loginDTO", new LoginDTO());
			model.addAttribute("registrationDTO", new RegistrationDTO());
			return "index";
		}
	}

	@Override
	public String mainPage(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		return "main_page";
	}

	@Override
	public String specifications(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		return "specifications";
	}

	@Override
	public String packagingInstruction(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		model.addAttribute("packagingInstruction", cargoService.packagingInstruction());
		return "packaging_instruction";
	}

	@Override
	public String emptiesList(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		model.addAttribute("boxesAndContainersList", cargoService.boxesAndContainersList());
		model.addAttribute("palletsAndLidsList", cargoService.palletsAndLidsList());
		return "empties_list";
	}

	@Override
	public String cargoList(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		cargoService.clearCargoItem();
		model.addAttribute("itemMap", cargoService.getItemNumberMap());
		model.addAttribute("itemNumberSetDTO", new ItemNumberSetDTO());
		session.setAttribute("qtyError", null);
		return "cargo_select";
	}

	@Override
	public String cargoSelect(Model model, ItemNumberSetDTO itemNumberSetDTO, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		session.setAttribute("itemNumberSetDTO", itemNumberSetDTO);
		String list = itemNumberSetDTO.getItemNumberSet().stream().map(n -> String.valueOf(n))
				.collect(Collectors.joining(", "));
		list = list.equals("") ? null : list;
		model.addAttribute("list", list);
		model.addAttribute("itemMap", cargoService.getItemNumberMap());
		model.addAttribute("itemNumberSetDTO", new ItemNumberSetDTO());
		return "cargo_select";
	}

	@Override
	public String cargoListQuantity(HttpSession session) {
		ItemNumberSetDTO itemNumberSetDTO = (ItemNumberSetDTO) session.getAttribute("itemNumberSetDTO");
		cargoService.cargoListCreate(itemNumberSetDTO.getItemNumberSet());
		return "redirect:/cargoPlanner";
	}

	@Override
	public String cargoPlanner(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		List<CargoItem> cargoItemList = cargoService.getAllCargoItems();
		CargoListDTO cargoListDTO = cargoService.calculateCargo(cargoItemList);
		model.addAttribute("cargoItemList", cargoItemList);
		model.addAttribute("cargoListDTO", cargoListDTO);
		model.addAttribute("qtyError", session.getAttribute("qtyError"));
		return "cargo_planner";
	}

	@Override
	public String modifyQuantity(Model model, int id, HttpSession session) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		Optional<CargoItem> optCargoItem = cargoService.getCargoItem(id);
		if (optCargoItem.isPresent()) {
			CargoItem cargoItem = optCargoItem.get();
			CargoItemDTO cargoItemDTO = new CargoItemDTO();
			cargoItemDTO.setId(cargoItem.getId());
			cargoItemDTO.setItemNumber(cargoItem.getItemNumber());
			cargoItemDTO.setQtyNeeds(cargoItem.getQtyNeeds());
			cargoItemDTO.setQtyToBeDelivered(cargoItem.getQtyToBeDelivered());
			cargoItemDTO.setQtyNeedsString(cargoItem.getQtyNeeds() + "");
			model.addAttribute("cargoItemDTO", cargoItemDTO);
			return "modify_quantity";
		} else {
			return "/";
		}
	}

	@Override
	public String modifyItem(Model model, HttpSession session, CargoItemDTO cargoItemDTO) throws ParseException {
		String username = (String) session.getAttribute("username");
		model.addAttribute("username", username);
		if (cargoService.validQuantity(cargoItemDTO.getQtyNeedsString())) {
			CargoItem cargoItem = new CargoItem();
			cargoItem.setId(cargoItemDTO.getId());
			cargoItem.setItemNumber(cargoItemDTO.getItemNumber());
			cargoItem.setQtyNeeds(cargoService.parseQuantity(cargoItemDTO.getQtyNeedsString()));
			cargoItem.setQtyToBeDelivered(cargoItemDTO.getQtyToBeDelivered());
			cargoService.calculateCargoItemQuantities(cargoItem);
			List<CargoItem> cargoItemList = cargoService.getAllCargoItems();
			CargoListDTO cargoListDTO = cargoService.calculateCargo(cargoItemList);
			model.addAttribute("cargoItemList", cargoItemList);
			model.addAttribute("cargoListDTO", cargoListDTO);
			session.setAttribute("qtyError", null);
			return "cargo_planner";
		} else {
			session.setAttribute("qtyError",
					"Please enter a positive integer on the form on the Change Requested Quantity page!");
			return "redirect:/cargoPlanner";
		}
	}

}
