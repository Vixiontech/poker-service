package it.gti.cims.pokerservice.enumeration;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public enum PokerChipColor {

  NO_COLOR(100000);

  @Getter
  private final int code;

  public static @NonNull Optional<PokerChipColor> fromCode(int code) {
    return Stream.of(PokerChipColor.values())
      .filter(it -> it.getCode() == code)
      .findAny();
  }

}
