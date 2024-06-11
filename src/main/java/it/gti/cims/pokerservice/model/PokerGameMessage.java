package it.gti.cims.pokerservice.model;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import it.gti.cims.gamecommons.model.game.GameMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class PokerGameMessage extends GameMessage<ValuePokerPlayer> {

  @NonNull
  private Set<ValuePokerPlayer> players = new ConcurrentSkipListSet<>();
  @NonNull
  private String userName;
  
  public PokerGameMessage() {
    super();
  }
  
}
