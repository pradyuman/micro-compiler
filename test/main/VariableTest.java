package main;

import org.junit.Test;

import static org.junit.Assert.*;

public class VariableTest {

    @Test
    public void generateConstantInt() throws Exception {
        Variable actual = Variable.generateConstant("20");
        assertTrue(actual.isConstant());
        assertTrue(actual.isInt());
        assertEquals("20", actual.getValue());
    }

    @Test
    public void generateConstantNegativeInt() throws Exception {
        Variable actual = Variable.generateConstant("-20");
        assertTrue(actual.isConstant());
        assertTrue(actual.isInt());
        assertEquals("-20", actual.getValue());
    }

    @Test
    public void generateConstantFloat() throws Exception {
        Variable actual = Variable.generateConstant("22.44");
        assertTrue(actual.isConstant());
        assertTrue(actual.isFloat());
        assertEquals("22.44", actual.getValue());
    }

    @Test
    public void generateConstantNegativeFloat() throws Exception {
        Variable actual = Variable.generateConstant("-22.44");
        assertTrue(actual.isConstant());
        assertTrue(actual.isFloat());
        assertEquals("-22.44", actual.getValue());
    }
}