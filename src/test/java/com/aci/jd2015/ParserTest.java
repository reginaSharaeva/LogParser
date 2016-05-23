package com.aci.jd2015;

import java.io.*;
import java.util.ArrayList;
import java.util.InputMismatchException;

import org.junit.Test;

public class ParserTest {

    @Test
    public void testExample() throws IOException {
        InputStream in = new FileInputStream("src/example.log");
        OutputStream out = new FileOutputStream("src/result.log");
        new Parser(new ArrayList<String>(), new ArrayList<Message>()).process(in, out);
    }

    @Test
    public void testExampleOk() throws IOException {
        InputStream in = new FileInputStream("src/exampleOk.log");
        OutputStream out = new FileOutputStream("src/resultOk.log");
        new Parser(new ArrayList<String>(), new ArrayList<Message>()).process(in, out);
    }

    @Test(expected = InputMismatchException.class)
    public void testNoMessageForCRC() throws IOException {
        InputStream in = new FileInputStream("src/example1.log");
        OutputStream out = new FileOutputStream("src/result1.log");
        new Parser(new ArrayList<String>(), new ArrayList<Message>()).process(in, out);
    }

    @Test(expected = InputMismatchException.class)
    public void testIncorrectLine() throws IOException {
        InputStream in = new FileInputStream("src/example2.log");
        OutputStream out = new FileOutputStream("src/result2.log");
        new Parser(new ArrayList<String>(), new ArrayList<Message>()).process(in, out);
    }

    @Test(expected = InputMismatchException.class)
    public void testIncorrectInputData() throws IOException {
        InputStream in = new FileInputStream("src/example3.log");
        OutputStream out = new FileOutputStream("src/result3.log");
        new Parser(new ArrayList<String>(), new ArrayList<Message>()).process(in, out);
    }
}
