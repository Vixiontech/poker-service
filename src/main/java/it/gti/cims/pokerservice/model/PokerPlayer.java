package it.gti.cims.pokerservice.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import it.gti.cims.gamecommons.model.game.Player;
import it.gti.cims.gamecommons.model.inventoryfeed.SingleAntennaInventory;
import it.gti.cims.pokerservice.util.OptionalUtils;
import it.gti.cims.chipencodingcommons.enumeration.Denomination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PokerPlayer extends Player {
  
  @NonNull
  private ColorAssociation association;
  @NonNull
  protected Collection<PokerChipInfo> bet = new ConcurrentSkipListSet<>();
  @NonNull
  protected Collection<PokerChipInfo> payout = new ConcurrentSkipListSet<>();



/*
  public ColorAssociation getAssociation() {
    return this.association;
  }
*/

  public double getBetTotalValue() {
    return bet.size() * association.getChipValue();
  }

  public double getPayoutTotalValue() {
    return payout.size() * association.getChipValue();
  }


  public void prepareForNewGame() {
    bet.clear();
    payout.clear();
  }

  @Override
  public @NonNull String getCsvLine() {
    if (this.association != null) {
      return "%s, %s, %.2f, %.2f, %.2f".formatted(
          association.getUsername(),
          association.getColor(),
          association.getChipValue(),
          getBetTotalValue(),
          getPayoutTotalValue());
    } else {
      return "%.2f, %.2f".formatted(
          getBetTotalValue(),
          getPayoutTotalValue());
    }
  }

  @Override
  public @NonNull String getPlain() {
    return "%s, %.2f, %.2f #".formatted(
        association.getUsername(),
        getBetTotalValue(),
        getPayoutTotalValue());
  }

}
