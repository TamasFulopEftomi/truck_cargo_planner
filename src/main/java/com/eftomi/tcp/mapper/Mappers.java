package com.eftomi.tcp.mapper;

import com.eftomi.tcp.dto.PackagingInstructionDTO;
import com.eftomi.tcp.entity.Item;

public class Mappers {

    public static PackagingInstructionDTO itemToPackagingInstructionDTOMapper(Item item) {
        PackagingInstructionDTO packagingInstructionDTO = new PackagingInstructionDTO();
        packagingInstructionDTO.setItemNo(item.getItemNo());
        packagingInstructionDTO.setWeight(item.getItemWeight());
        packagingInstructionDTO.setPalletType(item.getPallet().getPalletType());
        packagingInstructionDTO.setBoxType(item.getBox().getBoxType());
        packagingInstructionDTO.setPcsInBox(item.getPcsInBox());
        packagingInstructionDTO.setBoxesInRow(item.getBox().getBoxesInRow());
        packagingInstructionDTO.setRowsOnPallet(item.getBox().getRowsOnPallet());
        packagingInstructionDTO.setStackable(item.getPallet().isStackable());
        return packagingInstructionDTO;
    }
}