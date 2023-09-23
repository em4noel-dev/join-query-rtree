/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.obinject.exception;

/**
 *
 * @author windows
 */
public class TransientObjectException extends RuntimeException{
    
    public TransientObjectException(String obj, String ref, String typeRef) {
        super("Reference in object [" + obj + "." + ref + 
                "] has an unsaved transient instance [" + typeRef + "].");
    }

    public TransientObjectException(String obj) {
        super("Object " + obj + "is an unsaved transient instance");
    }

    
}
