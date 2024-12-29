package ssw.mj.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ssw.mj.Errors.Message;
import ssw.mj.test.support.BaseCompilerTestCase;
import ssw.mj.test.support.Configuration;

import static ssw.mj.Errors.Message.*;

@Timeout(value = Configuration.TIMEOUT)
public class RecoverTest extends BaseCompilerTestCase {
  @Test
  public void wrongGlobalDecl() {
    initCode("program Test" + LF + //
            "  123;" + LF + //
            "{ void main() { } }");
    expectError(2, 3, INVALID_DECL);
    parseVerifyVisualize();
  }

  @Test
  public void wrongMethDecl1() {
    initCode("program Test {" + LF + //
            "  void main() { }" + LF + //
            "  program wrong1() { " + LF + //
            "    if (1>2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 3, INVALID_METH_DECL);
    parseVerifyVisualize();
  }

  @Test
  public void wrongMethDecl2() {
    initCode("program Test {" + LF + //
            "  program wrong1() { " + LF + //
            "    if (1>2);" + LF + //
            "  }" + LF + //
            "  void main() { }" + LF + //
            "  program wrong2() {" + LF + //
            "    if (1>2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(2, 3, INVALID_METH_DECL);
    expectError(6, 3, INVALID_METH_DECL);
    parseVerifyVisualize();
  }

  @Test
  public void wrongMethDecl3() {
    initCode("program Test {" + LF + //
            "  program wrong1() { }" + LF + //
            "  void main() { }" + LF + //
            "  program wrong2() { }" + LF + //
            "}");
    expectError(2, 3, INVALID_METH_DECL);
    expectError(4, 3, INVALID_METH_DECL);
    parseVerifyVisualize();
  }

  @Test
  public void wrongStat() {
    initCode("program Test {" + LF + //
            "  void main() {  " + LF + //
            "    123;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 5, INVALID_STAT);
    parseVerifyVisualize();
  }

  @Test
  public void multipleErrors() {
    initCode("program Test " + LF + //
            "  int x" + LF + //
            "{" + LF + //
            "  void main( {" + LF + //
            "    if (1 x 2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 1, TOKEN_EXPECTED, ";");
    expectError(4, 14, TOKEN_EXPECTED, ")");
    expectError(5, 11, REL_OP);
    parseVerifyVisualize();
  }

  // ---- multiple errors & recovery
  @Test
  public void noRecover1() {
    initCode("program Test {" + LF + //
            "  void main this method will never recover");

    expectError(2, 13, TOKEN_EXPECTED, "(");
    parseVerifyVisualize();
  }

  @Test
  public void noRecover2() {
    initCode("program Test {" + LF + //
            "  void main() {  " + LF + //
            "    if this method will never recover");

    expectError(3, 8, TOKEN_EXPECTED, "(");
    parseVerifyVisualize();
  }

  @Test
  public void recoverDecl1() {
    initCode("program Test" + LF + //
            "  int i1, if" + LF + //
            "  in i2;" + LF + //
            "  final int i3 = 0;" + LF + //
            "{" + LF + //
            "  void main() {  " + LF + //
            "    if (i1 < i3);" + LF + //
            "  }" + LF + //
            "}");

    expectError(2, 11, TOKEN_EXPECTED, "identifier");
    parseVerifyVisualize();
  }

  @Test
  public void recoverDecl2() {
    initCode("program Test" + LF + //
            "  int i1, if" + LF + //
            "  in i2;" + LF + //
            "  int i3;" + LF + //
            "{" + LF + //
            "  void main() {  " + LF + //
            "    if (i1 < i3);" + LF + //
            "  }" + LF + //
            "}");

    expectError(2, 11, TOKEN_EXPECTED, "identifier");
    parseVerifyVisualize();
  }

  @Test
  public void recoverStat() {
    initCode("program Test {" + LF + //
            "  void main() {  " + LF + //
            "    567 since distance stays too small no follow up errors here;" + LF + //
            "    if (1 < 2);" + LF + //
            "    if (1 x 2);" + LF + //
            "  }" + LF + //
            "}");

    expectError(3, 5, INVALID_STAT);
    expectError(5, 11, REL_OP);
    parseVerifyVisualize();
  }

  @Test
  public void resetErrDist() {
    initCode("program Test {" + LF + //
            "  void main() {" + LF + //
            "    if () if () if();" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 9, INVALID_FACT);
    expectError(3, 15, INVALID_FACT);
    expectError(3, 20, INVALID_FACT);
    parseVerifyVisualize();
  }

  @Test
  public void illegalMethodStart() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            "  void foo()" + LF + // 3
            "  void foo(char x) { }" + LF + // 4
            "  void main() { }" + LF + // 5
            "}" + LF // 6
    );
    expectError(4, 3, Message.TOKEN_EXPECTED, "{");
    parseVerifyVisualize();
  }
}
