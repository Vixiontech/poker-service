package it.gti.cims.pokerservice.model;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.math.BigDecimal;

import it.gti.cims.chipencodingcommons.enumeration.Denomination;
import it.gti.cims.gamecommons.model.game.Player;
import it.gti.cims.pokerservice.enumeration.PokerChipColor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor

@EqualsAndHashCode(callSuper = false)
public class ValuePokerPlayer extends PokerPlayer {
  /*
  @Override
  public ColorAssociation getAssociation() {
    return new ColorAssociation("Anonymous", PokerChipColor.NO_COLOR, 0d);
  }
  */
  
  private volatile BigDecimal rakePercentage;
  private volatile BigDecimal progressive;
  private volatile boolean fixedProgressive;
  private volatile BigDecimal houseHold;
  private volatile BigDecimal royalFlush;
  private volatile BigDecimal straightFlush;
  private volatile BigDecimal fourOfAKind;

  @Override
  public double getBetTotalValue() {
    double betTotalValue;
    betTotalValue = bet.stream()
        .map(PokerChipInfo::getDenomination)
        .flatMap(Optional::stream)
        .mapToDouble(Denomination::getValue)
        .sum();
    return betTotalValue;
  }

  @Override
  public double getPayoutTotalValue() {
    double payoutValue;
    payoutValue = payout.stream()
        .map(PokerChipInfo::getDenomination)
        .flatMap(Optional::stream)
        .mapToDouble(Denomination::getValue)
        .sum();
    return payoutValue;
  }
  
  public void prepareForNewGame() {
    // log.info("BET:{} P:{} color: {}", getBetTotalValue(), getPayoutTotalValue(), getAssociation());
    log.info("BET:{} P:{}", getBetTotalValue(), getPayoutTotalValue());

    // BET:500.0 P:500.0 color:ColorAssociation(username=Anonymous, color=NO_COLOR, chipValue=0.0) 
  }
  
  public @NonNull String getCsvLine() {
    return "%s, %.2f, %.2f".formatted(
        "Anonymous",
        getBetTotalValue(),
        getPayoutTotalValue());
  }

  public @NonNull String getPlain() {
    return "%s, %.2f, %.2f".formatted(
        "Anonymous",
        getBetTotalValue(),
        getPayoutTotalValue());
  }
}
