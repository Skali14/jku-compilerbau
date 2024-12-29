package ssw.mj.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ssw.mj.test.support.BaseCompilerTestCase;
import ssw.mj.test.support.Configuration;

import static ssw.mj.Errors.Message.*;

@Timeout(value = Configuration.TIMEOUT)
public class CodeGenerationTest extends BaseCompilerTestCase {

  /**
   * Symbol table for most examples of this test class.
   */
  private void expectExampleSymTab() {
    expectSymTabUniverse();
    expectSymTab("Program A:");
    expectSymTab("  Method: void <clinit> (0 locals, 0 parameters)");
    expectSymTab("  Constant: int max = 12");
    expectSymTab("  Global Variable 0: char c");
    expectSymTab("  Global Variable 1: int i");
    expectSymTab("  Type B: class (2 fields)");
    expectSymTab("    Local Variable 0: int x");
    expectSymTab("    Local Variable 1: int y");
    expectSymTab("  Method: void main (3 locals, 0 parameters)");
    expectSymTab("    Local Variable 0: int[] iarr");
    expectSymTab("    Local Variable 1: class (2 fields) b");
    expectSymTab("    Local Variable 2: int n");
  }

  private void expectSymTabWithSum() {
    expectSymTabUniverse();
    expectSymTab("Program A:");
    expectSymTab("  Method: void <clinit> (0 locals, 0 parameters)");
    expectSymTab("  Constant: int max = 12");
    expectSymTab("  Global Variable 0: char c");
    expectSymTab("  Global Variable 1: int i");
    expectSymTab("  Type B: class (2 fields)");
    expectSymTab("    Local Variable 0: int x");
    expectSymTab("    Local Variable 1: int y");
    expectSymTab("  Method: void main (4 locals, 0 parameters)");
    expectSymTab("    Local Variable 0: int[] iarr");
    expectSymTab("    Local Variable 1: class (2 fields) b");
    expectSymTab("    Local Variable 2: int n");
    expectSymTab("    Local Variable 3: int sum");
  }

  @Test
  public void bsp11() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    read(i); " + LF + //
            "    if (i <= n) n = 1;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectExampleSymTab();
    addExpectedRun("0", "1");
    addExpectedRun("1", "0");
    parseVerifyVisualize();
  }

  @Test
  public void bsp12() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    read(i); " + LF + //
            "    n = 1; " + LF + //
            "    if (i <= n && n < 0) n = 2;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectExampleSymTab();
    addExpectedRun("0", "1");
    addExpectedRun("2", "1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp13() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    read(i); " + LF + //
            "    n = 1; " + LF + //
            "    if (i <= n || i < 10) n = 2;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectExampleSymTab();
    addExpectedRun("0", "2");
    addExpectedRun("2", "2");
    addExpectedRun("20", "1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp14() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    read(i); " + LF + //
            "    n = 1; " + LF + //
            "    if (i <= n || i < 10 && i > 5) n = 2;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectExampleSymTab();
    addExpectedRun("0", "2");
    addExpectedRun("2", "1");
    addExpectedRun("6", "2");
    addExpectedRun("20", "1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp15() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    read(n); " + LF + //
            "    while (i <= n) { i++; }" + LF + //
            "    print(i); " + LF + //
            "  }" + LF + //
            "}");

    expectExampleSymTab();
    addExpectedRun("0", "1");
    addExpectedRun("-1", "0");
    addExpectedRun("1", "2");
    addExpectedRun("10", "11");
    parseVerifyVisualize();
  }

  @Test
  public void bsp16() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    read(i); " + LF + //
            "    if (i <= max) n = 1; else n = 2;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectExampleSymTab();
    addExpectedRun("0", "1");
    addExpectedRun("13", "2");
    addExpectedRun("12", "1");
    addExpectedRun("-1", "1");
    addExpectedRun("-13", "1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp17() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n; int sum;" + LF + //
            "  {" + LF + //
            "    read(n); " + LF + //
            "    sum = 0; " + LF + //
            "    while (i <= n) { sum += i; i++; }" + LF + //
            "    print(sum); " + LF + //
            "  }" + LF + //
            "}");

    expectSymTabWithSum();
    addExpectedRun("0", "0");
    addExpectedRun("-1", "0");
    addExpectedRun("1", "1");
    addExpectedRun("10", "55");
    parseVerifyVisualize();
  }

  @Test
  public void bsp18() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n; int sum;" + LF + //
            "  {" + LF + //
            "    read(n); " + LF + //
            "    sum = 0; " + LF + //
            "    i = 2;" + LF + //
            "    while (i <= n) { sum += i; i++; }" + LF + //
            "    print(sum); " + LF + //
            "  }" + LF + //
            "}");

    expectSymTabWithSum();
    addExpectedRun("0", "0");
    addExpectedRun("-1", "0");
    addExpectedRun("1", "0");
    addExpectedRun("10", "54");
    parseVerifyVisualize();
  }

  @Test
  public void methodCall() {
    initCode("program A" + LF + // 1
            "{" + LF + // 2
            "  void bar() {" + LF + // 3
            "    print('b');" + LF + // 4
            "    print('a');" + LF + // 5
            "    print('r');" + LF + // 6
            "  }" + LF + // 7
            "  void foo() {" + LF + // 8
            "    print('f');" + LF + // 9
            "    print('o');" + LF + // 10
            "    print('o');" + LF + // 11
            "  }" + LF + // 12
            "  void main () {" + LF + // 13
            "    foo();" + LF + // 14
            "  }" + LF + // 15
            "}"); // 16

    addExpectedRun("", "foo");
    parseVerifyVisualize();
  }

  @Test
  public void fib() {
    initCode("program A" + LF + //
            "{" + LF + //
            "  int fib(int n) {" + LF + //
            "     if (n <= 1) return 1; " + LF + //
            "     return fib(n-1) + fib(n-2); " + LF + //
            "  }" + LF + //
            "  void main ()" + LF + //
            "    int n;" + LF + //
            "  {" + LF + //
            "    read(n); " + LF + //
            "    print(fib(n)); " + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("-1", "1");
    addExpectedRun("0", "1");
    addExpectedRun("1", "1");
    addExpectedRun("2", "2");
    addExpectedRun("3", "3");
    addExpectedRun("4", "5");
    addExpectedRun("5", "8");
    addExpectedRun("6", "13");
    addExpectedRun("7", "21");
    addExpectedRun("8", "34");
    addExpectedRun("9", "55");
    addExpectedRun("10", "89");
    addExpectedRun("11", "144");
    addExpectedRun("22", "28657");
    parseVerifyVisualize();
  }

  @Test
  public void fibDyn() {
    initCode("program A" + LF + //
            " int[] matrix; " + LF + //
            "{" + LF + //
            "  int fib(int n) int r; {" + LF + //
            "     if (n <= 1) return 1; " + LF + //
            "     if(matrix[n] != 0) return matrix[n]; " + LF + //
            "     r = fib(n-1) + fib(n-2); " + LF + //
            "     matrix[n] = r; " + LF + //
            "     return r; " + LF + //
            "  }" + LF + //
            "  void main ()" + LF + //
            "    int n;" + LF + //
            "  {" + LF + //
            "    matrix = new int[1000]; " + LF + //
            "    read(n); " + LF + //
            "    print(fib(n)); " + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("-1", "1");
    addExpectedRun("0", "1");
    addExpectedRun("1", "1");
    addExpectedRun("2", "2");
    addExpectedRun("3", "3");
    addExpectedRun("4", "5");
    addExpectedRun("5", "8");
    addExpectedRun("6", "13");
    addExpectedRun("7", "21");
    addExpectedRun("8", "34");
    addExpectedRun("9", "55");
    addExpectedRun("10", "89");
    addExpectedRun("11", "144");
    addExpectedRun("22", "28657");
    addExpectedRun("30", "1346269");
    addExpectedRun("40", "165580141");
    addExpectedRun("45", "1836311903");
    parseVerifyVisualize();
  }

  @Test
  public void testElseIf() {
    initCode("program Test {" + LF + // 1
            "  void main() int i; {" + LF + // 2
            "    read(i);" + LF + // 3
            "    if (i == 1) print(9);" + LF + // 4
            "    else if (i == 2) print(8);" + LF + // 5
            "    else print(7);" + LF + // 6
            "  }" + LF + // 7
            "}");
    addExpectedRun("1", "9");
    addExpectedRun("2", "8");
    addExpectedRun("3", "7");
    addExpectedRun("4", "7");
    parseVerifyVisualize();
  }

  @Test
  public void mainVar() {
    initCode("program Test" + LF + //
            "  int main;" + LF + //
            "{" + LF + //
            "}");
    expectError(4, 2, MAIN_NOT_FOUND);
    parseVerifyVisualize();
  }

  @Test
  public void mainNotVoid() {
    initCode("program Test {" + LF + //
            "  char main() { }" + LF + //
            "}");
    expectError(2, 15, MAIN_NOT_VOID);
    parseVerifyVisualize();
  }

  @Test
  public void noLoop() {
    initCode("program Test {" + LF + //
            "  void main() {" + LF + //
            "    break;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 10, NO_LOOP);
    parseVerifyVisualize();
  }

  @Test
  public void returnVoid() {
    initCode("program Test {" + LF + //
            "  void test() {" + LF + //
            "    return 5;" + LF + //
            "  }" + LF + //
            "  void main() {}" + LF + //
            "}");
    expectError(3, 12, RETURN_VOID);
    parseVerifyVisualize();
  }

  @Test
  public void wrongReturnType() {
    initCode("program Test {" + LF + //
            "  int test() {" + LF + //
            "    return 'x';" + LF + //
            "  }" + LF + //
            "  void main() {}" + LF + //
            "}");
    expectError(3, 15, NON_MATCHING_RETURN_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void wrongReturnTypeNull() {
    initCode("program Test {" + LF + //
            "  int test() {" + LF + //
            "    return null;" + LF + //
            "  }" + LF + //
            "  void main() {}" + LF + //
            "}");
    expectError(3, 16, NON_MATCHING_RETURN_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void noReturnVal() {
    initCode("program Test {" + LF + //
            "  int test() {" + LF + //
            "    return;" + LF + //
            "  }" + LF + //
            "  void main() {}" + LF + //
            "}");
    expectError(3, 11, RETURN_NO_VAL);
    parseVerifyVisualize();
  }

  @Test
  public void wrongReturnTypeArr() {
    initCode("program Test {" + LF + //
            "  int[] test() {" + LF + //
            "    return new int[10];" + LF + //
            "  }" + LF + //
            "  void main() {}" + LF + //
            "}");
    expectError(2, 9, INVALID_METH_RETURN_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void wrongReturnClass() {
    initCode("program Test" + LF + //
            "  class C1 { }" + LF + //
            "{" + LF + //
            "  C1 test() {" + LF + //
            "    return new C1;" + LF + //
            "  }" + LF + //
            "  void main() {}" + LF + //
            "}");
    expectError(4, 6, INVALID_METH_RETURN_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void noMeth() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i(10);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 7, NO_METH);
    parseVerifyVisualize();
  }

  @Test
  public void paramType() {
    initCode("program Test {" + LF + //
            "  void method(int x) { }" + LF + //
            "  void main() {" + LF + //
            "    method('a');" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 15, PARAM_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void paramTypeArr() {
    initCode("program Test {" + LF + //
            "  void method(int[] x) { }" + LF + //
            "  void main() {" + LF + //
            "    method(new char[10]);" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 24, PARAM_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void paramTypeClass() {
    initCode("program Test" + LF + //
            "  class C1 { }" + LF + //
            "  class C2 { }" + LF + //
            "{" + LF + //
            "  void method(C1 c1) { }" + LF + //
            "  void main() {" + LF + //
            "    method(new C2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(7, 18, PARAM_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void moreParams() {
    initCode("program Test {" + LF + //
            "  void method(int x, char c) { }" + LF + //
            "  void main() {" + LF + //
            "    method(1, 'a', 1);" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 21, MORE_ACTUAL_PARAMS);
    parseVerifyVisualize();
  }

  @Test
  public void lessParams() {
    initCode("program Test {" + LF + //
            "  void method(int x, char c) { }" + LF + //
            "  void main() {" + LF + //
            "    method(1);" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 13, LESS_ACTUAL_PARAMS);
    parseVerifyVisualize();
  }

  @Test
  public void incompTypesCond() {
    initCode("program Test {" + LF + //
            "  void main() int i; {  " + LF + //
            "    if (i > null) { }" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 17, INCOMP_TYPES);
    parseVerifyVisualize();
  }

  @Test
  public void incompTypesCondArr() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia; char[] ca; {  " + LF + //
            "    if (ia > ca) { }" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 16, INCOMP_TYPES);
    parseVerifyVisualize();
  }

  @Test
  public void incompTypesCondClass() {
    initCode("program Test" + LF + //
            "  class C1 { }" + LF + //
            "{" + LF + //
            "  void main() C1 c1; int i; {  " + LF + //
            "    if (c1 > i) { };" + LF + //
            "  }" + LF + //
            "}");
    expectError(5, 15, INCOMP_TYPES);
    parseVerifyVisualize();
  }

  @Test
  public void wrongEqCheck() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia1, ia2; {" + LF + //
            "    if (ia1 > ia2) { }" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 18, EQ_CHECK);
    parseVerifyVisualize();
  }

  @Test
  public void testSimpleBreak() {
    initCode("program Test {" + LF + //
            "  void main() {" + LF + //
            "    while(42 > 0) /* while(true) */" + LF + //
            "    {" + LF + //
            "      break;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");
    parseVerifyVisualize();
  }

  @Test
  public void testBreak() {
    initCode("program A" + LF + //
            "  int i;" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int n;" + LF + //
            "  {" + LF + //
            "    read(n); " + LF + //
            "    while (i <= n) { while(1 < 2) { if(1 == 1) { break; } } if(i == 5) break; i++; }"
            + LF + //
            "    print(i); " + LF + //
            "  }" + LF + //
            "}");
    addExpectedRun("10", "5");
    parseVerifyVisualize();
  }

  @Test
  public void testNestedBreak() {
    initCode("program Test {" + LF + //
            "  void main() " + LF + //
            "    int n, o;" + LF + //
            "  {" + LF + //
            "    o = 21;" + LF + //
            "    while(83 < 84)" + LF + //
            "    {" + LF + //
            "      while(167 < 168)" + LF + //
            "      {" + LF + //
            "        break;" + LF + //
            "      }" + LF + //
            "      break;" + LF + //
            "    }" + LF + //
            "  }" + LF + //
            "}");
    parseVerifyVisualize();
  }

  @Test
  public void lenTest() {
    initCode("program A" + LF + //
            "  class A { int[] x; }" + LF + //
            "  class B { A a; }" + LF + //
            "  class C { B b; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    C[] c;" + LF + //
            "  {" + LF + //
            "    c = new C[5];" + LF + //
            "    print(len(c));" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("5");
    parseVerifyVisualize();
  }

  @Test
  public void basicOrdChrTest() {
    initCode("program Test {" + LF + //
            "  void main() int i; char c; {" + LF + //
            "    i = ord('A');" + LF + //
            "    print(i);" + LF + //
            "    i = ord('*');" + LF + //
            "    print(i);" + LF + //
            "    c = chr(49);" + LF + //
            "    print(c);" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("65421");
    parseVerifyVisualize();
  }

  @Test
  public void trappingOrdChrTest() {
    initCode("program Test {" + LF + //
            "  int trap() {" + LF + //
            "    print(7 * 7);" + LF + //
            "  }" + LF + //
            "  void main() int i; char c; {" + LF + //
            "    ord('!');" + LF + //
            "    chr(42);" + LF + //
            "    i = ord('!');" + LF + //
            "    c = chr(42);" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("");
    parseVerifyVisualize();
  }

  @Test
  public void unusedReturnVal() {
    initCode("program Test {" + LF + //
            "  int getUnused() {" + LF + //
            "    return 351;" + LF + //
            "  }" + LF + //
            "  int polluteAndGet() {" + LF + //
            "    getUnused();" + LF + //
            "    return 42;" + LF + //
            "  }" + LF + //
            "  void main() {" + LF + //
            "    print(932 + polluteAndGet());" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("974");
    parseVerifyVisualize();
  }

  @Test
  public void coverUniverseMethod() {
    initCode("program Test {" + LF + //
            "  int cast(char c) { return ord(c); }" + LF + //
            "  int ord(char c) { return cast(c) - 30; }" + LF + //
            "  void main() {" + LF + //
            "    print(chr(ord('A')));" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("#");
    parseVerifyVisualize();
  }

  @Test
  public void paramType2() {
    initCode("program Test {" + LF + //
            "  void method(int x, int y) { }" + LF + //
            "  void main() {" + LF + //
            "    method(1, 'a');" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 18, PARAM_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void paramTypeArr2() {
    initCode("program Test {" + LF + //
            "  void method(int[] x, int y) { }" + LF + //
            "  void main() {" + LF + //
            "    method(new int[10], new char[10]);" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 37, PARAM_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void paramTypeClass2() {
    initCode("program Test" + LF + //
            "  class C1 { }" + LF + //
            "  class C2 { }" + LF + //
            "{" + LF + //
            "  void method(C1 c1, C2 c2) { }" + LF + //
            "  void main() {" + LF + //
            "    method(new C1, new C1);" + LF + //
            "  }" + LF + //
            "}");
    expectError(7, 26, PARAM_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void testRelops() {
    initFile("relops.mj");
    addExpectedRun("0", "!=,<,<=,");
    addExpectedRun("1", "==,<=,>=,");
    addExpectedRun("2", "!=,>,>=,");
    parseVerifyVisualize();
  }

  @Test
  public void testAnimals() {
    initFile("animals.mj");
    addExpectedRun("0", "cat");
    addExpectedRun("1", "dog");
    addExpectedRun("2", "octopus");
    parseVerifyVisualize();
  }

  @Test
  public void compareNeg() {
    initCode("program A" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int neg;" + LF + //
            "  {" + LF + //
            "    neg = -42;" + LF + //
            "    if (neg == -42) print(42);" + LF + //
            "    else print(neg); " + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("42");
    parseVerifyVisualize();
  }

  @Test
  public void testWeekdayEnum() {
    initCode("""
            program Test
              singleton Weekdays {
                int Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday;
              }(1, 2, 3, 4, 5, 6, 7)
            {
              void printWednesday() {
                print('I');
                print('t');
                print(' ');
                print('i');
                print('s');
                print(' ');
                print('W');
                print('e');
                print('d');
                print('n');
                print('e');
                print('s');
                print('d');
                print('a');
                print('y');
                print(' ');
                print('m');
                print('y');
                print(' ');
                print('d');
                print('u');
                print('d');
                print('e');
                print('s');
              }
            
              void main() int day; {
                read(day);
                if (day == Weekdays.Wednesday) {
                  printWednesday();
                }
              }
            }""");
    addExpectedRun("1", "");
    addExpectedRun("3", "It is Wednesday my dudes");
    parseVerifyVisualize();
  }

  @Test
  public void testExpressionsForInitialization() {
    initCode("""
            program Test
              final int k = 42;
              int m = 13;
              int[] arr = new int[5];
              singleton S { int i; char c; int len; } (k * m, chr(k + 23), len(arr))
            {
              void main() int a; char b; {
                a = S.i;
                b = S.c;
                print(m);
                print(' ');
                print(a);
                print(' ');
                print(S.i);
                print(' ');
                print(b);
                print(' ');
                print(S.c);
                print(' ');
                print(S.len);
                print(' ');
                print(len(arr));
              }
            }""");
    addExpectedRun("13 546 546 A A 5 5");
    parseVerifyVisualize();
  }

  @Test
  public void singletonWriteAndRead1() {
    initCode("""
            program Test
              singleton Math { int PI; } (3)
              singleton Workdays { int Mon, Tue, Wed, Thu, Fri; }
            {
              void main() {
                Workdays.Mon = 1;
                Workdays.Tue = 2;
                Workdays.Wed = 3;
                Workdays.Thu = 4;
                Workdays.Fri = 5;
                
                print(Workdays.Mon + Workdays.Tue + Workdays.Wed + Workdays.Thu + Workdays.Fri);
              }
            }
            """);
    addExpectedRun("15");
    parseVerifyVisualize();
  }

  @Test
  public void singletonWriteAndRead2() {
    initCode("""
            program Test
              singleton Math { int PI; } (3)
              singleton Workdays { int Mon, Tue, Wed, Thu, Fri; } (1,2,3,4,5)
            {
              void main() {
                /* Workdays.Mon stays 1 */;
                Workdays.Tue = 1;
                Workdays.Wed = 1;
                Workdays.Thu = 1;
                /* Workdays.Fri stays 5 */;
                
                print(Workdays.Mon + Workdays.Tue + Workdays.Wed + Workdays.Thu + Workdays.Fri);
              }
            }
            """);
    addExpectedRun("9");
    parseVerifyVisualize();
  }

  @Test
  public void testCorrectSingletonInitializerParameters() {
    initCode("""
            program Test
              singleton A { int a, b; } (0, 1)
              singleton B { int a; int b; } (0, 1)
              singleton C { int a; char b; } (0, 'x')
            {
              void main() {}
            }
            """);
    parseVerifyVisualize();
  }

  @Test
  public void testWrongSingletonInitializerParameters() {
    initCode("""
            program Test
              singleton A { int a; } ('h')
              singleton B { int a, b; } (0)
              singleton C { int a; } (1, 2, 3)
              singleton D { int a; int b; } (0)
            {
              void main() {}
            }
            """);
    expectError(2, 30, PARAM_TYPE);
    expectError(3, 31, LESS_INITIALIZERS);
    expectError(4, 34, MORE_INITIALIZERS);
    expectError(5, 35, LESS_INITIALIZERS);
    parseVerifyVisualize();
  }
}
