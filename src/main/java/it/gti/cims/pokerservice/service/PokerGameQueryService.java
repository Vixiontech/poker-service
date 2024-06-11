package it.gti.cims.pokerservice.service;

import org.springframework.stereotype.Service;

import it.gti.cims.gamecommons.enumeration.GameType;
import it.gti.cims.gamehistoryservice.model.pagination.GamePagedResponse;
import it.gti.cims.gamehistoryservice.service.GameQueryService;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.spring.graphql.commons.model.pagination.PageInput;
import lombok.NonNull;
import reactor.core.publisher.Mono;

@Service
public class PokerGameQueryService extends GameQueryService<PokerGameMessage> {
    
  public @NonNull Mono<GamePagedResponse<PokerGameMessage>> findPokerGames(@NonNull PageInput pageRequest) {
    return findByGameType(GameType.POKER, pageRequest);
  }

  public @NonNull Mono<GamePagedResponse<PokerGameMessage>> findPokerGamesByTableName(@NonNull String tableName,
      @NonNull PageInput pageRequest) {
    return findByGameTypeAndTableName(GameType.POKER, tableName, pageRequest);
  }
  
}
