/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Extract different parts of a method.
 */
public class MethodInfo {

  private final ClassInfo owningClass;
  private final int       access;
  private final String    methodName;
  private final String    methodDescriptor;

  public MethodInfo() {
    this(new ClassInfo(0, 0, "", "", "", new String[0]), 0, "", "()V");
  }

  private MethodInfo(final ClassInfo owningClass, final int access,
      final String name, final String methodDescriptor) {
    this.owningClass = owningClass;
    this.access = access;
    this.methodName = name;
    this.methodDescriptor = methodDescriptor;
  }

  public String getDescription() {
    return this.owningClass.getName() + "::" + getName();
  }

  public String getName() {
    return this.methodName;
  }

  public String getMethodDescriptor() {
    return this.methodDescriptor;
  }

  @Override
  public String toString() {
    return "MethodInfo [access=" + this.access + ", desc="
        + this.methodDescriptor + ",  name=" + this.methodName + "]";
  }

  /**
   * Check whether or not this method is static.
   *
   * @return true iff the method is static
   */
  public boolean isStatic() {
    return ((this.access & Opcodes.ACC_STATIC) != 0);
  }

  /**
   * Check whether or not this method is synthetic.
   *
   * @return true iff the method is synthetic
   */
  public boolean isSynthetic() {
    return ((this.access & Opcodes.ACC_SYNTHETIC) != 0);
  }

  /**
   * Check whether or not this method is a constructor.
   *
   * @return true iff the method is a constructor
   */
  public boolean isConstructor() {
    return isConstructor(this.methodName);
  }

  /**
   * Check whether or not this method contains "<init>" in the class file (constructor).
   *
   * @return true iff the method contains "<init>"
   */
  public static boolean isConstructor(final String methodName) {
    return "<init>".equals(methodName);
  }

  /**
   * Get this method's return type.
   *
   * @return the return type of the method
   */
  public Type getReturnType() {
    return Type.getReturnType(this.methodDescriptor);
  }

  /**
   * Check whether or not a method returns void.
   *
   * @return true iff the method returns void.
   */
  public static boolean isVoid(final String desc) {
    return Type.getReturnType(desc).equals(Type.VOID_TYPE);
  }

  public boolean isStaticInitializer() {
    return "<clinit>".equals(this.methodName);
  }

  public boolean isVoid() {
    return isVoid(this.methodDescriptor);
  }

  public boolean takesNoParameters() {
    return this.methodDescriptor.startsWith("()");
  }

  public boolean isInGroovyClass() {
    return this.owningClass.isGroovyClass();
  }

  public boolean isGeneratedEnumMethod() {
    return this.owningClass.isEnum()
        && (isValuesMethod() || isValueOfMethod() || isStaticInitializer());
  }

  private boolean isValuesMethod() {
    return this.getName().equals("values") && takesNoParameters() && isStatic();
  }

  private boolean isValueOfMethod() {
    return this.getName().equals("valueOf")
        && this.methodDescriptor.startsWith("(Ljava/lang/String;)")
        && isStatic();
  }

  public MethodInfo withMethodDescriptor(final String newDescriptor) {
    return new MethodInfo(this.owningClass, this.access, this.methodName,
        newDescriptor);
  }

  public MethodInfo withAccess(final int accessModifier) {
    return new MethodInfo(this.owningClass, accessModifier, this.methodName,
        this.methodDescriptor);
  }

  public MethodInfo withMethodName(final String newMethodName) {
    return new MethodInfo(this.owningClass, this.access, newMethodName,
        this.methodDescriptor);
  }

  public MethodInfo withOwner(final ClassInfo newOwnerClass) {
    return new MethodInfo(newOwnerClass, this.access, this.methodName,
        this.methodDescriptor);
  }

}
