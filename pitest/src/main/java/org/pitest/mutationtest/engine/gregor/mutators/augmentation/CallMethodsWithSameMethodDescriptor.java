package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

public class CallMethodsWithSameMethodDescriptor {

    public CallMethodsWithSameMethodDescriptor() {
        /*
         * TODO M3 mutation Since this is only calling methods, it will be
         * INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC. Only need to
         * keep the method descriptor the same. 
         * 
         * INVOKEVIRTUAL utd/Add.add(III)I
         * INVOKEVIRTUAL utd/Add.add(II)I
         */
    }

}