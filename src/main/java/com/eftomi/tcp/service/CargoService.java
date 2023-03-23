package com.eftomi.tcp.service;

import com.eftomi.tcp.dto.CargoListDTO;
import com.eftomi.tcp.dto.PackagingInstructionDTO;
import com.eftomi.tcp.entity.Box;
import com.eftomi.tcp.entity.CargoItem;
import com.eftomi.tcp.entity.Item;
import com.eftomi.tcp.entity.Pallet;
import com.sun.el.parser.ParseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CargoService {

    String PAL002 = "pal002";
    String PAL003 = "pal003";
    String PAL004 = "pal004";
    String PLASTIC_LID = "Plastic Lid";
    String EUR_PLACE = "EUR Place";

    List<Item> getAllItems();
    List<PackagingInstructionDTO> packagingInstruction();
    List<Box> boxesAndContainersList();
    List<Pallet> palletsAndLidsList();
    Map<String, String> getItemNumberMap();
    void cargoListCreate(Set<String> itemNumberSet);
    List<CargoItem> getAllCargoItems();
    void clearCargoItem();
    Optional<CargoItem> getCargoItem(int id);
    void calculateCargoItemQuantities(CargoItem cargoItem);
    CargoListDTO calculateCargo(List<CargoItem> cargoItems);
    boolean validQuantity(String qty);
    int parseQuantity(String qty) throws ParseException;
}
