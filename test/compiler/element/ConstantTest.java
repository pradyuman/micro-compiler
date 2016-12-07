package compiler.element;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConstantTest {

    @Test
    public void generateConstantInt() throws Exception {
        Element actual = Constant.parse("20");
        assertTrue(actual.isConstant());
        assertTrue(actual.isInt());
        assertEquals("20", actual.getValue());
    }

    @Test
    public void generateConstantNegativeInt() throws Exception {
        Element actual = Constant.parse("-20");
        assertTrue(actual.isConstant());
        assertTrue(actual.isInt());
        assertEquals("-20", actual.getValue());
    }

    @Test
    public void generateConstantFloat() throws Exception {
        Element actual = Constant.parse("22.44");
        assertTrue(actual.isConstant());
        assertTrue(actual.isFloat());
        assertEquals("22.44", actual.getValue());
    }

    @Test
    public void generateConstantNegativeFloat() throws Exception {
        Element actual = Constant.parse("-22.44");
        assertTrue(actual.isConstant());
        assertTrue(actual.isFloat());
        assertEquals("-22.44", actual.getValue());
    }
}