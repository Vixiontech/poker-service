package it.gti.cims.pokerservice.phase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.gti.cims.gamecommons.Feed;
import it.gti.cims.gamecommons.Phase;
import it.gti.cims.gamecommons.enumeration.GameStatus;
import it.gti.cims.gamecommons.entity.License;
import it.gti.cims.gamecommons.model.inventoryfeed.SingleAntennaInventory;
import it.gti.cims.pokerservice.enumeration.PokerChipColor;
import it.gti.cims.pokerservice.model.PokerChipInfo;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.pokerservice.model.ValuePokerPlayer;
import it.gti.cims.pokerservice.model.ValuePokerPlayer;
import it.gti.cims.pokerservice.util.OptionalUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class BettingPhase extends Phase<PokerGameMessage> {

  private final Flux<PokerGameMessage> publisher;

  private int lastInventorySize;
  License localLicense;

  // public BettingPhase(int phaseNumber, @NonNull Feed<Set<SingleAntennaInventory>> inventoryFeed) {
  public BettingPhase(int phaseNumber, @NonNull Feed<Set<SingleAntennaInventory>> inventoryFeed, License lastLicense) {  
    super(phaseNumber, inventoryFeed);
    localLicense = lastLicense;
    this.publisher = Flux.<PokerGameMessage>create(emitter -> {
      this.subscriptions.add(inventoryFeed.getPublisher().subscribe(it -> inventoryHandler(it, emitter)));
    }).subscribeOn(Schedulers.boundedElastic())
      .doFinally(ignore -> this.dispose());
  }

  private void inventoryHandler(Set<SingleAntennaInventory> inventories, FluxSink<PokerGameMessage> emitter) {
    log.info("[{}] Received message from inventoryFeed", phaseNumber);
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
      player.setBet(valueChips);
      players.put(PokerChipColor.NO_COLOR.ordinal(), player);
    }

    sendMessage(emitter);
  }

  private void sendMessage(FluxSink<PokerGameMessage> emitter) {
    log.info("[{}] Sending message", phaseNumber);

    PokerGameMessage message = new PokerGameMessage();
    message.setCurrentGameStatus(GameStatus.PLAYING);
    message.setCurrentPhaseNumber(this.phaseNumber);
    message.setLicense(localLicense);
    // log.info("betting License is :{}", localLicense);
    Set<ValuePokerPlayer> pokerPlayers = players.values().stream()
      .map(ValuePokerPlayer.class::cast)
      .collect(Collectors.toSet());

    message.setPlayers(pokerPlayers);

    // log.debug("[{}] Message: {}", phaseNumber, message);
    emitter.next(message);
  }

}
