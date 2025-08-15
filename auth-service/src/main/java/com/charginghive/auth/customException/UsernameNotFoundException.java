package com.charginghive.auth.customException;


public class UsernameNotFoundException extends Exception {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
