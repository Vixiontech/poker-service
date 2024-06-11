package it.gti.cims.pokerservice.phase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import it.gti.cims.chipencodingcommons.enumeration.Denomination;
import it.gti.cims.gamecommons.Feed;
import it.gti.cims.gamecommons.Phase;
import it.gti.cims.gamecommons.enumeration.GameStatus;
import it.gti.cims.gamecommons.model.inventoryfeed.SingleAntennaInventory;
import it.gti.cims.gamecommons.entity.PokerTable;
import it.gti.cims.gamecommons.entity.License;
import it.gti.cims.pokerservice.enumeration.PokerChipColor;
import it.gti.cims.pokerservice.model.PokerChipInfo;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.pokerservice.model.ValuePokerPlayer;
import it.gti.cims.pokerservice.util.OptionalUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Getter
@EqualsAndHashCode(callSuper = false)
public class PayoutPhase extends Phase<PokerGameMessage> {

  Optional<PokerTable> localTable;
  String workingWorker = "";
  License localLicense;

  private final Flux<PokerGameMessage> publisher;

  private boolean betBoxesCleared = false;
  // public PayoutPhase(int phaseNumber, @NonNull Feed<Set<SingleAntennaInventory>> inventoryFeed, Optional<PokerTable> pokerTable, String userName) {
  public PayoutPhase(int phaseNumber, @NonNull Feed<Set<SingleAntennaInventory>> inventoryFeed, Optional<PokerTable> pokerTable, String userName, License lastLicense) {
    super(phaseNumber, inventoryFeed);
    localTable = pokerTable;
    workingWorker = userName;
    localLicense = lastLicense;
    this.publisher = Flux.<PokerGameMessage>create(emitter -> {
      this.subscriptions.add(inventoryFeed.getPublisher().subscribe(it -> inventoryHandler(it, emitter)));
    }).subscribeOn(Schedulers.boundedElastic())
      .doFinally(ignore -> {
        players.values().forEach(player -> {
          ValuePokerPlayer valuePokerPlayer = (ValuePokerPlayer) player;
          valuePokerPlayer.prepareForNewGame();
        });

        this.dispose();
      });
  }

  private void inventoryHandler(Set<SingleAntennaInventory> inventories, FluxSink<PokerGameMessage> emitter) {
    Set<PokerChipInfo> pokerInventory = inventories.stream()
      .map(SingleAntennaInventory::getInventory)
      .flatMap(Collection::stream)
      .map(PokerChipInfo::fromChipInfo)
      .distinct()
      .collect(Collectors.toSet());


    List<PokerChipInfo> valueChips = pokerInventory.stream()
      .filter(OptionalUtils.isEmpty(PokerChipInfo::getChipColor))
      .filter(OptionalUtils.isPresent(PokerChipInfo::getDenomination))
      .toList();

    // log.info("ZZZZ77ZZZZZ valueChips= {}",valueChips);

    if (!valueChips.isEmpty()) {
      ValuePokerPlayer player = (ValuePokerPlayer) players.getOrDefault(PokerChipColor.NO_COLOR.ordinal(), new ValuePokerPlayer());
      player.setNumber(PokerChipColor.NO_COLOR.ordinal());
      player.setPayout(valueChips);


      double potInvValue = pokerInventory.stream()
        .map(PokerChipInfo::getDenomination)
        .flatMap(Optional::stream)
        .mapToDouble(Denomination::getValue)
        .sum();

      BigDecimal potInBigDecimal = new BigDecimal(potInvValue);
      BigDecimal rake = (potInBigDecimal.multiply(localTable.get().getRakePercentage())).divide(new BigDecimal(100));
      BigDecimal progressive;
      if (localTable.get().isFixedProgressive()){
        progressive = localTable.get().getProgressive();
        player.setFixedProgressive(true);
      } else {
        BigDecimal tempPotMinusRake = potInBigDecimal.subtract(rake);
        progressive = (tempPotMinusRake.multiply(localTable.get().getProgressive())).divide(new BigDecimal(100));
        player.setFixedProgressive(false);
      }
      progressive = progressive.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      BigDecimal houseHold = (progressive.multiply(new BigDecimal(localTable.get().getHouseHold()))).divide(new BigDecimal(100));
      houseHold = houseHold.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      BigDecimal contribution = progressive.subtract(houseHold);
      contribution = contribution.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      BigDecimal royalFlush = (contribution.multiply(new BigDecimal(localTable.get().getRoyalFlush()))).divide(new BigDecimal(100));
      royalFlush = royalFlush.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      BigDecimal straightFlush = (contribution.multiply(new BigDecimal(localTable.get().getStraightFlush()))).divide(new BigDecimal(100));
      straightFlush = straightFlush.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      BigDecimal fourOfAKind = (contribution.multiply(new BigDecimal(localTable.get().getFourOfAKind()))).divide(new BigDecimal(100));
      fourOfAKind = fourOfAKind.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      log.info("rake : {}, progressive : {}, houseHold : {}, royalFlush : {}, straightFlush : {}, fourOfAKind : {}", rake, progressive, houseHold, royalFlush, straightFlush, fourOfAKind);
      

      player.setRakePercentage(rake);
      player.setProgressive(progressive);
      player.setHouseHold(houseHold);
      player.setRoyalFlush(royalFlush);
      player.setStraightFlush(straightFlush);
      player.setFourOfAKind(fourOfAKind);
      players.put(PokerChipColor.NO_COLOR.ordinal(), player);
    }

    sendMessage(emitter);

  }

  private void sendMessage(FluxSink<PokerGameMessage> emitter) {
    log.info("[{}] Sending message", phaseNumber);
    
    PokerGameMessage message = new PokerGameMessage();
    message.setCurrentGameStatus(GameStatus.PLAYING);
    message.setCurrentPhaseNumber(this.phaseNumber);
    message.setUserName(workingWorker);
    message.setLicense(localLicense);
    log.info("payout License is :{}", localLicense);
    Set<ValuePokerPlayer> pokerPlayers = players.values().stream()
      .map(ValuePokerPlayer.class::cast)
      .collect(Collectors.toSet());
    
    message.setPlayers(pokerPlayers);
    
    // log.info("[{}] Message: {}", phaseNumber, message);
    emitter.next(message);
  }

}
