package ssw.mj.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ssw.mj.scanner.Token;
import ssw.mj.test.support.BaseCompilerTestCase;
import ssw.mj.test.support.Configuration;

import static ssw.mj.Errors.Message.*;

@Timeout(value = Configuration.TIMEOUT)
public class ParserTest extends BaseCompilerTestCase {

  @Test
  public void testWorkingFinalDecls() {
    initCode("program Test" + LF + // 1
            "  final int i = 1;" + LF + // 2
            "  final int j = 1;" + LF + // 3
            "  final int k = 1;" + LF + // 4
            "{ void main() { } }"); // 5
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingDecls() {
    initCode("program Test" + LF + // 1
            "  int i;" + LF + // 2
            "  int j, k;" + LF + // 3
            "{ void main() { } }"); // 4
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethods() {
    initCode("program Test" + LF + // 1
            "  int i;" + LF + // 2
            "  int j, k;" + LF + // 3
            "{" + LF + // 4
            " void foo() { }" + LF + // 5
            " void bar() { }" + LF + // 6
            " void main() { }" + LF + // 7
            " }" + LF // 8
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethodsWithParameters() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(int i) { }" + LF + // 3
            " void bar(int i, char c) { }" + LF + // 4
            " void main() { }" + LF + // 5
            " }" + LF // 6
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethodsWithLocals() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo() int i; { }" + LF + // 3
            " void bar() int i; char c; { }" + LF + // 4
            " void main() { }" + LF + // 5
            " }" + LF // 6
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethodsWithParametersAndLocals() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch) int i; { }" + LF + // 3
            " void bar(int x, int y) int i; char c; { }" + LF + // 4
            " void main() { }" + LF + // 5
            " }" + LF // 6
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethodCall() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch) int i; { }" + LF + // 3
            " void main() { foo('a'); }" + LF + // 4
            " }" + LF // 5
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethodCallTwoParams() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch, int x) int i; { }" + LF + // 3
            " void main() { foo('a', 1); }" + LF + // 4
            " }" + LF // 5
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingMethodCallThreeParams() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch, int x, char ch2) int i; { }" + LF + // 3
            " void main() { foo('a', 1, 'b'); }" + LF + // 4
            " }" + LF // 5
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingClass() {
    initCode("program Test" + LF + // 1
            "class X { int i; int j; }" + LF + // 2
            "{" + LF + // 3
            " void main() X x; { x = new X; }" + LF + // 4
            "}" + LF // 5
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingArray() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            "  void main() int[] x; { x = new int[10]; }" + LF + // 3
            "}" + LF // 4
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingIncDec() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            " void main() int i; { i--; i++; }" + LF + // 3
            " }" + LF // 4
    );
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingElseIf() {
    initCode("program Test {" + LF + // 1
            "  void main() int i; {" + LF + // 2
            "    if (i > 10) i++;" + LF + // 3
            "    else if (i < 5) i--;" + LF + // 4
            "    else i += 8;" + LF + // 5
            "  }" + LF + // 6
            "}");
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingLoop() {
    initCode("program Test {" + LF + // 1
            "  void main () int i; {" + LF + // 2
            "    i = 0;" + LF + // 3
            "    while (i < 42) {" + LF + // 4
            "	   i++;" + LF + // 5
            "    }" + LF + // 6
            "  }" + LF + // 7
            "}");
    parseVerifyVisualize();
  }

  @Test
  public void mulAssign() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 2;" + LF + //
            "    i *= 2;" + LF + //
            "  }" + LF + //
            "}");
    parseVerifyVisualize();
  }

  @Test
  public void returnExpr() {
    initCode("program Test {" + LF + //
            "  void main() { }" + LF + //
            "  int wrong1() { " + LF + //
            "    return 2 + 3;" + LF + //
            "  }" + LF + //
            "}");
    parseVerifyVisualize();
  }

  @Test
  public void wrongConstDecl() {
    initCode("program Test" + LF + //
            "  final int i = a;" + LF + //
            "{ void main() { } }");
    expectError(2, 17, CONST_DECL);
    parseVerifyVisualize();
  }

  @Test
  public void wrongDesignFollow() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i**;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 6, DESIGN_FOLLOW);
    parseVerifyVisualize();
  }

  @Test
  public void wrongFactor() {
    initCode("program Test {" + LF + //
            "  void main () int i; {  " + LF + //
            "    i = i + if;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 13, INVALID_FACT);
    parseVerifyVisualize();
  }

  @Test
  public void wrongRelOp() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    if (i x 5);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 11, REL_OP);
    parseVerifyVisualize();
  }

  @Test
  public void wrongStart() {
    initCode("noprogram Test { }");
    expectError(1, 1, TOKEN_EXPECTED, "program");
    parseVerifyVisualize();
  }

  @Test
  public void noProgName() {
    initCode("program { }");
    expectError(1, 9, TOKEN_EXPECTED, "identifier");
    parseVerifyVisualize();
  }

  @Test
  public void wrongVarDecl() {
    initCode("program Test " + LF + //
            "int var1,,,,var2;" + LF + //
            "{ void main() { } }");
    expectError(2, 10, TOKEN_EXPECTED, Token.Kind.ident.label());
    parseVerifyVisualize();
  }

  @Test
  public void eofExpected() {
    initCode("program Test {" + LF + //
            "  void main() {}" + LF + //
            "}moretext");
    expectError(3, 2, TOKEN_EXPECTED, "end of file");
    parseVerifyVisualize();
  }

  @Test
  public void invalidEOF1() {
    initCode("program Test {" + LF + //
            "  void main() {");
    expectError(2, 16, TOKEN_EXPECTED, "}");
    parseVerifyVisualize();
  }

  @Test
  public void invalidEOF2() {
    initCode("program Test {" + LF + //
            "  void main() {" + LF + //
            "    if ()");
    expectError(3, 9, INVALID_FACT);
    parseVerifyVisualize();
  }

  @Test
  public void invalidEOF3() {
    initCode("program Test" + LF + //
            "  class C {" + LF + //
            "    int i");
    expectError(3, 10, TOKEN_EXPECTED, ";");
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingReadAndPrint() {
    initCode("program Test {" + LF + // 1
            " void main() int i; {" + LF + // 2
            " read(i);" + LF +
            " print(i);" + LF +
            " }" + LF + //3
            "}");//4
    parseVerifyVisualize();
  }

  @Test
  public void
  testWorkingGlobalInitializersAllWithInitValue() {
    initCode("""
            program Test
              int i = 5;
              int j, k = 12;
            {
              void main() {}
            }""");
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingGlobalInitializersMixedInitValues() {
    initCode("""
            program Test
              int i = 5;
              int j, k;
              char l, m = 'n';
              char o;
            {
              void main() {}
            }""");
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingSingleton() {
    initCode("""
            program Test
              singleton Math { int PI; int E; }
              {
                void main() int i; { i = Math.PI; }
              }""");
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingSingletonWithInitializers() {
    initCode("""
            program Test
              singleton Math { int PI; int E; }(3, 2)
            {
              void main() int i; { i = Math.PI * Math.E; }
            }""");
    parseVerifyVisualize();
  }

  @Test
  public void testWorkingSingletonWithExpressionInitializers() {
    initCode("""
            program Test
              final int k = 42;
              int m = 13;
              int[] arr = new int[5];
              singleton S { int i; char c; int len; } (k * m, chr(k - m), len(arr))
              int res;
            {
              void main() int a; char b; {
                a = S.i;
                b = S.c;
                res = a * a;
                print(a);
                print(b);
                print(S.len);
              }
            }""");
    parseVerifyVisualize();
  }
}
