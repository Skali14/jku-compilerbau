package ssw.mj;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class TracingClassLoader extends ClassLoader {
  private ClassPool pool;

  public TracingClassLoader(ClassLoader parent) {
    super(parent);

    // The class pool is used by Javassist to hold the classes that are being manipulated
    // We create it once and it stores the CtClass objects of all loaded classes
    pool = ClassPool.getDefault();
    // We need to add the class path to the ClassPool
    // otherwise, Javassist will not be able to find classes on the class path
    addClassPathToPool();
  }

  private void addClassPathToPool() {
    // Hopefully, this is the correct way to get the class path
    String classpath = System.getProperty("java.class.path");
    String[] classpathEntries = classpath.split(File.pathSeparator);
    for (String cp : classpathEntries) {
      try {
        pool.appendClassPath(cp);
      } catch (NotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /*
  We do not rely on the parent class loader to load the class. Instead, we use Javassist to
  manipulate the class as needed and then define the class using the byte code generated by Javassist.

  If we followed the default delegation model for loading classes, we would override the findClass method.
  But this method would not be called if the parent class loader can find the class. Therefore, we override
  the loadClass method instead.
  */
  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    // Check if the class has already been loaded by this class loader
    Class<?> foundClazz = findLoadedClass(name);
    if (foundClazz != null) {
      // otherwise we get java.lang.LinkageError: loader ssw.mj.TracingClassLoader @<hashcode> attempted duplicate class definition for <classname>
      return foundClazz;
    }
    /* if (name.startsWith("java.")
            || name.startsWith("javax.")
            || name.startsWith("jdk.internal.")
            || name.startsWith("sun.")
            || name.startsWith("com.sun.")
            || name.startsWith("org.w3c.")
            || name.startsWith("org.xml.")
    ) { */
    if (!name.startsWith("ssw")) {
      // class library classes should be loaded by the bootstrap class loader
      // there are three class loaders: bootstrap, extension, and system class loader
      // - the bootstrap class loader is implemented in native code and is responsible for loading the core Java classes
      // - the extension class loader (also called platform class loader) is implemented in Java and is responsible for loading the classes in the extension directories (i.e., jre/lib/ext)
      // - the system class loader (also sometimes called the app class loader) is implemented in Java and is responsible for loading the classes in the class path
      // --- The TracingClassLoader is meant to be used as the system class loader
      return super.loadClass(name);
    }
    CtClass cc = null;
    try {
      cc = pool.get(name);
      if (!cc.isFrozen() && cc.getName().equals("ssw.mj.impl.Parser")) {
        cc.addField(CtField.make("public static ssw.mj.Recorder __recorder__ = new ssw.mj.Recorder();", cc));

        // Difference between getMethods and getDeclaredMethods: https://stackoverflow.com/a/73069812/2938364
        CtMethod[] methods = cc.getDeclaredMethods();
        for (CtMethod method : methods) {
          // System.out.println("Instrumenting: " + method.getName());

          // TODO: Check if EBNF contains rule for method.getName() (ignoreCase)
          // e.g., we want to instrument program or ProGram if EBNF has a production for Program = ... .
          // We could use an array/set with all production names for this.
          if (Character.isUpperCase(method.getName().charAt(0))) {
            method.insertBefore("__recorder__.customEnter(t, la, \"%s\");".formatted(method.getName()));
            method.insertAfter("__recorder__.exit(t, la);");
          } else if (method.getName().equals("parse")) {
            // We need to reset the recorder once we perform a new parse
            // since it's declared as a static field in the parser
            method.insertBefore("__recorder__.reset();");
          } else if (method.getName().equals("check")) {
            method.insertBefore("__recorder__.checkEnter(t, la, $1);");
            method.insertAfter("__recorder__.exit(t, la);");
          } else if (method.getName().equals("scan")) {
            method.insertBefore("__recorder__.scanEnter(t, la);");
            method.insertAfter("__recorder__.exit(t, la);");
          } else if (method.getName().equals("error")) {
            method.instrument(new ExprEditor() {
              @Override
              public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().equals("ssw.mj.Errors") && m.getMethodName().equals("error")) {
                  // We record enter and exit directly one after another, but we do not need more details than that
                  method.insertAt(m.getLineNumber(), "__recorder__.errorEnter(t, la, $1, $2); __recorder__.exit(t, la);");
                }
                super.edit(m);
              }
            });
          } else if (method.getName().startsWith("recover")) {
            method.insertBefore("__recorder__.recoverEnter(t, la, \"%s\");".formatted(method.getName()));
            method.insertAfter("__recorder__.exit(t, la);");
          }
        }
      }

      cc.debugWriteFile();

      byte[] byteCode = cc.toBytecode();
      Class<?> newClazz = defineClass(name, byteCode, 0, byteCode.length);
      return newClazz;
    } catch (NotFoundException | CannotCompileException | IOException e) {
      e.printStackTrace();
      System.err.println("Could not load " + name + "\n" + e);
      throw new ClassNotFoundException("Could not load " + name, e);
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }

  /**
   * See ClassLoaders.AppClassLoader
   * Called by the VM to support dynamic additions to the class path
   *
   * @see java.lang.instrument.Instrumentation#appendToSystemClassLoaderSearch
   */
  void appendToClassPathForInstrumentation(String path) {
    try {
      System.out.println("appendToClassPathForInstrumentation:" + path);
      pool.appendClassPath(path);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public URL getResource(String name) {
    if (name.contains("JUnitStarter")) {
      System.out.println("JUnitStarter");
    }
    return super.getResource(name);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (name.contains("JUnitStarter")) {
      System.out.println("JUnitStarter");
    }
    return super.getResources(name);
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    if (name.contains("JUnitStarter")) {
      System.out.println("JUnitStarter");
    }
    return super.getResourceAsStream(name);
  }
}
