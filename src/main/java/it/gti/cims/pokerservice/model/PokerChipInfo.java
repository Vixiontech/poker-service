package it.gti.cims.pokerservice.model;

import java.util.Optional;

import it.gti.cims.chipencodingcommons.model.ChipInfo;
import it.gti.cims.pokerservice.enumeration.PokerChipColor;
import lombok.NonNull;

public class PokerChipInfo extends ChipInfo {

  public PokerChipInfo(@NonNull String epc) {
    super(epc);
  }

  public @NonNull Optional<PokerChipColor> getChipColor() {
    String epcHeader = getEpc().substring(4, 6);
    int code = Integer.parseInt(epcHeader, 16);
    
    return PokerChipColor.fromCode(code);
  }

  public static @NonNull PokerChipInfo fromChipInfo(@NonNull ChipInfo chip) {
    return new PokerChipInfo(chip.getEpc());
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof PokerChipInfo chip){
      return this.getEpc().contentEquals(chip.getEpc());
    }  
    return false;
  }
}
