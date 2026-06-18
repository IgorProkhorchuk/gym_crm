package com.epam.gymcrm.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

class SensitiveToStringTest {

  @Test
  void constructorShouldBePrivate() throws Exception {
    Constructor<SensitiveToString> constructor = SensitiveToString.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertThatCode(constructor::newInstance).doesNotThrowAnyException();
  }

  @Test
  void toStringShouldMaskSensitiveRecordComponents() {
    TestRecord value = new TestRecord("secret", "visible");

    assertThat(SensitiveToString.toString(value))
        .isEqualTo("TestRecord[secret=[PROTECTED], visible=visible]");
  }

  private record TestRecord(@SensitiveInfo String secret, String visible) {}
}
