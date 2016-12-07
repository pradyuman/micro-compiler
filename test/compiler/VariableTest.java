package compiler;

import compiler.element.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class VariableTest {

    @Test
    public void generateConstantInt() throws Exception {
        Element actual = Element.parseConstant("20");
        assertTrue(actual.isConstant());
        assertTrue(actual.isInt());
        assertEquals("20", actual.getValue());
    }

    @Test
    public void generateConstantNegativeInt() throws Exception {
        Element actual = Element.parseConstant("-20");
        assertTrue(actual.isConstant());
        assertTrue(actual.isInt());
        assertEquals("-20", actual.getValue());
    }

    @Test
    public void generateConstantFloat() throws Exception {
        Element actual = Element.parseConstant("22.44");
        assertTrue(actual.isConstant());
        assertTrue(actual.isFloat());
        assertEquals("22.44", actual.getValue());
    }

    @Test
    public void generateConstantNegativeFloat() throws Exception {
        Element actual = Element.parseConstant("-22.44");
        assertTrue(actual.isConstant());
        assertTrue(actual.isFloat());
        assertEquals("-22.44", actual.getValue());
    }
}