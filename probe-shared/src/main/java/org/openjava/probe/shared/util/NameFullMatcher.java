package org.openjava.probe.shared.util;

public class NameFullMatcher implements Matcher<String> {
    private final String name;

    public NameFullMatcher(String name) {
        this.name = name;
    }

    /**
     * (null, null)    == true
     * (1L,2L)         == false
     * (1L,1L)         == true
     * ("abc",null)    == false
     * (null,"abc")    == false
     */
    @Override
    public boolean match(String s) {
        return (name == null && s == null) || (name != null && s != null && name.equals(s));
    }
}
