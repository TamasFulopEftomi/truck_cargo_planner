package com.eftomi.tcp.controller;

import com.eftomi.tcp.dto.CargoItemDTO;
import com.eftomi.tcp.dto.ItemNumberSetDTO;
import com.eftomi.tcp.dto.LoginDTO;
import com.eftomi.tcp.dto.RegistrationDTO;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import com.sun.el.parser.ParseException;

public interface MainController  {

    @GetMapping("/")
    public String index(Model model);

    @PostMapping("/index")
    public String login(LoginDTO loginDTO, HttpSession session, Model model);

    @GetMapping("/logout")
    public String logout(HttpSession session);

    @PostMapping("/registration")
    public String registration(RegistrationDTO registrationDTO, HttpSession session, Model model);

    @GetMapping("/mainPage")
    public String mainPage(Model model, HttpSession session);

    @GetMapping("/specifications")
    public String specifications(Model model, HttpSession session);

    @GetMapping("/packagingInstruction")
    public String packagingInstruction(Model model, HttpSession session);

    @GetMapping("/emptiesList")
    public String emptiesList(Model model, HttpSession session);

    @GetMapping("/cargoList")
    public String cargoList(Model model, HttpSession session);

    @PostMapping("/cargoSelect")
    public String cargoSelect(Model model, ItemNumberSetDTO itemNumberSetDTO, HttpSession session);

    @PostMapping("/cargoListQuantity")
    public String cargoListQuantity(HttpSession session);

    @GetMapping("/cargoPlanner")
    public String cargoPlanner(Model model, HttpSession session);

    @PostMapping("/modifyQuantity")
    public String modifyQuantity(Model model, int id, HttpSession session);

    @PostMapping("/modifyItem")
    public String modifyItem(Model model, HttpSession session, CargoItemDTO cargoItemDTO) throws ParseException;


}
