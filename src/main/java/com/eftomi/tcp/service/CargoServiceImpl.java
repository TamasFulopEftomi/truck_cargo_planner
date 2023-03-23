package com.eftomi.tcp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.eftomi.tcp.mapper.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eftomi.tcp.dto.CargoListDTO;
import com.eftomi.tcp.dto.PackagingInstructionDTO;
import com.eftomi.tcp.entity.Box;
import com.eftomi.tcp.entity.CargoItem;
import com.eftomi.tcp.entity.Item;
import com.eftomi.tcp.entity.Pallet;
import com.eftomi.tcp.repository.BoxRepository;
import com.eftomi.tcp.repository.CargoItemRepository;
import com.eftomi.tcp.repository.ItemRepository;
import com.eftomi.tcp.repository.PalletRepository;
import com.eftomi.tcp.service.exception.CargoItemNotFoundException;
import com.eftomi.tcp.service.exception.ItemNotFoundException;
import com.sun.el.parser.ParseException;

@Service
public class CargoServiceImpl implements CargoService {

    @Autowired
    private ItemRepository itemDAO;

    @Autowired
    private BoxRepository boxDAO;

    @Autowired
    private PalletRepository palletDAO;

    @Autowired
    private CargoItemRepository cargoItemDAO;

    @Override
    public List<Item> getAllItems() {
        return itemDAO.findAll();
    }

    @Override
    public List<PackagingInstructionDTO> packagingInstruction() {
        List<Item> itemList = getAllItems();
        List<PackagingInstructionDTO> pIDTOList = new ArrayList<>();
        itemList.stream().forEach(item -> {
            pIDTOList.add(Mappers.itemToPackagingInstructionDTOMapper(item));
        });
        return pIDTOList;
    }

    @Override
    public List<Box> boxesAndContainersList() {
        return boxDAO.findAll();
    }

    @Override
    public List<Pallet> palletsAndLidsList() {
        return palletDAO.findAll();
    }

    @Override
    public Map<String, String> getItemNumberMap() {
        List<Item> itemList = getAllItems();
        Map<String, String> itemNumberMap = new TreeMap<>();
        itemList.stream().forEach(item -> {
            itemNumberMap.put(item.getItemNo(), item.getItemNo());
        });
        return itemNumberMap;
    }

    @Override
    public void cargoListCreate(Set<String> itemNumberSet) {
        itemNumberSet.stream().forEach(itemNumber -> {
            cargoItemDAO.save(new CargoItem(itemNumber, 0, 0));
        });
    }

    @Override
    public List<CargoItem> getAllCargoItems() {
        return cargoItemDAO.findAll();
    }

    @Override
    public void clearCargoItem() {
        cargoItemDAO.deleteAll();
    }

    @Override
    public Optional<CargoItem> getCargoItem(int id) {
        return cargoItemDAO.findById(id);
    }

    @Override
    public void calculateCargoItemQuantities(CargoItem cargoItem) {
        String itemNumber = cargoItem.getItemNumber();
        int pcsInBox;
        int qtyNeeds = cargoItem.getQtyNeeds();
        Optional<Item> optItem = itemDAO.findByItemNo(itemNumber);
        if (optItem.isPresent()) {
            Item item = optItem.get();
            pcsInBox = item.getPcsInBox();
        } else {
            throw new ItemNotFoundException(cargoItem.getId());
        }
        int qtyToBeDelivered;
        int wholeBoxesNumber = qtyNeeds / pcsInBox;
        if (qtyNeeds % pcsInBox == 0) {
            qtyToBeDelivered = wholeBoxesNumber * pcsInBox;
        } else {
            qtyToBeDelivered = (wholeBoxesNumber + 1) * pcsInBox;
        }
        cargoItem.setQtyToBeDelivered(qtyToBeDelivered);
        update(cargoItem);
    }

    private void update(CargoItem cargoItem) {
        Optional<CargoItem> optDbCargoItem = cargoItemDAO.findById(cargoItem.getId());
        if (optDbCargoItem.isPresent()) {
            CargoItem dbCargoItem = optDbCargoItem.get();
            dbCargoItem.setItemNumber(cargoItem.getItemNumber());
            dbCargoItem.setQtyNeeds(cargoItem.getQtyNeeds());
            dbCargoItem.setQtyToBeDelivered(cargoItem.getQtyToBeDelivered());
            cargoItemDAO.save(dbCargoItem);
        } else {
            throw new CargoItemNotFoundException(cargoItem.getId());
        }
    }

    @Override
    public CargoListDTO calculateCargo(List<CargoItem> cargoItems) {
        Map<String, Integer> empties = new TreeMap<>();
        double sumNetWeight = 0;
        double sumEmptiesWeight = 0;
        int sumOfWholePallets = 0;
        int sumOfNotWholePallets = 0;
        int sumNumberOfStackablePallets = 0;

        for (CargoItem cargoItem : cargoItems) {
            Optional<Item> optItem = itemDAO.findByItemNo(cargoItem.getItemNumber());
            if (optItem.isPresent()) {
                Item item = optItem.get();

                // helping variables for speed
                int qtyTBD = cargoItem.getQtyToBeDelivered();
                int pcsInBox = item.getPcsInBox();
                int boxesInRow = item.getBox().getBoxesInRow();
                int rowsOnPallet = item.getBox().getRowsOnPallet();
                String palletType = item.getPallet().getPalletType();

                // calculate net weight
                sumNetWeight += qtyTBD * item.getItemWeight();

                // calculate empties weight
                int boxesToBeDelivered = qtyTBD / pcsInBox;
                int boxesOnPallet = boxesInRow * rowsOnPallet;
                int numberOfWholePallets = boxesToBeDelivered / boxesOnPallet;
                int numberOfNotWholePallets = (boxesToBeDelivered % boxesOnPallet == 0) ? 0 : 1;
                double palletsAndLidsWeight = (numberOfWholePallets + numberOfNotWholePallets) *
                        item.getPallet().getPalletWeight() + numberOfWholePallets * item.getPallet().getLidWeight();
                sumEmptiesWeight += boxesToBeDelivered * item.getBox().getBoxWeight() + palletsAndLidsWeight;

                // calculate whole pallets and not whole pallets
                sumOfWholePallets += numberOfWholePallets;
                sumOfNotWholePallets += numberOfNotWholePallets;

                // calculate cargo space I.
                int boxesToBeDeliveredOnStackablePallet = 0;
                int boxesOnStackablePallet = 0;
                if (item.getPallet().isStackable()) {
                    boxesToBeDeliveredOnStackablePallet = qtyTBD / pcsInBox;
                    boxesOnStackablePallet = boxesInRow * rowsOnPallet;
                    sumNumberOfStackablePallets += boxesToBeDeliveredOnStackablePallet / boxesOnStackablePallet;
                }

                // prepare and create empties list
                int boxesNumber = qtyTBD / pcsInBox;
                int pcsOnPallet = pcsInBox * boxesInRow * rowsOnPallet;
                int palletsNumber = (qtyTBD % pcsOnPallet == 0) ? qtyTBD / pcsOnPallet : qtyTBD / pcsOnPallet + 1;
                int numberOfWholePalletsFromStackable = 0;
                if (palletType.equals(PAL002) || palletType.equals(PAL003) || palletType.equals(PAL004)) {
                    numberOfWholePalletsFromStackable = qtyTBD / pcsOnPallet;
                }
                addEmpties(empties, item.getBox().getBoxName(), boxesNumber);
                addEmpties(empties, item.getPallet().getPalletName(), palletsNumber);
                addEmpties(empties, PLASTIC_LID, numberOfWholePalletsFromStackable);
            } else {
                throw new CargoItemNotFoundException(cargoItem.getId());
            }
        }

        // calculate grossWeight
        double grossWeight = sumNetWeight + sumEmptiesWeight;

        // calculate number of pallets
        int sumNumberOfPallets = sumOfWholePallets + sumOfNotWholePallets;

        // calculate cargo space II.
        int half;
        int subtraction;
        half = sumNumberOfPallets / 2;
        subtraction = half < sumNumberOfStackablePallets ? half : sumNumberOfStackablePallets;
        int loadingSpace = sumNumberOfPallets - subtraction;

        CargoListDTO cargoListDTO = new CargoListDTO();
        cargoListDTO.setNetWeight(roundToOneDecimalPlace(sumNetWeight));
        cargoListDTO.setEmptiesWeight(roundToOneDecimalPlace(sumEmptiesWeight));
        cargoListDTO.setGrossWeight(roundToOneDecimalPlace(grossWeight));
        cargoListDTO.setNumberOfNotWholePallets(sumOfNotWholePallets);
        cargoListDTO.setNumberOfWholePallets(sumOfWholePallets);
        cargoListDTO.setNumberOfPallets(sumNumberOfPallets);
        cargoListDTO.setLoadingSpace(loadingSpace);
        cargoListDTO.setEmpties(empties);
        return cargoListDTO;
    }

    private double roundToOneDecimalPlace(double amount) {
        return (double) (Math.round(amount * 10)) / 10;
    }

    private void addEmpties(Map<String, Integer> emptiesMap, String empties, Integer qty) {
        if (emptiesMap.containsKey(empties)) {
            int emptiesValue = emptiesMap.get(empties);
            int sum = emptiesValue + qty;
            emptiesMap.put(empties, sum);
        } else if (0 < qty && !empties.equals(EUR_PLACE)) {
            emptiesMap.put(empties, qty);
        }
    }

    @Override
    public boolean validQuantity(String qty) {
        if (qty.matches("[0-9]+")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int parseQuantity(String qty) throws ParseException {
        return Integer.parseInt(qty);
    }
}

