package it.gti.cims.pokerservice.model;

import it.gti.cims.pokerservice.enumeration.PokerChipColor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorAssociation {

  @NonNull
  private String username = "Unknown";
  @NonNull
  private PokerChipColor color;
  @NonNull
  private Double chipValue;

}
