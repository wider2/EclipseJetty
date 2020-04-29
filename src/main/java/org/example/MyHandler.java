package org.example;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class MyHandler extends AbstractHandler {

    private List<BigInteger> inputNumbers = initList();
    private BigInteger result;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request.getMethod().equalsIgnoreCase("POST")) {
            BufferedReader reader = request.getReader();
            String line = reader.readLine();
            reader.close();
            logger.info("Logger line: "+ line);

            if (line.equals("finish")) {
                doSumNumbers();
            } else {
                try {
                    doAddNumber(new BigInteger(line));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println(result);
        }
    }

    private static List<BigInteger> initList() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    private void doSumNumbers() {
        synchronized (this) {
            result = new BigInteger("0");
            for (BigInteger inputNumber : inputNumbers) {
                result = result.add(inputNumber);
            }
            logger.info("Logger result: "+ result);
            inputNumbers = initList();
            notifyAll();
        }
    }

    private void doAddNumber(BigInteger number) throws InterruptedException {
        inputNumbers.add(number);
        logger.info("Logger add number: "+ number);
        synchronized (this) {
            wait();
        }
    }
}
