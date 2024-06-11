package it.gti.cims.pokerservice.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.gti.cims.gamecommons.Feed;
import it.gti.cims.gamecommons.Game;
import it.gti.cims.gamecommons.Phase;
import it.gti.cims.gamecommons.client.InventoryServiceGrpcClient;
import it.gti.cims.gamecommons.entity.ReadingPoint;
import it.gti.cims.gamecommons.enumeration.ReadingPointType;
import it.gti.cims.gamecommons.feed.GameFrameFeed;
import it.gti.cims.gamecommons.feed.InventoryFeed;
import it.gti.cims.gamecommons.model.cardfeed.GameFrame;
import it.gti.cims.gamecommons.model.inventoryfeed.SingleAntennaInventory;
import it.gti.cims.gamecommons.repository.GameRepository;
import it.gti.cims.gamecommons.repository.ReadingPointRepository;
import it.gti.cims.gamecommons.service.GameService;
import it.gti.cims.pokerservice.game.PokerGame;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.pokerservice.phase.BettingPhase;
import it.gti.cims.pokerservice.phase.PayoutPhase;
import it.gti.cims.gamecommons.entity.PokerTable;
import it.gti.cims.gamecommons.repository.PokerTableRepository;
import it.gti.cims.gamecommons.repository.LicenseRepository;
import it.gti.cims.gamecommons.entity.License;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PokerGameService extends GameService<PokerGameMessage> {


  @Autowired
  private GameRepository<PokerGameMessage> pokerGameRepository;
  @Autowired
  private ReadingPointRepository readingPointRepository;
  @Autowired
  private InventoryServiceGrpcClient inventoryClient;

  @Autowired
  private PokerTableRepository tableRepository;

  @Autowired
  private LicenseRepository licenseRepository;

  @Override
  protected Game<PokerGameMessage> generateGame(@NonNull String tableName, String userName) {
    
    log.info("Generating poker game at table {} with this worker:{}", tableName, userName);
    
    PokerGame game = new PokerGame(tableName, pokerGameRepository, () -> this.phaseGenerator(tableName, userName));
    game.nextPhase();

    return game;
  }
  /*
  @Override
  protected Game<PokerGameMessage> generatePokerGame(@NonNull String tableName, String userName) {
    log.info("Generating poker game at table {} with worker {}", tableName, userName);
    
    PokerGame game = new PokerGame(tableName, pokerGameRepository, () -> this.phaseGenerator(tableName, userName));
    game.nextPhase();

    return game;
  }
  */
  @SneakyThrows
  private Collection<Phase<PokerGameMessage>> phaseGenerator(String tableName, String userName) {
    log.info("Entering phase generator table {}", tableName);
    ReadingPoint potReadingPoint = readingPointRepository.findByTableName(tableName).stream()
      .filter(rp -> rp.getType().equals(ReadingPointType.SINGLE_ANTENNA.toString()))
      .findAny().orElseThrow();
    log.info("potReadingPoint {}", potReadingPoint);
    List<License> licenseList = licenseRepository.findAll();
    License lastLicense = licenseList.get(licenseList.size() - 1);
    log.info("lastLicense: {}", lastLicense);
    Feed<Set<SingleAntennaInventory>> bettingInventoryFeed = new InventoryFeed(
      potReadingPoint.getReaderIp(), potReadingPoint.getConfiguration().getAntennas(), inventoryClient);
    Feed<Set<SingleAntennaInventory>> payoutInventoryFeed = new InventoryFeed(
      potReadingPoint.getReaderIp(), potReadingPoint.getConfiguration().getAntennas(), inventoryClient);

    Optional<PokerTable> pokerTable = tableRepository.findById(tableName);
    
    BettingPhase bettingPhase = new BettingPhase(0, bettingInventoryFeed, lastLicense);
    PayoutPhase payoutPhase = new PayoutPhase(1, payoutInventoryFeed, pokerTable, userName, lastLicense);
    log.info("phases are now created");
    /*
    if (betReadingPoint.getReaderIp().equals(payoutReadingPoint.getReaderIp())) {
      Set<Integer> payoutAntennas = new HashSet<>(payoutReadingPoint.getConfiguration().getAntennas());
      payoutAntennas.addAll(betReadingPoint.getConfiguration().getAntennas());
  
      Feed<Set<SingleAntennaInventory>> payoutInventoryFeed = new InventoryFeed(
        payoutReadingPoint.getReaderIp(), payoutAntennas, inventoryClient);

      payoutPhase = new PayoutPhase(1, payoutInventoryFeed, payoutReadingPoint.getConfiguration().getAntennas());
    } else {
      Feed<Set<SingleAntennaInventory>> payoutInventoryFeed = new InventoryFeed(
        payoutReadingPoint.getReaderIp(), payoutReadingPoint.getConfiguration().getAntennas(), inventoryClient);
      Feed<Set<SingleAntennaInventory>> payoutBetBoxInventoryFeed = new InventoryFeed(
        betReadingPoint.getReaderIp(), betReadingPoint.getConfiguration().getAntennas(), inventoryClient);

      payoutPhase = new PayoutPhase(1, payoutInventoryFeed, payoutBetBoxInventoryFeed);
    }
    */

    return List.of(bettingPhase, payoutPhase);
  }
  
}
