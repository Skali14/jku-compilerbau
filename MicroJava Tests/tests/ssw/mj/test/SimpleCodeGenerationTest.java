package ssw.mj.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ssw.mj.codegen.Operand;
import ssw.mj.test.support.BaseCompilerTestCase;
import ssw.mj.test.support.Configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ssw.mj.Errors.Message.*;

@Timeout(value = Configuration.TIMEOUT)
public class SimpleCodeGenerationTest extends BaseCompilerTestCase {

  /**
   * Symbol table for most examples of this test class.
   */
  private void expectSymTab() {
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

  @Test
  public void undefNameMeth() {
    initCode("program Test {" + LF + //
            "  void main() {" + LF + //
            "    method();" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 11, NOT_FOUND, "method");
    parseVerifyVisualize();
  }

  @Test
  public void forwardDeclErrorMissingMethod() {
    initCode("program Test" + LF + // 1
            "{" + LF + // 2
            "  void main() { foo(); }" + LF + // 3
            "  void foo() {}" + LF + // 4
            "}" + LF // 5
    );
    expectError(3, 20, NOT_FOUND, "foo");

    parseVerifyVisualize();
  }

  @Test
  public void undefNameVar() {
    initCode("program Test {" + LF + //
            "  void main() {" + LF + //
            "    var++;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 8, NOT_FOUND, "var");
    parseVerifyVisualize();
  }

  @Test
  public void bspEmpty() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();

    parseVerifyVisualize();
  }

  @Test
  public void bsp01() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    n = 3;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("3");
    parseVerifyVisualize();
  }

  @Test
  public void bsp01a() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    n = -1 + 2;" + LF + //
            "    print(n); " + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp02() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    i = 10;" + LF + //
            "    print(i);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("10");
    parseVerifyVisualize();
  }

  @Test
  public void bsp03() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    i = 1;" + LF + //
            "    n = 3 + i;" + LF + //
            "    print(n);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("4");
    parseVerifyVisualize();
  }

  @Test
  public void bsp04() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    i = 1;" + LF + //
            "    n = 3 + i * max - n;" + LF + //
            "    print(n);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("15");
    parseVerifyVisualize();
  }

  @Test
  public void bsp05() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    iarr = new int[10];" + LF + //
            "    iarr[5] = 10;" + LF + //
            "    print(iarr[0]);" + LF + //
            "    print(iarr[5]);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();

    addExpectedRun("010");
    parseVerifyVisualize();
  }

  @Test
  public void bsp06() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    iarr = new int[10];" + LF + //
            "    iarr[5] = 10;" + LF + //
            "    b = new B;" + LF + //
            "    b.y = iarr[5] * 3;" + LF + //
            "    print(b.y);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("30");

    parseVerifyVisualize();
  }

  @Test
  public void bsp07() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    n--;" + LF + //
            "    print(n);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("-1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp08() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    i--;" + LF + //
            "    print(i);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("-1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp09() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    b = new B;" + LF + //
            "    b.y--;" + LF + //
            "    print(b.y);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("-1");
    parseVerifyVisualize();
  }

  @Test
  public void bsp10() {
    initCode("program A" + LF + //
            "  final int max = 12;" + LF + //
            "  char c; int i;" + LF + //
            "  class B { int x, y; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int[] iarr; B b; int n;" + LF + //
            "  {" + LF + //
            "    iarr = new int[10];" + LF + //
            "    iarr[0]--;" + LF + //
            "    print(iarr[0]);" + LF + //
            "  }" + LF + //
            "}");

    expectSymTab();
    addExpectedRun("-1");
    parseVerifyVisualize();
  }

  // ---- Errors in Code.java
  @Test
  public void noVarMethod() {
    initCode("program Test {" + LF + //
            "  int method() { return 0; }" + LF + //
            "  void main() int i; {" + LF + //
            "    method = i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 12, CANNOT_ASSIGN_TO, Operand.Kind.Meth.name());
    parseVerifyVisualize();
  }

  @Test
  public void noVarIncMethod() {
    initCode("program Test {" + LF + //
            "  int method() { return 0; }" + LF + //
            "  void main() int i; {" + LF + //
            "    method++;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 11, CANNOT_ASSIGN_TO, Operand.Kind.Meth.name());
    parseVerifyVisualize();
  }

  @Test
  public void noOperand() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    Test = i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 10, NO_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void noValueAssign() {
    initCode("program Test {" + LF + //
            "  char method() { return 'a'; }" + LF + //
            "  void main() char c; {" + LF + //
            "    c = method;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 15, NO_VAL);
    parseVerifyVisualize();
  }

  @Test
  public void noValueCalc() {
    initCode("program Test {" + LF + //
            "  int method() { return 0; }" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 5 * method;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 19, NO_VAL);
    parseVerifyVisualize();
  }

  @Test
  public void noValueInc() {
    initCode("program Test {" + LF + //
            "  int method() { return 0; }" + LF + //
            "  void main() int i; {" + LF + //
            "    i += method;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 16, NO_VAL);
    parseVerifyVisualize();
  }

  @Test
  public void assignPlusNoIntOp() {
    initCode("program Test {" + LF + //
            "  void main() int i; char c; {" + LF + //
            "    c += i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 11, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void assignTimesNoIntOp() {
    initCode("program Test {" + LF + //
            "  void main() int i; char c; {" + LF + //
            "    i *= c;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 11, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void incompTypes() {
    initCode("program Test {" + LF + //
            "  void main() int i; {  " + LF + //
            "    i = null;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 13, INCOMP_TYPES);
    parseVerifyVisualize();
  }

  @Test
  public void incompTypesArr() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia; char[] ca; {  " + LF + //
            "    ia = ca;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 12, INCOMP_TYPES);
    parseVerifyVisualize();
  }

  @Test
  public void incompTypesClass() {
    initCode("program Test" + LF + //
            "  class C1 { }" + LF + //
            "  class C2 { }" + LF + //
            "{" + LF + //
            "  void main() C1 c1; C2 c2; {  " + LF + //
            "    c1 = c2;" + LF + //
            "  }" + LF + //
            "}");
    expectError(6, 12, INCOMP_TYPES);
    parseVerifyVisualize();
  }

  @Test
  public void noIntegerInc() {
    initCode("program Test {" + LF + //
            "  void main() char ch; {" + LF + //
            "    ch++;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 7, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void noIntegerDec() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia; {" + LF + //
            "    ia--;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 7, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void wrongReadValue() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia; { " + LF + //
            "    read(ia);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 12, READ_VALUE);
    parseVerifyVisualize();
  }

  @Test
  public void wrongPrintValue() {
    initCode("program Test" + LF + //
            "  class C { }" + LF + //
            "{" + LF + //
            "  void main() C obj; { " + LF + //
            "    print(obj);" + LF + //
            "  }" + LF + //
            "}");
    expectError(5, 14, PRINT_VALUE);
    parseVerifyVisualize();
  }

  @Test
  public void noIntOpNeg() {
    initCode("program Test {" + LF + //
            "  void main() int i; char c; {" + LF + //
            "    i = -c;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 11, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void noIntOpPlus() {
    initCode("program Test {" + LF + //
            "  void main() int i; int[] ia; {" + LF + //
            "    i = i + ia;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 15, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void noIntOpTimes() {
    initCode("program Test {" + LF + //
            "  void main() int i; int[] ia; {" + LF + //
            "    i = ia * i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 15, NO_INT_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void procAsFunc() {
    initCode("program Test {" + LF + //
            "  void main() int x; {" + LF + //
            "    x = main();" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 13, INVALID_CALL);
    parseVerifyVisualize();
  }

  @Test
  public void noTypeNew() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = new main;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 17, NO_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void wrongArraySize() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia; {" + LF + //
            "    ia = new int[ia];" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 20, ARRAY_SIZE);
    parseVerifyVisualize();
  }

  @Test
  public void noClassType() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = new int;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 16, NO_CLASS_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void noClass() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = i.i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 10, NO_CLASS);
    parseVerifyVisualize();
  }

  @Test
  public void noArrayIndex() {
    initCode("program Test {" + LF + //
            "  void main() int[] ia; {" + LF + //
            "    ia[ia] = 1;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 10, ARRAY_INDEX);
    parseVerifyVisualize();
  }

  @Test
  public void noArray() {
    initCode("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i[i]++;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 8, NO_ARRAY);
    parseVerifyVisualize();
  }

  @Test
  public void noTypeNewArray() {
    initCode("program A {" + LF + //
            "  void main () {" + LF + //
            "    print(len(new null[10]));" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 23, NO_TYPE);
    parseVerifyVisualize();
  }

  @Test
  public void testPrint() {
    initCode("program A {" + LF + //
            "  void main () {" + LF + //
            "	 print('a');" + LF + //
            "    print('b',1);" + LF + //
            "    print('c',2);" + LF + //
            "    print('d',3);" + LF + //
            "    print('e',4);" + LF + //
            "  }" + LF + //
            "}");
    addExpectedRun("ab c  d   e");
    parseVerifyVisualize();
  }

  @Test
  public void testDesignator() {
    initCode("program A" + LF + //
            "  class A { int x; }" + LF + //
            "  class B { A a; }" + LF + //
            "  class C { B b; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    A a; B b; C c;" + LF + //
            "  {" + LF + //
            "    c = new C;" + LF + //
            "    c.b = new B;" + LF + //
            "    c.b.a = new A;" + LF + //
            "    c.b.a.x++;" + LF + //
            "    print(c.b.a.x);" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("1");
    parseVerifyVisualize();
  }

  @Test
  public void testArrayAndDesignator() {
    initCode("program A" + LF + //
            "  class A { int[] x; }" + LF + //
            "  class B { A a; }" + LF + //
            "  class C { B b; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    A a; B b; C[] c;" + LF + //
            "  {" + LF + //
            "    c = new C[5];" + LF + //
            "    c[0] = new C;" + LF + //
            "    c[0].b = new B;" + LF + //
            "    c[0].b.a = new A;" + LF + //
            "    c[0].b.a.x = new int[10];" + LF + //
            "    c[3] = new C;" + LF + //
            "    c[3].b = new B;" + LF + //
            "    c[3].b.a = new A;" + LF + //
            "    c[3].b.a.x = new int[30];" + LF + //
            "    c[0].b.a.x[0]--;" + LF + //
            "    c[0].b.a.x[8]++;" + LF + //
            "    c[3].b.a.x[2]++;" + LF + //
            "    c[3].b.a.x[2]*=3;" + LF + //
            "    c[0].b.a.x[8]+=50 + c[3].b.a.x[2] * c[3].b.a.x[2] * c[0].b.a.x[0];" + LF + //
            "    print(c[0].b.a.x[8]);" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("42");
    parseVerifyVisualize();
  }

  @Test
  public void testArrayAndDesignatorAndAssign() {
    initCode("program A" + LF + //
            "  class A { int[] x; }" + LF + //
            "  class B { A a; }" + LF + //
            "  class C { B b; }" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    A a; B b; C[] c;" + LF + //
            "  {" + LF + //
            "    c = new C[5];" + LF + //
            "    c[0] = new C;" + LF + //
            "    c[0].b = new B;" + LF + //
            "    c[0].b.a = new A;" + LF + //
            "    c[0].b.a.x = new int[2];" + LF + //
            "    c[3] = new C;" + LF + //
            "    c[3].b = new B;" + LF + //
            "    c[3].b.a = new A;" + LF + //
            "    c[3].b.a.x = new int[3];" + LF + //
            "    c[0].b.a.x[1]++;" + LF + //
            "    c[0].b.a.x[1]*=256;" + LF + //
            "    c[0].b.a.x[1]/=2;" + LF + //
            "    c[0].b.a.x[1]--;" + LF + //
            "    c[0].b.a.x[1]%=64;" + LF + //
            "    c[3].b.a.x[2]++;" + LF + //
            "    c[3].b.a.x[2]*=21;" + LF + //
            "    c[0].b.a.x[1]-=c[3].b.a.x[2];" + LF + //
            "    print(c[0].b.a.x[1]);" + LF + //
            "  }" + LF + //
            "}");

    addExpectedRun("42");
    parseVerifyVisualize();
  }

  @Test
  public void testArrayIndexExpression() {
    initCode("program A" + LF + // 1
            "{" + LF + // 2
            "  void main()" + LF + // 3
            "    int[] arr;" + LF + // 4
            "  {" + LF + // 5
            "    arr = new int[10];" + LF + // 6
            "    arr[ ( 1 + 2 ) * 3 ] = 4;" + LF + // 7
            "    arr[ 4 - 2 * 2 ] = 2;" + LF + // 8
            "    print(arr[ 90 / 10 ]);" + LF + // 9
            "    print(arr[ 6 - 3 * 2 ]);" + LF + // 10
            "  }" + LF + // 11
            "}");
    addExpectedRun("42");
    parseVerifyVisualize();
  }

  @Test
  public void testReadAndPrint() {
    initCode("program A" + LF + // 1
            "{" + LF + // 2
            "  void main()" + LF + // 3
            "    int n;" + LF + // 4
            "  {" + LF + // 5
            "    n = 0;" + LF + // 6
            "    read(n);" + LF + // 7
            "    print(n);" + LF + // 7
            "  }" + LF + // 9
            "}");
    addExpectedRun("2", "2");
    parseVerifyVisualize();
  }

  @Test
  public void testFields() {
    initCode("program A" + LF + //
            "  class A { int x; }" + LF + //
            "  class B { A a; }" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    A a;" + LF + //
            "    B b;" + LF + //
            "  {" + LF + //
            "    a = new A;" + LF + //
            "    b = new B;" + LF + //
            "    a.x = 20;" + LF + //
            "    a.x++;" + LF + //
            "    a.x /= 7;" + LF + //
            "    a.x *= a.x;" + LF + //
            "    a.x %= a.x - 5;" + LF + //
            "    b.a = new A;" + LF + //
            "    b.a.x = -12;" + LF + //
            "    b.a.x -= a.x;" + LF + //
            "    b.a.x *= -a.x;" + LF + //
            "    b.a.x %= 5;" + LF + //
            "    b.a.x *= a.x + 2 * 3;" + LF + //
            "    print(b.a.x);" + LF + //
            "  }" + LF + //
            "}");
    addExpectedRun("21");
    parseVerifyVisualize();
  }

  @Test
  public void twoMethods() {
    initCode("program A" + LF + // 1
            "{" + LF + // 2
            "  void deadMethodToMoveMainPcFrom0()" + LF + // 3
            "    int n;" + LF + // 4
            "  {" + LF + // 5
            "    n = 0;" + LF + // 6
            "  }" + LF + // 7
            "  void main()" + LF + // 8
            "  {" + LF + // 9
            "    print(2);" + LF + // 10
            "  }" + LF + // 11
            "}");
    addExpectedRun("2");
    parseVerifyVisualize();
    assertTrue(
            parser.code.mainpc > 0,
            "In this example mainpc must be > 0, most likely it should be 7, but it is: " + parser.code.mainpc);
  }

  @Test
  public void noMain() {
    initCode("program Test {" + LF + //
            "  void main_() { }" + LF + //
            "}");
    expectError(3, 2, MAIN_NOT_FOUND);
    parseVerifyVisualize();
  }

  @Test
  public void noValueAssignopMethod() {
    initCode("program Test {" + LF + //
            "  int method() { return 0; }" + LF + //
            "  void main() int i; {" + LF + //
            "    method += i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 12, CANNOT_ASSIGN_TO, Operand.Kind.Meth.name());
    parseVerifyVisualize();
  }

  @Test
  public void testMulops() {
    initCode("program Mulops" + LF + //
            "{" + LF + //
            "  void main ()" + LF + //
            "    int a; int b;" + LF + //
            "  {" + LF + //
            "    a = 42;" + LF + //
            "    b = 3;" + LF + //
            "    a = a / b;" + LF + //
            "    a = a % ( b * b );" + LF + //
            "    print(a);" + LF + //
            "  }" + LF + //
            "}");
    addExpectedRun("5");
    parseVerifyVisualize();
  }

  @Test
  public void testLocalVarsIncDec() {
    initCode("program LocalVars" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "    int a;" + LF + //
            "    int b;" + LF + //
            "  {" + LF + //
            "    a = 2;" + LF + //
            "    b = 5;" + LF + //
            "    a++;" + LF + //
            "    b--;" + LF + //
            "    print(a+b);" + LF + //
            "  }" + LF + //
            "}");
    addExpectedRun("7");
    parseVerifyVisualize();
  }

  @Test
  public void testConstDecl() {
    initCode("program ConstDecl" + LF + //
            " final int a = 100;" + LF + //
            " final char b = 'A';" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "  {" + LF + //
            "    print(a);" + LF + //
            "    print(b);" + LF + //
            "  }" + LF + //
            "}");
    addExpectedRun("100A");
    parseVerifyVisualize();
  }

  @Test
  public void testMethodAsOperand() {
    initCode("program ConstDecl" + LF + //
            "{" + LF + //
            "  int foo() {}" + LF + //
            "  void main()" + LF + //
            "  {" + LF + //
            "    foo++;" + LF + //
            "  }" + LF + //
            "}");
    expectError(6, 8, CANNOT_ASSIGN_TO, Operand.Kind.Meth.name());
    parseVerifyVisualize();
  }

  @Test
  public void testTypeAsOperand() {
    initCode("program ConstDecl" + LF + //
            "class Foo {}" + LF + //
            "{" + LF + //
            "  void main()" + LF + //
            "  {" + LF + //
            "    Foo++;" + LF + //
            "  }" + LF + //
            "}");
    expectError(6, 8, NO_OPERAND);
    parseVerifyVisualize();
  }

  @Test
  public void writeConstant() {
    initCode("program Test" + LF + //
            "  final int max = 42;" + LF + //
            "{" + LF + //
            "  void main() {" + LF + //
            "    max = 68;" + LF + //
            "  }" + LF + //
            "}");
    expectError(5, 9, CANNOT_ASSIGN_TO, Operand.Kind.Con.name());
    parseVerifyVisualize();
  }

  @Test
  public void assignmentExampleGlobals() {
    initCode("program test" + LF + //
            "  int x,y,z = 42;" + LF + //
            "  singleton Math { int PI; int E; }" + LF + //
            "{" + LF + //
            "  void main() {" + LF + //
            "  print(x);" + LF + //
            "  print(y);" + LF + //
            "  print(Math.PI);" + LF + //
            "  print(Math.E);" + LF + //
            "  }" + LF + //
            "}");
    // TODO: can't be executed yet in P5 due to missing call of <clinit>
    // but with this line re-enabled it could be run in P6
    addExpectedRun("424200");
    parseVerifyVisualize();
  }
}