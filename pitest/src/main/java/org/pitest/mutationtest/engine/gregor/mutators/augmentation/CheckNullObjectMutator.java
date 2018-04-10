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
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;

/* M1 mutation, check for null before dereferencing.
 * 
 * field dereferencing are called using GETFIELD in java bytecode
 * Loading objects are loaded using ALOAD and ALOAD_n 
 * This code stops at GETFIELD and PUTFIELD. We skip ALOAD because 
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
     * Override visitFieldInsn to check for PUTFIELD or GETFIELD
     * If the bytecode is PUTFIELD or GETFIELD, perform a nullcheck.
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.GETFIELD) {
            final MutationIdentifier muID = this.context.registerMutation(factory, "Checked for NULL object here.");

            if (this.context.shouldMutate(muID)) {
                addIfNullCondition(opcode, owner, name, desc);
                // addAssertNullMethod();

            }
        } else {
            super.visitFieldInsn(opcode,owner, name, desc);
        }
    }

    /*
     * Use JUnit assertNotNull to check object/item for null. This will throw an
     * error
     */
    private void addAssertNullMethod() {
        // need to add another ALOAD here, but I don't know the location on the stack.
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertNull", "(Ljava/lang/Object;)V", false);
        // super.visitEnd();
    }

    /*
     * Use IFNONNULL to check for null object before dereferencing.
     */
    private void addIfNullCondition(int opcode, String owner, String name, String desc) {

        // create a label to mark where IFNONNULL jump to
        Label ifNotNull = new Label();
        Label ifNull = new Label();
        Label returnToNormalCode = new Label();
        // copy the object from ALOAD to use in IFNULL. If it is null, skip the dereference section

        super.visitInsn(Opcodes.DUP);
        super.visitJumpInsn(Opcodes.IFNULL, ifNull);
        super.visitLabel(ifNotNull);
        
        //the original GETFIELD/PUTFIELD instruction
        super.visitFieldInsn(opcode, owner, name, desc);
        super.visitJumpInsn(Opcodes.GOTO, returnToNormalCode);
        
        // set the location so IFNULL skip dereferencing and go here. POP the object reference because we don't need it on the stack
        super.visitLabel(ifNull);
        super.visitInsn(Opcodes.POP);
        super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        super.visitLdcInsn("Object is null, skip dereferencing.");
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "<init>", "(Ljava/lang/String;)V", false);
    
        //set the location to exit the modified section.
        super.visitLabel(returnToNormalCode);
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