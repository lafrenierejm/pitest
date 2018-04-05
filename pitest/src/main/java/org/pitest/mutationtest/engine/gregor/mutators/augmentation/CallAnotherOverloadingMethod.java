package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

public class CallAnotherOverloadingMethod {

    public CallAnotherOverloadingMethod() {
        /* TODO M2 mutation
         * Invoking object method are called using INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC.
         * Overloading methods are method that have the same name and return type, but different parameters.
         *     INVOKEVIRTUAL utd/Add.add(III)I
         *     INVOKEVIRTUAL utd/Add.add(II)I
         *     
         * Meaning:
         * static method vs non-static method can overload each other -> INVOKEVIRTUAL and INVOKESTATIC
         * package name is the same (overload, not override)
         * same classname and method name (utd/Add.add)
         * same return type
         * different descriptor/parameter.
         * 
         * This should work with constructor as well.
         * 
         * How do I dissect the method descriptor?
        */
    }

}