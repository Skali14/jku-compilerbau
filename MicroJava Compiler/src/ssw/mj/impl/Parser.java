package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.codegen.Label;
import ssw.mj.codegen.Operand;
import ssw.mj.scanner.Token;

import java.util.*;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.scanner.Token.Kind.*;

import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;

public final class Parser {

  /**
   * Maximum number of global variables per program
   */
  private static final int MAX_GLOBALS = 32767;

  /**
   * Maximum number of fields per class
   */
  private static final int MAX_FIELDS = 32767;

  /**
   * Maximum number of local variables per method
   */
  private static final int MAX_LOCALS = 127;

  /**
   * Last recognized token;
   */
  private Token t;

  /**
   * Lookahead token (not recognized).)
   */
  private Token la;

  /**
   * Shortcut to kind attribute of lookahead token (la).
   */
  private Token.Kind sym;

  /**
   * Distance to last recognized error (inititalized with 3 to detect errors at the beginning too)
   */
  private static final int MIN_ERROR_DIST = 3;
  private int errorDistance = MIN_ERROR_DIST;

  /**
   * According scanner
   */
  public final Scanner scanner;

  /**
   * According code buffer
   */
  public final Code code;

  /**
   * According symbol table
   */
  public final Tab tab;


  Label breakLab;
  Stack<Label> breaks = new Stack<>();
  Obj curMethod;

  public Parser(Scanner scanner) {
    this.scanner = scanner;
    tab = new Tab(this);
    code = new Code(this);
    // Pseudo token to avoid crash when 1st symbol has scanner error.
    la = new Token(none, 1, 1);
  }


  /**
   * Reads ahead one symbol.
   */
  private void scan() {
    t = la;
    la = scanner.next();
    sym = la.kind;
    errorDistance++;
  }

  /**
   * Verifies symbol and reads ahead.
   */
  private void check(Token.Kind expected) {
    if (sym == expected) {
      scan();
    } else {
      error(TOKEN_EXPECTED, expected);
    }
  }

  /**
   * Adds error message to the list of errors.
   */
  public void error(Message msg, Object... msgParams) {
    if (errorDistance >= 3) {
      scanner.errors.error(la.line, la.col, msg, msgParams);
    }
    errorDistance = 0;
  }

  /**
   * Starts the analysis.
   */
  public void parse() {
    scan(); // scan first symbol, initializes look-ahead
    Program(); // start analysis
    check(eof);
  }


  // ===============================================
  // TODO Exercise 2: Implementation of parser
  // TODO Exercise 3: Error recovery methods
  // TODO Exercise 4: Symbol table handling
  // TODO Exercise 5-6: Code generation
  // ===============================================

  private static final EnumSet<Token.Kind> firstStatement = EnumSet.of(ident, if_, while_, break_, return_, read, print, lbrace, semicolon);
  private static final EnumSet<Token.Kind> firstAssignop = EnumSet.of(assign, plusas, minusas, timesas, slashas, remas);
  private static final EnumSet<Token.Kind> firstFactor = EnumSet.of(ident, number, charConst, new_, lpar);
  private static final EnumSet<Token.Kind> recoverDeclSet = EnumSet.of(final_, ident, class_, singleton, lbrace, eof);
  private static final EnumSet<Token.Kind> recoverMethodDeclSet = EnumSet.of(ident, void_, rbrace, eof);
  private static final EnumSet<Token.Kind> recoverStatSet = EnumSet.of(if_, while_, break_, return_, read, print, semicolon, rbrace, eof);

  // ---------------------------------

  /**
   * Program = <br>
   * "program" ident <br>
   * { ConstDecl | GlobalVarDecl | ClassDecl | SingletonDecl } <br>
   * "{" { MethodDecl } "}" .
   */
  private void Program() {
    check(program);
    check(ident);
    Obj prog = tab.insert(Obj.Kind.Prog, t.val, Tab.noType);
    tab.openScope();
    tab.insert(Obj.Kind.Meth, "<clinit>", Tab.noType);
    code.put(Code.OpCode.enter);
    code.put(0);
    code.put(0);

    outer: for (;;) {
      switch (sym) {
        case final_:
          ConstDecl();
          break;
        case ident:
          GlobalVarDecl();
          break;
        case class_:
          ClassDecl();
          break;
        case singleton:
          SingletonDecl();
          break;
        case lbrace, eof:
          break outer;
        default:
          recoverDecl();
      }
    }
    check(lbrace);
    code.put(Code.OpCode.exit);
    code.put(Code.OpCode.return_);
    for (;;) {
      if(sym == ident || sym == void_) {
        MethodDecl();
      } else if(sym == rbrace || sym == eof) {
        break;
      } else {
        recoverMethodDecl();
      }
    }
    check(rbrace);
    if (code.mainpc == -1) {
      error(MAIN_NOT_FOUND);
    }
    prog.locals = tab.curScope.locals();
    code.dataSize = tab.curScope.nVars();
    tab.closeScope();
  }

  private void ConstDecl() {
    check(final_);
    Struct type = Type();
    check(ident);
    Obj con = tab.insert(Obj.Kind.Con, t.val, type);
    check(assign);
    if(sym == number) {
      if (type != Tab.intType) {
        error(CONST_TYPE);
      }
      scan();
    } else if(sym == charConst) {
      if (type != Tab.charType) {
        error(CONST_TYPE);
      }
      scan();
    } else {
      error(CONST_DECL);
    }
    con.val = t.numVal;
    check(semicolon);
  }

  private void GlobalVarDecl() {
    ArrayList<Obj> globalVarDecls = new ArrayList<>();
    Struct type = Type();
    check(ident);
    globalVarDecls.add(tab.insert(Obj.Kind.Var, t.val, type));
    while (sym == comma) {
      scan();
      check(ident);
      globalVarDecls.add(tab.insert(Obj.Kind.Var, t.val, type));
    }
    if (sym == assign) {
      scan();
      Operand x = Expr();

      for (int i = 0; i < globalVarDecls.size(); i++) {
        Obj o = globalVarDecls.get(i);
        if (i < globalVarDecls.size() - 1) {
          code.load(x);
          code.put(Code.OpCode.dup);
        }
        code.assign(new Operand(o, this), x);
      }
    }
    check(semicolon);
    if (tab.curScope.nVars() > MAX_GLOBALS) {
      error(TOO_MANY_GLOBALS);
    }
  }

  private void VarDecl() {
    Struct type = Type();
    check(ident);
    tab.insert(Obj.Kind.Var, t.val, type);
    while (sym == comma) {
      scan();
      check(ident);
      tab.insert(Obj.Kind.Var, t.val, type);
    }
    check(semicolon);
  }

  private void ClassDecl() {
    check(class_);
    check(ident);
    Obj clazz = tab.insert(Obj.Kind.Type, t.val, new Struct(Struct.Kind.Class));
    check(lbrace);
    tab.openScope();
    while (sym == ident) {
      VarDecl();
    }
    if(tab.curScope.nVars() > MAX_FIELDS) {
      error(TOO_MANY_FIELDS);
    }
    clazz.type.fields = tab.curScope.locals();
    check(rbrace);
    tab.closeScope();
  }

  private void SingletonDecl() {
    check(singleton);
    check(ident);
    Obj singleton = tab.insert(Obj.Kind.Var, t.val, new Struct(Struct.Kind.Class));

    check(lbrace);
    tab.openScope();
    while(sym == ident) {
      VarDecl();
    }

    code.put(Code.OpCode.new_);
    code.put2(tab.curScope.nVars());
    code.assign(new Operand(singleton, this), new Operand(Tab.noType));

    if (tab.curScope.nVars() > MAX_FIELDS) {
      error(TOO_MANY_FIELDS);
    }
    singleton.type.fields = tab.curScope.locals();
    check(rbrace);
    tab.closeScope();
    if(sym == lpar) {
      SingletonInitializers(singleton);
    }
  }

  private void SingletonInitializers(Obj singleton) {
    Operand y;
    check(lpar);
    Iterator<Obj> singletonLocals = singleton.type.fields.values().iterator();
    Obj field;
    int nrInitializers = 0;
    int nrFields = singleton.type.nrFields();
    code.load(new Operand(singleton, this));
    y = Expr();
    nrInitializers++;
    if(singletonLocals.hasNext()) {
      field = singletonLocals.next();
      if(!y.type.assignableTo(field.type)) {
        error(PARAM_TYPE);
      }
      Operand fieldOperand = new Operand(field, this);
      fieldOperand.kind = Operand.Kind.Fld;
      code.assign(fieldOperand, y);
    }

    while (sym == comma) {
      scan();
      code.load(new Operand(singleton, this));
      y = Expr();
      nrInitializers++;
      if(singletonLocals.hasNext()) {
        field = singletonLocals.next();

        if(!y.type.assignableTo(field.type)) {
          error(PARAM_TYPE);
        }

        Operand fieldOperand = new Operand(field, this);
        fieldOperand.kind = Operand.Kind.Fld;
        code.assign(fieldOperand, y);
      }
    }

    if(nrInitializers > nrFields) {
      error(MORE_INITIALIZERS);
    } else if(nrInitializers < nrFields) {
      error(LESS_INITIALIZERS);
    }
    check(rpar);
  }

  private void MethodDecl() {
    int n = 0;
    Struct type = Tab.noType;
    if(type.isRefType()) {
      error(INVALID_METH_RETURN_TYPE);
    }
    switch (sym) {
      case ident:
        type = Type();
        if(type != Tab.intType && type != Tab.charType) {
        error(INVALID_METH_RETURN_TYPE);
      }
        break;
      case void_:
        scan();
        break;
      default:
        error(INVALID_METH_DECL);
    }
    check(ident);

    String methodName = t.val;
    curMethod = tab.insert(Obj.Kind.Meth, methodName, type);

    tab.openScope();
    check(lpar);

    if (sym == ident) {
      n = FormPars();
    }

    check(rpar);
    curMethod.nPars = n;
    if (Objects.equals(curMethod.name, "main")) {
      code.mainpc = code.pc;
      if(curMethod.type != Tab.noType) {
        error(MAIN_NOT_VOID);
      }
      if(curMethod.nPars != 0) {
        error(MAIN_WITH_PARAMS);
      }
    }
    while (sym == ident) {
      VarDecl();
    }
    curMethod.locals = tab.curScope.locals();
    curMethod.adr = code.pc;
    code.put(Code.OpCode.enter);

    code.put(curMethod.nPars);
    code.put(tab.curScope.nVars());

    if (curMethod.name.equals("main")) {
      code.methodCall(new Operand(type));
    }

    if(tab.curScope.nVars() > MAX_LOCALS) {
      error(TOO_MANY_LOCALS);
    }
    Block();
    if(curMethod.type == Tab.noType) {
      code.put(Code.OpCode.exit);
      code.put(Code.OpCode.return_);
    } else {
      code.put(Code.OpCode.trap);
      code.put(1);
    }

    tab.closeScope();
  }

  private int FormPars() {
    int n = 0;
    Struct type = Type();
    check(ident);
    n++;
    tab.insert(Obj.Kind.Var, t.val, type);
    while (sym == comma) {
      scan();
      Struct type2 = Type();
      check(ident);
      n++;
      tab.insert(Obj.Kind.Var, t.val, type2);
    }
    return n;
  }

  private Struct Type() {
    check(ident);
    Obj o = tab.find(t.val);
    if(o.kind != Obj.Kind.Type) {
      error(NO_TYPE);
    }
    Struct type = o.type;
    if (sym == lbrack) {
      scan();
      check(rbrack);
      type = new Struct(type);
    }
    return type;
  }

  private void Block() {
    check(lbrace);
    for(;;) {
      if(firstStatement.contains(sym)) {
        Statement();
      } else if(sym == rbrace || sym == eof) {
        break;
      } else {
        recoverStat();
      }
    }
    check(rbrace);
  }

  private void Statement() {
    Operand x;
    Operand y;
    switch (sym) {
      case none:
        break;
      case ident:
        x = Designator();
        if(firstAssignop.contains(sym)) {

          if(!x.canBeAssignedTo()) {
            error(CANNOT_ASSIGN_TO, x.kind);
          }

          Code.OpCode op = Assignop();
          if(op == Code.OpCode.add || op == Code.OpCode.sub || op == Code.OpCode.mul || op == Code.OpCode.div || op == Code.OpCode.rem) {
            code.compoundAssignmentPrepareLHS(x);
          }
          y = Expr();

          if (op != Code.OpCode.store && (x.type != Tab.intType || y.type != Tab.intType)) {
            error(NO_INT_OPERAND);
          }

          if (y.type.assignableTo(x.type)) {
            switch (op) {
              case store -> code.assign(x, y);
              case add, sub, mul, div, rem -> {
                code.load(y);
                code.put(op);
                code.assign(x, y);
              }
            }
          } else {
            error(INCOMP_TYPES);
          }

        } else if (sym == lpar) {
          ActPars(x);
          code.methodCall(x);
          if(x.type != Tab.noType) {
            code.put(Code.OpCode.pop);
          }
        } else if (sym == pplus) {
          if(!x.canBeAssignedTo()) {
            error(CANNOT_ASSIGN_TO, x.kind);
          }
          if(x.type != Tab.intType) {
            error(NO_INT_OPERAND);
          }
          scan();
          code.inc(x, 1);
        } else if (sym == mminus) {
          if(!x.canBeAssignedTo()) {
            error(CANNOT_ASSIGN_TO, x.kind);
          }
          if(x.type != Tab.intType) {
            error(NO_INT_OPERAND);
          }
          scan();
          code.inc(x, -1);
        } else {
          error(DESIGN_FOLLOW);
        }
        check(semicolon);
        break;
      case if_:
        scan();
        check(lpar);
        x = Condition();
        check(rpar);
        code.fJump(x.op, x.fLabel);
        x.tLabel.here();
        Statement();
        if(sym == else_) {
          Label end = new Label(code);
          code.jump(end);
          x.fLabel.here();
          scan();
          Statement();
          end.here();
        } else {
          x.fLabel.here();
        }
        break;
      case while_:
        scan();
        breaks.push(breakLab);
        breakLab = new Label(code);
        check(lpar);
        Label top = new Label(code);
        top.here();
        x = Condition();
        code.fJump(x.op, x.fLabel);
        x.tLabel.here();
        check(rpar);
        Statement();
        code.jump(top);
        x.fLabel.here();
        breakLab.here();
        breakLab = breaks.pop();
        break;
      case break_:
        scan();

        if(breakLab == null) {
          error(NO_LOOP);
        } else {
          code.jump(breakLab);
        }
        check(semicolon);
        break;
      case return_:
        scan();
        if(sym == minus || firstFactor.contains(sym)) {
          if(curMethod.type == Tab.noType) {
            error(RETURN_VOID);
          }
          x = Expr();
          code.load(x);
          if(!x.type.assignableTo(curMethod.type)) {
            error(NON_MATCHING_RETURN_TYPE);
          }
        } else if(curMethod.type != Tab.noType) {
            error(RETURN_NO_VAL);
        }
        code.put(Code.OpCode.exit);
        code.put(Code.OpCode.return_);
        check(semicolon);
        break;
      case read:
        scan();
        check(lpar);
        x = Designator();
        if(!x.canBeAssignedTo()) {
          error(CANNOT_ASSIGN_TO, x.kind);
        }

        if(x.type == Tab.intType) {
          code.put(Code.OpCode.read);
          code.assign(x, new Operand(Tab.intType));
        } else if(x.type == Tab.charType) {
          code.put(Code.OpCode.bread);
          code.assign(x, new Operand(Tab.charType));
        } else {
          error(READ_VALUE);
        }

        check(rpar);
        check(semicolon);
        break;
      case print:
        scan();
        check(lpar);
        x = Expr();
        code.load(x);
        int width = 0;
        if(sym == comma) {
          scan();
          check(number);
          width = t.numVal;
        }
        code.load(new Operand(width));

        if(x.type == Tab.intType) {
          code.put(Code.OpCode.print);
        } else if(x.type == Tab.charType) {
          code.put(Code.OpCode.bprint);
        } else {
          error(PRINT_VALUE);
        }

        check(rpar);
        check(semicolon);
        break;
      case lbrace:
        Block();
        break;
      case semicolon:
        scan();
        break;
      default:
        error(INVALID_STAT);
    }
  }

  private Code.OpCode Assignop() {
    Code.OpCode x;
    switch (sym) {
      case assign:
        x = Code.OpCode.store;
        scan();
        break;
      case plusas:
        x = Code.OpCode.add;
        scan();
        break;
      case minusas:
        x = Code.OpCode.sub;
        scan();
        break;
      case timesas:
        x = Code.OpCode.mul;
        scan();
        break;
      case slashas:
        x = Code.OpCode.div;
        scan();
        break;
      case remas:
        x = Code.OpCode.rem;
        scan();
        break;
      default:
        x = Code.OpCode.nop;
        error(ASSIGN_OP);
    }
    return x;
  }

  private void ActPars(Operand x) {
    Operand y;
    check(lpar);
    if(x.kind != Operand.Kind.Meth) {
      error(NO_METH);
      x.obj = tab.noObj;
    }
    int aPars = 0;
    int fPars = x.obj.nPars;
    Iterator<Obj> localsMapIterator = x.obj.locals.values().iterator();
    Obj fp = null;
    if(localsMapIterator.hasNext()) {
      fp = localsMapIterator.next();
    }

    if (sym == minus || firstFactor.contains(sym)) {
      y = Expr();
      code.load(y);
      aPars++;
      if(fp != null && x.obj != tab.lenObj && !y.type.assignableTo(fp.type)) {
        error(PARAM_TYPE);
      }

      while (sym == comma) {
        scan();
        y = Expr();
        code.load(y);
        aPars++;
        if(localsMapIterator.hasNext()) {
          fp = localsMapIterator.next();
        } else {
          fp = null;
        }

        if(fp != null && !y.type.assignableTo(fp.type)) {
          error(PARAM_TYPE);
        }
      }
    }
    if(aPars > fPars) {
      error(MORE_ACTUAL_PARAMS);
    } else if(aPars < fPars) {
      error(LESS_ACTUAL_PARAMS);
    }
    check(rpar);
  }

  private Operand Condition() {
    Operand x = CondTerm();
    while (sym == or) {
      code.tJump(x.op, x.tLabel);
      scan();
      x.fLabel.here();
      Operand y = CondTerm();
      x.fLabel = y.fLabel;
      x.op = y.op;
    }
    return x;
  }

  private Operand CondTerm() {
    Operand x = CondFact();
    while (sym == and) {
      code.fJump(x.op, x.fLabel);
      scan();
      Operand y = CondFact();
      x.op = y.op;
    }
    return x;
  }

  private Operand CondFact() {
    Operand x = Expr();
    code.load(x);
    Code.CompOp op = Relop();
    Operand y = Expr();
    code.load(y);
    if(!x.type.compatibleWith(y.type)) {
      error(INCOMP_TYPES);
    }
    if(x.type.isRefType() && op != Code.CompOp.eq && op != Code.CompOp.ne) {
      error(EQ_CHECK);
    }
    return new Operand(op, code);
  }

  private Code.CompOp Relop() {
    Code.CompOp x;
    switch (sym) {
      case eql:
        x = Code.CompOp.eq;
        scan();
        break;
      case neq:
        x = Code.CompOp.ne;
        scan();
        break;
      case gtr:
        x = Code.CompOp.gt;
        scan();
        break;
      case geq:
        x = Code.CompOp.ge;
        scan();
        break;
      case lss:
        x = Code.CompOp.lt;
        scan();
        break;
      case leq:
        x = Code.CompOp.le;
        scan();
        break;
      default:
        x = Code.CompOp.eq;
        error(REL_OP);
    }
    return x;
  }

  private Operand Expr() {

    Operand x;
    if (sym == minus) {
      scan();
      x = Term();

      if(x.type != Tab.intType) {
        error(NO_INT_OPERAND);
      }

      if(x.kind == Operand.Kind.Con) {
        x.val = -x.val;
      } else {
        code.load(x);
        code.put(Code.OpCode.neg);
      }
    } else {
      x = Term();
    }

    while (sym == plus || sym == minus) {
      Code.OpCode op = Addop();
      code.load(x);
      Operand y = Term();
      code.load(y);
      if(x.type != Tab.intType || y.type != Tab.intType) {
        error(NO_INT_OPERAND);
      }
      code.put(op);
    }
    return x;
  }

  private Operand Term() {
    Operand x = Factor();

    while (sym == times || sym == slash || sym == rem) {
      Code.OpCode op = Mulop();
      code.load(x);
      Operand y = Factor();
      code.load(y);
      if(x.type != Tab.intType || y.type != Tab.intType) {
        error(NO_INT_OPERAND);
      }
      code.put(op);
    }
    return x;
  }

  private Operand Factor() {
    Operand x;
    switch (sym) {
      case ident:
        x = Designator();
        if (sym == lpar) {
          if(x.kind != Operand.Kind.Meth) {
            error(NO_METH);
          } else if(x.obj.type == Tab.noType) {
            error(INVALID_CALL);
          }
          ActPars(x);
          code.methodCall(x);
          x.kind = Operand.Kind.Stack;
        }
        break;
      case number:
        scan();
        x = new Operand(t.numVal);
        break;
      case charConst:
        scan();
        x = new Operand(t.numVal);
        x.type = Tab.charType;
        break;
      case new_:
        scan();
        check(ident);
        Obj obj = tab.find(t.val);
        if (obj.kind != Obj.Kind.Type) {
          error(NO_TYPE);
        }
        Struct type = obj.type;

        if (sym == lbrack) {
          scan();
          x = Expr();

          if (x.type != Tab.intType) {
            error(ARRAY_SIZE);
          }
          code.load(x);
          code.put(Code.OpCode.newarray);
          if (type == Tab.charType) {
            code.put(0);
          } else {
            code.put(1);
          }
          type = new Struct(type);

          check(rbrack);
        } else {
          if(obj.kind != Obj.Kind.Type || type.kind != Struct.Kind.Class) {
            error(NO_CLASS_TYPE);
          }
          code.put(Code.OpCode.new_);
          code.put2(type.nrFields());
        }
        x = new Operand(type);
        break;
      case lpar:
        scan();
        x = Expr();
        check(rpar);
        break;
      default:
        x = new Operand(1);
        x.kind = Operand.Kind.Stack;
        error(INVALID_FACT);
    }
    return x;
  }

  private Operand Designator() {
    check(ident);
    Obj o = tab.find(t.val);
    Operand x = new Operand(o, this);
    while (sym == period || sym == lbrack) {
      if (sym == period) {
        if(x.type.kind != Struct.Kind.Class) {
          error(NO_CLASS);
        }
        scan();
        code.load(x);
        check(ident);
        Obj obj = tab.findField(t.val, x.type);
        x.kind = Operand.Kind.Fld;
        x.type = obj.type;
        x.adr = obj.adr;
      } else {
        scan();
        code.load(x);
        Operand y = Expr();
        if(x.type.kind != Struct.Kind.Arr) {
          error(NO_ARRAY);
        }
        if(y.type != Tab.intType) {
          error(ARRAY_INDEX);
        }
        code.load(y);
        x.kind = Operand.Kind.Elem;
        x.type = x.type.elemType;
        check(rbrack);
      }
    }
    return x;
  }

  private Code.OpCode Addop() {
    Code.OpCode x = null;
    switch (sym) {
      case plus:
        x = Code.OpCode.add;
        scan();
        break;
      case minus:
        x = Code.OpCode.sub;
        scan();
        break;
      default:
        error(ADD_OP);
    }
    return x;
  }

  private Code.OpCode Mulop() {
    Code.OpCode x = null;
    switch (sym) {
      case times:
        x = Code.OpCode.mul;
        scan();
        break;
      case slash:
        x = Code.OpCode.div;
        scan();
        break;
      case rem:
        x = Code.OpCode.rem;
        scan();
        break;
      default:
        error(MUL_OP);
    }
    return x;
  }

  // ...

  // ------------------------------------

  private void recoverDecl() {
    error(INVALID_DECL);
    do {
      scan();
    } while (!recoverDeclSet.contains(sym));
    errorDistance = 0;
  }

  private void recoverMethodDecl() {
    error(INVALID_METH_DECL);
    do {
      scan();
    } while (!recoverMethodDeclSet.contains(sym));
    errorDistance = 0;
  }

  private void recoverStat() {
    error(INVALID_STAT);
    do {
      scan();
    } while (!recoverStatSet.contains(sym));
    errorDistance = 0;
  }

  // ====================================
  // ====================================
}
