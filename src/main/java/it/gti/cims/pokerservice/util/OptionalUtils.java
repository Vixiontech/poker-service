package it.gti.cims.pokerservice.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OptionalUtils {
  
  public <T, O> Function<T, O> extract(Function<T, Optional<O>> opt) {
    return opt.andThen(Optional::orElseThrow);
  }

  public <T, O> Predicate<T> isPresent(Function<T, Optional<O>> opt) {
    return it -> opt.apply(it).isPresent();
  }

  public <T, O> Predicate<T> isEmpty(Function<T, Optional<O>> opt) {
    return it -> opt.apply(it).isEmpty();
  }
  
}
