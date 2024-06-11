package it.gti.cims.pokerservice.resolver.model;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import it.gti.cims.pokerservice.service.PokerGameQueryService;
import it.gti.cims.gamehistoryservice.model.GenericTableModel;
import it.gti.cims.gamehistoryservice.model.pagination.GamePagedResponse;
import it.gti.cims.spring.graphql.commons.annotation.Auth;
import it.gti.cims.spring.graphql.commons.model.pagination.PageInput;

@Component
public class GenericTableResolver implements GraphQLResolver<GenericTableModel> {

  @Autowired
  private PokerGameQueryService service;

  public CompletableFuture<GamePagedResponse<PokerGameMessage>> pokerGames(GenericTableModel table, PageInput pageRequest, DataFetchingEnvironment dfe) {
    return service.findPokerGamesByTableName(table.getName(), pageRequest).toFuture();
  }
  
}
