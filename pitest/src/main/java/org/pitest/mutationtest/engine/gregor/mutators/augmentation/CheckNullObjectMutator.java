package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
//import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/* M1 mutation, check for null before dereferencing.
 * 
 * field dereferencing are called using GETFIELD in java bytecode
 * Invoking object method are called using INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC.
 *  
 */
public enum CheckNullObjectMutator implements MethodMutatorFactory {

    CHECK_NULL_OBJECT_MUTATOR;
    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor mv) {
        return new ArithmeticOperatorReplacementMethodVisitor(this, methodInfo, context, mv);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return name();
    }

}

/*
 * Two ways to do this. One is using Junit assertNotNull.Two is using IFNULL in
 * ASM.Extends ASM MethodVisitor to manipulate bytecode that goes into
 * MethodVisitor.
 */
class CheckNullObjectVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    String mName;
    int line;

    CheckNullObjectVisitor(final MethodMutatorFactory factory, final MutationContext context, final MethodVisitor mv) {
        super(Opcodes.ASM6, mv);
        this.factory = factory;
        this.context = context;
    }

    /*
     * If the bytecode is ALOAD, perform a null check. AALOAD is for object
     * reference. ALOAD is for object.
     */
    public void visitObjectLoad(int opcode) {
        if (opcode == Opcodes.ALOAD) {
            final MutationIdentifier muID = this.context.registerMutation(factory, "Checked for NULL object here.");

            if (this.context.shouldMutate(muID)) {
                // addIfNullCondition();
                addAssertNullMethod();

            }
        } else {
            super.visitInsn(opcode);
        }
    }

    /*
     * Use JUnit assertNotNull to check object/item for null
     */
    private void addAssertNullMethod() {
        //need to add another ALOAD here, but I don't know the location on the stack.
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertNull", "(Ljava/lang/Object;)V", false);
        super.visitVarInsn(Opcodes.ALOAD, 0);
        super.visitEnd();
    }

    /*
     * Use IFNULL to check object is null If it is null, throw NullPointerException
     * I don't know if this will work.
     */
    private void addIfNullCondition() {
        Label afterIf = new Label();
        Label beforeIf = new Label();
        super.visitVarInsn(Opcodes.ALOAD, 1);
        super.visitJumpInsn(Opcodes.IFNULL, beforeIf);
        super.visitLabel(afterIf);
        super.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
        super.visitInsn(Opcodes.DUP);
        super.visitLdcInsn("Object is null.");
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V",
                false);

        super.visitEnd();

    }

}