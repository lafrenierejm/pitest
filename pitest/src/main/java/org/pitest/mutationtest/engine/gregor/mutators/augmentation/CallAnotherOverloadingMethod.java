package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
//import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum CallAnotherOverloadingMethod implements MethodMutatorFactory {

    CALL_OVERLOADING_METHOD;

    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor) {
        return new ReplaceWithOverloadingMethod(this, context, methodVisitor);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + name();
    }

    @Override
    public String getName() {
        return "Replaced with overloading method - " + name();
    }
    /*
     * TODO M2 mutation Invoking object method are called using INVOKEVIRTUAL,
     * INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC. Overloading methods are method
     * that have the same name and return type, but different parameters.
     * INVOKEVIRTUAL utd/Add.add(III)I INVOKEVIRTUAL utd/Add.add(II)I
     * 
     * Meaning: static method vs non-static method can overload each other ->
     * INVOKEVIRTUAL and INVOKESTATIC package name is the same (overload, not
     * override) same classname and method name (utd/Add.add) same return type
     * different descriptor/parameter.
     * 
     * This should work with constructor as well.
     * 
     * How do I dissect the method descriptor?
     */

}

class ReplaceWithOverloadingMethod extends MethodVisitor {
    private final MethodMutatorFactory factory;
    private final MutationContext context;

    ReplaceWithOverloadingMethod(final MethodMutatorFactory factory, final MutationContext context,
            final MethodVisitor mv) {
        super(Opcodes.ASM6, mv);
        this.factory = factory;
        this.context = context;
    }

    /*
     * If the INVOKE keywords exist, then start replacing it with other overloading
     * methods.
     */
    public void visitInvokeKeyword(int opcode) {
        if (opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.INVOKESTATIC
                || opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEDYNAMIC) {
            final MutationIdentifier muID = this.context.registerMutation(factory,
                    "Replace with overloading method here.");

            if (this.context.shouldMutate(muID)) {
                //
                replaceMethodDescriptor(opcode);
            }
        } else {
            super.visitInsn(opcode);
        }
    }

    /*
     * Inject bytecode to replace method descriptor.
     */
    private void replaceMethodDescriptor(int opcode) {
        // TODO how do I do this?
        super.vistMethodInsn(opcode, ..., "<init>",   ,false);
        
        /*
         * use the same opcode, same name. Just change the descriptor
         */

    }

}