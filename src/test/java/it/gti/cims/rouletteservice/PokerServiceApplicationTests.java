package it.gti.cims.pokerservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.vavr.collection.Stream;
import it.gti.cims.gamecommons.client.InventoryServiceGrpcClient;
import it.gti.cims.gamecommons.entity.Card;
import it.gti.cims.gamecommons.entity.ReadingPoint;
import it.gti.cims.gamecommons.entity.readingpoint.ReadingPointConfiguration;
import it.gti.cims.gamecommons.enumeration.ReadingPointType;
import it.gti.cims.gamecommons.repository.CardRepository;
import it.gti.cims.gamecommons.repository.GameRepository;
import it.gti.cims.gamecommons.repository.ReadingPointRepository;
import it.gti.cims.gamecommons.repository.PokerTableRepository;
import it.gti.cims.gamecommons.repository.LicenseRepository;
import it.gti.cims.gamecommons.service.GameService;
// import it.gti.cims.pokerservice.entity.PokerAssociation;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.pokerservice.model.ValuePokerPlayer;
// import it.gti.cims.pokerservice.repository.PokerAssociationRepository;
import it.gti.cims.pokerservice.service.PokerGameService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@Import({
  PokerGameService.class,
})
class PokerServiceApplicationTests {

  final int ANTENNAS_NUMBER = 3;
	final String TABLE_NAME = "test";

	final Map<Integer, Set<String>> bets = new HashMap<>();
  final Set<String> sellingBox = new HashSet<>();

  @Autowired
  GameService<PokerGameMessage> gameService;

  /*
  @Autowired
  ColorAssociationService associationService;
  */

  @MockBean
  CardRepository cardRepository;
  @MockBean
  ReadingPointRepository readingPointRepository;
  @MockBean
  PokerTableRepository pokerTableRepository;
  @MockBean
  LicenseRepository licenseRepository;

  @MockBean
  GameRepository<PokerGameMessage> gameRepository;
  /*
  @MockBean
  PokerAssociationRepository associationRepository;
  */

  @MockBean
	InventoryServiceGrpcClient inventoryGrpcClient;

  // PokerAssociation association = new PokerAssociation("id", TABLE_NAME, new HashMap<>());

	@Test
  public void testGame() {
	/*
    StepVerifier.create(associationService.associateColorToUser(TABLE_NAME))
      .expectSubscription()
      .as("Start association")
      .then(() -> {
				sellingBox.add("A0032C0000019000188FA1F4");
				sellingBox.add("CC032C0000019000188FA1F4");
				sellingBox.add("A203CF000003840639FC0B99");
			})
      .thenConsumeWhile(
				message -> ObjectUtils.anyNull(message.getChipValue(), message.getColor(), message.getUsername()), 
				message -> log.info("[Association] Waiting for association complete; username: {} value: {} color: {}",
          message.getUsername(), message.getChipValue(), message.getColor()))
			.as("Association")
      .thenAwait(Duration.ofSeconds(1))
      .verifyComplete();
	*/

	/*
    assertFalse(!(association.getColorAssociations().isEmpty()));
		StepVerifier.create(gameService.getGamePublisher(TABLE_NAME))
			.expectSubscription()
			.as("Start betting")
			.then(() -> bets.put(1, Set.of("A203CF000003840639FC0B99")))
			.thenConsumeWhile(
				message -> message.getPlayers().stream()
					.mapToDouble(ValuePokerPlayer::getBetTotalValue)
					.sum() == 0,
				message -> log.info("[Betting] Waiting for bets"))
			.as("Wait bet")
			.then(() -> bets.clear())
			.thenConsumeWhile(
				message -> message.getPlayers().stream()
					.mapToDouble(ValuePokerPlayer::getBetTotalValue)
					.sum() > 0,
				message -> log.info("[New game] Waiting for new game"))
			.as("New game")
			.then(() -> gameService.closeTableGame(TABLE_NAME))
			.thenAwait(Duration.ofSeconds(1))
			.verifyComplete();
		*/
}

  @BeforeEach
	void initMocks() {
		when(readingPointRepository.findByTableName(anyString())).thenAnswer(new Answer<List<ReadingPoint>>() {

			@Override
			public List<ReadingPoint> answer(InvocationOnMock invocation) throws Throwable {
				return List.of(
          mockedSingleReadingPoint(invocation.getArgument(0))
          // mockedSellingReadingPoint(invocation.getArgument(0))
        );
			}
			
		});

		when(inventoryGrpcClient.epcInventory(anyString(), anySet(), any(), any())).thenAnswer(new Answer<Mono<Set<String>>>() {

			@Override
			public Mono<Set<String>> answer(InvocationOnMock invocation) throws Throwable {
				return Mono.fromCallable(() -> {
          Optional<String> configuration = invocation.getArgument(2);
          if (configuration.isPresent() && configuration.get().equalsIgnoreCase(ReadingPointType.SELLING_ANTENNA.toString())) {
            return sellingBox;
          }
          
					Set<Integer> antennas = invocation.getArgument(1);
					Set<String> epcs = bets.entrySet().stream()
						.filter(entry -> antennas.contains(entry.getKey()))
						.map(Entry::getValue)
						.flatMap(Set::stream)
						.collect(Collectors.toSet());

					return epcs;
				}).delayElement(Duration.ofMillis(100));
			}
			
		});

    when(cardRepository.findById(anyString())).thenAnswer(new Answer<Optional<Card>>() {

			@Override
			public Optional<Card> answer(InvocationOnMock invocation) throws Throwable {
        String epc = invocation.getArgument(0);
        if (!epc.startsWith("CC")) {
          return Optional.empty();
        }

				var card = new Card();
				card.setCardId(epc);
				card.setUsername(epc);
				
				return Optional.of(card);
			}
			
		});

		// when(associationRepository.findById(anyString())).thenReturn(Mono.just(association));
		// when(associationRepository.save(any())).thenReturn(Mono.just(association));
		when(gameRepository.save(any())).thenReturn(Mono.empty());
	}

  @NonNull ReadingPoint mockedSingleReadingPoint(@NonNull String tableName) {
		ReadingPoint readingPoint = new ReadingPoint();
		Set<Integer> antennas = Stream.range(1, ANTENNAS_NUMBER + 1).toJavaSet();
		
		readingPoint.setName("mocked");
		readingPoint.setReaderIp("mocked");
		readingPoint.setTableName(tableName);
		readingPoint.setConfiguration(new ReadingPointConfiguration(antennas));
		readingPoint.setType(ReadingPointType.SINGLE_ANTENNA.toString());

		return readingPoint;
	}

	/*

  @NonNull ReadingPoint mockedSellingReadingPoint(@NonNull String tableName) {
		ReadingPoint readingPoint = new ReadingPoint();
		Set<Integer> antennas = Stream.range(1, ANTENNAS_NUMBER + 1).toJavaSet();
		
		readingPoint.setName("mocked");
		readingPoint.setReaderIp("mocked");
		readingPoint.setTableName(tableName);
		readingPoint.setConfiguration(new ReadingPointConfiguration(antennas));
		readingPoint.setType(ReadingPointType.SELLING_ANTENNA.toString());

		return readingPoint;
	}
	*/

}
