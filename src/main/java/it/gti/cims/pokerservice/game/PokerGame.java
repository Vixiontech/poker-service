package it.gti.cims.pokerservice.game;

import java.util.Collection;
import java.util.concurrent.Callable;

import it.gti.cims.gamecommons.Game;
import it.gti.cims.gamecommons.Phase;
import it.gti.cims.gamecommons.enumeration.GameType;
import it.gti.cims.gamecommons.repository.GameRepository;
import it.gti.cims.pokerservice.model.PokerGameMessage;
import lombok.NonNull;

public class PokerGame extends Game<PokerGameMessage> {

  public PokerGame(@NonNull String tableName, GameRepository<PokerGameMessage> repository,
      @NonNull Callable<Collection<Phase<PokerGameMessage>>> phasesGenerator) {
    super(tableName, true, repository, phasesGenerator);
  }

  @Override
  protected PokerGameMessage generateDefaultMessage() {
    return new PokerGameMessage();
  }

  @Override
  public GameType getGameType() {
    return GameType.POKER;
  }
  
}
