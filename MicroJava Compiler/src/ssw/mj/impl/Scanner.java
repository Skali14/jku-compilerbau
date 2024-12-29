package ssw.mj.impl;

import ssw.mj.Errors;
import ssw.mj.scanner.Token;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static ssw.mj.scanner.Token.Kind.*;

public class Scanner {

  // Scanner Skeleton - do not rename fields / methods !
  private static final char EOF = (char) -1;
  private static final char LF = '\n';

  /**
   * Input data to read from.
   */
  private final Reader in;

  /**
   * Lookahead character. (= next (unhandled) character in the input stream)
   */
  private char ch;

  /**
   * Current handled character. (= current (already handled) character in the input stream)
   */
  private char prevCh;

  /**
   * Current line in input stream.
   */
  private int line;

  /**
   * Current column in input stream.
   */
  private int col;

  /**
   * According errors object.
   */
  public final Errors errors;

  public Scanner(Reader r) {
    // store reader
    in = r;

    // initialize error handling support
    errors = new Errors();

    line = 1;
    col = 0;
    nextCh(); // read 1st char into ch, incr col to 1
  }

  /**
   * Adds error message to the list of errors.
   */
  public final void error(Token t, Errors.Message msg, Object... msgParams) {
    errors.error(t.line, t.col, msg, msgParams);

    // reset token content (consistent JUnit tests)
    t.numVal = 0;
    t.val = null;
  }


  // ================================================
  // Exercise 1: Implement Scanner (next() + private helper methods)
  // ================================================

  /**
   * Mapping from keyword names to appropriate token codes.
   */
  private static final Map<String, Token.Kind> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put(program.label(), program);
    keywords.put(class_.label(), class_);
    keywords.put(if_.label(), if_);
    keywords.put(else_.label(), else_);
    keywords.put(while_.label(), while_);
    keywords.put(read.label(), read);
    keywords.put(print.label(), print);
    keywords.put(return_.label(), return_);
    keywords.put(break_.label(), break_);
    keywords.put(void_.label(), void_);
    keywords.put(final_.label(), final_);
    keywords.put(new_.label(), new_);
    keywords.put(singleton.label(), singleton);
  }

  /**
   * Returns next token. To be used by parser.
   */
  public Token next() {
    while (Character.isWhitespace(ch)) {
      nextCh();
    }
    Token t = new Token(none, line, col);

    switch (ch) {
      case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
      case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
      case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
      case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
        readName(t); break;
      case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
        readNumber(t); break;
      case ';':
        nextCh();
        t.kind = semicolon;
        break;
      case '.':
        nextCh();
        t.kind = period;
        break;
      case ',':
        nextCh();
        t.kind = comma;
        break;
      case '(':
        nextCh();
        t.kind = lpar;
        break;
      case ')':
        nextCh();
        t.kind = rpar;
        break;
      case '[':
        nextCh();
        t.kind = lbrack;
        break;
      case ']':
        nextCh();
        t.kind = rbrack;
        break;
      case '{':
        nextCh();
        t.kind = lbrace;
        break;
      case '}':
        nextCh();
        t.kind = rbrace;
        break;
      case '\'':
        readCharConst(t);
        break;
      case EOF:
        t.kind = eof;
        break;
      case '+':
        nextCh();
        if (ch == '+') {
          nextCh(); t.kind = pplus;
        } else if (ch == '=') {
          nextCh(); t.kind = plusas;
        } else {
          t.kind = plus;
        }
        break;
      case '-':
        nextCh();
        if (ch == '-') {
          nextCh(); t.kind = mminus;
        } else if (ch == '=') {
          nextCh(); t.kind = minusas;
        } else {
          t.kind = minus;
        }
        break;
      case '*':
        nextCh();
        if (ch == '=') {
          nextCh(); t.kind = timesas;
        } else {
          t.kind = times;
        }
        break;
      case '%':
        nextCh();
        if (ch == '=') {
          nextCh(); t.kind = remas;
        } else {
          t.kind = rem;
        }
        break;
      case '!':
        nextCh();
        if (ch == '=') {
          nextCh(); t.kind = neq;
        } else {
          error(t, Errors.Message.INVALID_CHAR, prevCh);
        }
        break;
      case '<':
        nextCh();
        if (ch == '=') {
          nextCh(); t.kind = leq;
        } else {
          t.kind = lss;
        }
        break;
      case '>':
        nextCh();
        if (ch == '=') {
          nextCh(); t.kind = geq;
        } else {
          t.kind = gtr;
        }
        break;
      case '|':
        nextCh();
        if (ch == '|') {
          nextCh(); t.kind = or;
        } else {
          error(t, Errors.Message.INVALID_CHAR, prevCh);
        }
        break;
      case '=':
        nextCh();
        if (ch == '=') {
          nextCh(); t.kind = eql;
        } else {
           t.kind = assign;
        }
        break;
      case '&':
        nextCh();
        if (ch == '&') {
          nextCh(); t.kind = and;
        } else {
          error(t, Errors.Message.INVALID_CHAR, prevCh);
        }
        break;
      case '/':
        nextCh();
        if (ch == '*') {
          skipComment(t); t = next();
        } else if (ch == '=') {
          nextCh(); t.kind = slashas;
        } else {
          t.kind = slash;
        }
        break;
      default:
        error(t, Errors.Message.INVALID_CHAR, ch);
        nextCh();
        break;
    }
    return t;
  }

  /**
   * Reads next character from input stream into ch. Keeps pos, line and col
   * in sync with reading position.
   */
  private void nextCh() {
    try {
      prevCh = ch;
      ch = (char) in.read();
      col++;
      if(ch == LF) {
        line++;
        col = 0;
      }
    } catch (IOException e) {
      ch = EOF;
    }
  }

  private void readName(Token t) {
    StringBuilder sb = new StringBuilder();
    while (isLetter(ch) || isDigit(ch) || ch == '_') {
      sb.append(ch);
      nextCh();
    }
    String name = sb.toString();
    t.kind = keywords.getOrDefault(name, ident);
    t.val = name;
  }

  void readNumber(Token t) {
    StringBuilder sb = new StringBuilder();
    while (isDigit(ch)) {
      sb.append(ch);
      nextCh();
    }
    t.val = sb.toString();
    try {
      t.numVal = Integer.parseInt(t.val);
    } catch (NumberFormatException e) {
      error(t, Errors.Message.BIG_NUM, t.val);
    }
    t.kind = Token.Kind.number;
  }

  void readCharConst(Token t) {
    nextCh();

    //if immediately after beginning apostrophe there is another apostrophe -> error
    if(ch == '\'') {
      error(t, Errors.Message.EMPTY_CHARCONST);
      t.val = "\0";
      nextCh();
      t.kind = charConst;
      return;
    }

    //if there is a CR or LF in a charConst (not the char, but the symbol itself) -> error
    if(ch == '\r' || ch == '\n') {
      error(t, Errors.Message.ILLEGAL_LINE_END);
      t.val = "\0";
      nextCh();
      t.kind = charConst;
      return;
    }

    //different checks for escaped chars
    if(ch == '\\') {
      nextCh();
        switch (ch) {
          case 'n':
            t.val = "\n";
            t.numVal = '\n';
            break;
          case 'r':
            t.val = "\r";
            t.numVal = '\r';
            break;
          case '\'':
            t.val = "'";
            t.numVal = '\'';
            break;
          case '\\':
            t.val = "\\";
            t.numVal = '\\';
            break;
          default: //if any char other than r, n, \ or ' is escaped -> error
            error(t, Errors.Message.UNDEFINED_ESCAPE, ch);
            t.val = "\0";
            break;
        }
        nextCh();
      } else if(ch != EOF) { //for any other char apart from EOF in a char sequence
        t.val = String.valueOf(ch);
        t.numVal = ch;
        nextCh();
      }
      if(ch != '\'') { //if after a char there is no apostrophe right after -> error
        if(ch == EOF && (prevCh == '\'' || prevCh == '\\')) { //if there is an EOF directly after an apostrophe -> EOF error
          error(t, Errors.Message.EOF_IN_CHAR);
        } else { //if there is anything else after a char or an EOF after a valid symbol
          error(t, Errors.Message.MISSING_QUOTE);
        }
        t.val = "\0";
      } else { //set ch to the character after the last apostrophe
        nextCh();
      }
    t.kind = charConst;
  }

  void skipComment(Token t) {
    nextCh();
    int depth = 1; //start with depth 1 since first comment is already being detected in next()

    while(depth > 0) {
      while(ch != '/' && ch != '*' && ch != EOF) { //skip everything that is a comment
        nextCh();
      }

      if(ch == EOF) { //case of EOF without closed comment
        error(t, Errors.Message.EOF_IN_COMMENT);
        break;
      } else if (ch == '/') { //case of "/*"
        nextCh();
        if(ch == '*') {
          depth++;
          nextCh();
        }
      } else { //case of "*/"
        nextCh();
        if(ch == '/') {
          depth--;
          nextCh();
        }
      }
    }
  }

  private boolean isLetter(char c) {
    return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
  }

  private boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  // ================================================
  // ================================================
}
