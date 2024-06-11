package it.gti.cims.pokerservice.resolver;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import it.gti.cims.gamehistoryservice.model.pagination.GamePagedResponse;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.pokerservice.service.PokerGameQueryService;
import it.gti.cims.spring.graphql.commons.annotation.Auth;
import it.gti.cims.spring.graphql.commons.model.pagination.PageInput;

@Component
public class Query implements GraphQLQueryResolver {
  
  @Autowired
  private PokerGameQueryService service;

  public CompletableFuture<GamePagedResponse<PokerGameMessage>> pokerGames(PageInput pageRequest, DataFetchingEnvironment dfe) {
    return service.findPokerGames(pageRequest).toFuture();
  }

  public CompletableFuture<GamePagedResponse<PokerGameMessage>> pokerGamesByTable(String tableName, PageInput pageRequest, DataFetchingEnvironment dfe) {
    return service.findPokerGamesByTableName(tableName, pageRequest).toFuture();
  }
  
}
