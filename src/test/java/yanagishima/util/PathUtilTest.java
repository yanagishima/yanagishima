package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PathUtilTest {
  private static final String CURRENT_PATH = new File(".").getAbsolutePath();

  @BeforeEach
  void setUp() throws Exception {
    Files.deleteIfExists(Paths.get(CURRENT_PATH, "test"));
  }

  @Test
  void testGetResultFilePath() throws IOException {
    Path expected = Paths.get(CURRENT_PATH, "result/test/20190102/20190102_abcdef.tsv");
    Path actual = PathUtil.getResultFilePath("test", "20190102_abcdef", false);
    assertEquals(expected, actual);
    assertTrue(Files.deleteIfExists(actual.getParent()));

    expected = Paths.get(CURRENT_PATH, "result/test/20190102/20190102_abcdef.err");
    actual = PathUtil.getResultFilePath("test", "20190102_abcdef", true);
    assertEquals(expected, actual);
    assertTrue(Files.deleteIfExists(actual.getParent()));
  }
}
