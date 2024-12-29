package ssw.mj.impl;

import ssw.mj.Errors;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Struct;

public final class Tab {

  // Universe
  public static final Struct noType = new Struct(Struct.Kind.None);
  public static final Struct intType = new Struct(Struct.Kind.Int);
  public static final Struct charType = new Struct(Struct.Kind.Char);
  public static final Struct nullType = new Struct(Struct.Kind.Class);
  public static final Struct arrType = new Struct(new Struct(Struct.Kind.None));

  public final Obj noObj, chrObj, ordObj, lenObj;

  /**
   * Only used for reporting errors.
   */
  private final Parser parser;
  /**
   * The current top scope.
   */
  public Scope curScope = null;
  // First scope opening (universe) will increase this to -1
  /**
   * Nesting level of current scope.
   */
  private int curLevel = -2;

  public Tab(Parser p) {
    parser = p;

    // setting up "universe" (= predefined names)
    // opening scope (curLevel goes to -1, which is the universe level)
    openScope();

    noObj = new Obj(Obj.Kind.Var, "noObj", noType);

    insert(Obj.Kind.Type, "int", intType);
    insert(Obj.Kind.Type, "char", charType);
    insert(Obj.Kind.Con, "null", nullType);

    chrObj = insert(Obj.Kind.Meth, "chr", charType);
    openScope();
    Obj iVarObj = insert(Obj.Kind.Var, "i", intType);
    iVarObj.level = 1;
    chrObj.nPars = curScope.nVars();
    chrObj.locals = curScope.locals();
    closeScope();

    ordObj = insert(Obj.Kind.Meth, "ord", intType);
    openScope();
    Obj chVarObj = insert(Obj.Kind.Var, "ch", charType);
    chVarObj.level = 1;
    ordObj.nPars = curScope.nVars();
    ordObj.locals = curScope.locals();
    closeScope();

    lenObj = insert(Obj.Kind.Meth, "len", intType);
    openScope();
    Obj arrVarObj = insert(Obj.Kind.Var, "arr", arrType);
    arrVarObj.level = 1;
    lenObj.nPars = curScope.nVars();
    lenObj.locals = curScope.locals();
    closeScope();

    // still on level -1
    // now that the universe is constructed, the next node that will be added is the Program itself
    // (which will open its own scope with level 0)
  }

  // ===============================================
  // TODO Exercise 4: implementation of symbol table
  // ===============================================

  public void openScope() {
    curScope = new Scope(curScope);
    curLevel++;
  }

  public void closeScope() {
    curScope = curScope.outer();
    curLevel--;
  }

  public Obj insert(Obj.Kind kind, String name, Struct type) {
    if (curScope.findLocal(name) != null) {
      parser.error(Errors.Message.DECL_NAME, name);
      return noObj;
    } else {
      Obj o = new Obj(kind, name, type);
      if (kind == Obj.Kind.Var) {
        o.adr = curScope.nVars();
        o.level = curLevel;
      }
      curScope.insert(o);
      return o;
    }
  }

  /**
   * Retrieves the object with <code>name</code> from the innermost scope.
   */
  public Obj find(String name) {
    Obj o = curScope.findGlobal(name);
    if (o == null) {
      parser.error(Errors.Message.NOT_FOUND, name);
      return noObj;
    }
    return o;
  }

  /**
   * Retrieves the field <code>name</code> from the fields of
   * <code>type</code>.
   */
  public Obj findField(String name, Struct type) {
    Obj o = type.findField(name);
    if (o == null) {
      parser.error(Errors.Message.NO_FIELD, name);
      return noObj;
    }
    return o;
  }

  // ===============================================
  // ===============================================
}
