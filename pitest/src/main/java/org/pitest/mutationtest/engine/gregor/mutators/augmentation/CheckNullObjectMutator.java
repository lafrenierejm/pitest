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
 * Loading objects are loaded using ALOAD and ALOAD_n 
 *  
 */
public enum CheckNullObjectMutator implements MethodMutatorFactory {

    CHECK_NULL_OBJECT_MUTATOR;

    /*
     * Create a MethodVisitor class to add null check (CheckNullObjectVisitor)
     * 
     * @see
     * org.pitest.mutationtest.engine.gregor.MethodMutatorFactory#create(org.pitest.
     * mutationtest.engine.gregor.MutationContext,
     * org.pitest.mutationtest.engine.gregor.MethodInfo,
     * org.objectweb.asm.MethodVisitor)
     */
    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor mv) {
        return new ArithmeticOperatorReplacementMethodVisitor(this, methodInfo, context, mv);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + name();
    }

    @Override
    public String getName() {
        return "Check if object is null - " + name();
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
        if (opcode == Opcodes.ALOAD || opcode == Opcodes.GETFIELD) {
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
        // need to add another ALOAD here, but I don't know the location on the stack.
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertNull", "(Ljava/lang/Object;)V", false);
        // super.visitEnd();
    }

    /*
     * Use IFNULL to check object is null If it is null, throw NullPointerException
     * I don't know if this will work.
     */
    private void addIfNullCondition() {
        
        Label beforeIf = new Label();
        Label afterIf;
        super.visitVarInsn(Opcodes.ALOAD, 1);
        super.visitJumpInsn(Opcodes.IFNULL, afterIf);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NullPointerException", "<init>",
                "(Ljava/lang/String;)V", false);

        super.visitEnd();

    }

    /*
     * We don't introduce any new variable, just one computation, so the maxStack
     * should increase by 1. In FrameOptions.java, the ClassWriter already has
     * COMPUTE_FRAME so maybe this stack size will be automatically calculated.
     * 
     * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }

}