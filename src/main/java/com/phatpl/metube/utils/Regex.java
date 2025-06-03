package com.phatpl.metube.utils;


/*
        Username:
            -Must start with an alphabetic character. Can contain the following characters: [a-z] [A-Z] [0-9] and _
        Email:
            - at least 8 characters
            - must contain at least 1 uppercase letter, 1 lowercase letter, and 1 number
            - Can contain special characters
        Password:
            # start-of-string
            # a digit must occur at least once
            # a lower case letter must occur at least once
            # an upper case letter must occur at least once
            # a special character must occur at least once
            # no whitespace allowed in the entire string
            # anything, at least eight places though
            # end-of-string
     */
public class Regex {
    public static final String EMAIL = "^[a-zA-Z0-9].[a-zA-Z0-9\\._%\\+\\-]{0,63}@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,30}$";
    public static final String USERNAME = "^[A-Za-z][A-Za-z0-9_]{7,29}$";
    public static final String PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
}
