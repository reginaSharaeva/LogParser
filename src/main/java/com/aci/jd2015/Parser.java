package com.aci.jd2015;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.*;
import java.lang.String;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Parser implements LogParser {

    List<String> logStrings;
    List<String> messageHeaders;
    List<Message> messages;
    public Parser(List<String> logStrings, List<Message> messages) {
        this.logStrings = logStrings;
        this.messages = messages;
    }

    public void process(InputStream is, OutputStream os) throws IOException {
        try (BufferedReader bufIn = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"))) {
            String controlSum = null;
            String line = bufIn.readLine();
            while (line != null) {
                while (!line.matches("^CRC_.{0,1024}")) {
                    if (line.matches("^(0[1-9]|[12][0-9]|3[01])[.](0[1-9]|1[012])[.](19|20)\\d\\d\\s([0-1]\\d|2[0-3])(:[0-5]\\d){2}[.]\\d{3}.{0,1024}")
                            || line.matches(".*.{0,1024}")) {
                        char[] lineChars = line.toCharArray();
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < lineChars.length; i++) {
                            if (lineChars[i] != '\n' || lineChars[i] != '\r') {
                                stringBuilder.append(lineChars[i]);
                            }
                        }
                        logStrings.add(stringBuilder.toString());
                        line = bufIn.readLine();
                    } else {
                        throw new InputMismatchException("Incorrect line" + line);
                    }
                }
                if (line.matches("^CRC_.{0,1024}")) {
                    controlSum = line;
                }
                messageHeaders = new ArrayList<>();
                for (int i = 0; i < logStrings.size(); i++) {
                    if (logStrings.get(i).matches("^(0[1-9]|[12][0-9]|3[01])[.](0[1-9]|1[012])[.](19|20)\\d\\d\\s([0-1]\\d|2[0-3])(:[0-5]\\d){2}[.]\\d{3}.{0,1024}")) {
                        messageHeaders.add(logStrings.get(i));
                    }
                }
                boolean flag = false;
                int k = 0;
                while (!flag && k < messageHeaders.size()) {
                    String header = messageHeaders.get(k);
                    flag = check(header, controlSum, bufferedWriter);
                    k++;
                }
                if (!flag) {
                    throw new InputMismatchException("No message for checksum" + controlSum);
                }
                messageHeaders.remove(k-1);
                line = bufIn.readLine();
            }
            if (!logStrings.isEmpty()) {
                throw new InputMismatchException("Incorrect input data");
            }
            writeMessages(bufferedWriter);
        }
    }

    /**
     * Searching the message for checksum
     * true - message was found
     * @param header
     * @param controlSum
     * @param bufferedWriter
     * @return
     */
    public boolean check(String header, String controlSum, BufferedWriter bufferedWriter) {
        List<String> messageStrings = new ArrayList<>();
        String control = controlSum.substring(4);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(header);
        if (DigestUtils.md5Hex(stringBuilder.toString()).equals(control)) {
            logStrings.remove(headerIndex(header));
            addMessage(messageStrings, header, controlSum, bufferedWriter);
            return true;
        } else {
            int index = headerIndex(header);
            for (int j = index + 1; j < logStrings.size(); j++) {
                if (!logStrings.get(j).matches("^(0[1-9]|[12][0-9]|3[01])[.](0[1-9]|1[012])[.](19|20)\\d\\d\\s([0-1]\\d|2[0-3])(:[0-5]\\d){2}[.]\\d{3}.{0,1024}")) {
                    stringBuilder.append(logStrings.get(j));
                    messageStrings.add(logStrings.get(j));
                }
            }
            if (DigestUtils.md5Hex(stringBuilder.toString()).equals(control)) {
                addMessage(messageStrings, header, controlSum, bufferedWriter);
                logStrings.remove(index);
                for (int i = index; i < logStrings.size(); i++) {
                    for (int j = 0; j < messageStrings.size(); j++) {
                        if (logStrings.get(i).equals(messageStrings.get(j))) {
                            logStrings.remove(i);
                        }
                    }
                }
                return true;
            } else {
                stringBuilder.delete(header.length(), stringBuilder.length());
                return search(messageStrings, controlSum, stringBuilder, header, bufferedWriter);
            }
        }
    }


    /**
     * Full search
     * @param messageStrings
     * @param controlSum
     * @param stringBuilder
     * @param header
     * @param bufferedWriter
     * @return
     */
    public boolean search(List<String> messageStrings, String controlSum, StringBuilder stringBuilder, String header,
                          BufferedWriter bufferedWriter) {

        if (messageStrings.isEmpty()) {
            return false;
        }
        int i = messageStrings.size() - 1;
        while (i > 0) {
            int[] a = new int[i];
            boolean check;
            check = forSearch(0, 0, i, a, messageStrings, stringBuilder, controlSum, header, bufferedWriter);
            if (check == true) {
                return true;

            }
            stringBuilder.delete(header.length(), stringBuilder.length());
            i--;
        }
        return false;
    }

    public boolean forSearch(int start, int end, int count, int[] a, List<String> messageStrings,
                          StringBuilder stringBuilder, String controlSum, String header,
                          BufferedWriter bufferedWriter) {
        List<String> newMesStrings = new ArrayList<>();
        if (start == count) {
            for (int i = 0; i < a.length; i++) {
                newMesStrings.add(messageStrings.get(a[i] - 1));
                stringBuilder.append(messageStrings.get(a[i] - 1));
            }
            if (DigestUtils.md5Hex(stringBuilder.toString()).equals(controlSum.substring(4))) {
                addMessage(newMesStrings, header, controlSum, bufferedWriter);
                int index = headerIndex(header);
                logStrings.remove(index);
                for (int i = index; i < logStrings.size(); i++) {
                    for (int j = 0; j < newMesStrings.size(); j++) {
                        if (logStrings.get(i).equals(newMesStrings.get(j))) {
                            logStrings.remove(i);
                        }
                    }
                }
                return true;
            }
        } else {
            stringBuilder.delete(header.length(), stringBuilder.length());
            for(int i = end + 1; i <= messageStrings.size(); i++) {
                a[start] = i;
                return forSearch(start + 1, i, count, a, messageStrings, stringBuilder, controlSum, header, bufferedWriter);
            }
        }
        return false;
    }

    public void addMessage(List<String> mStrings, String header, String control, BufferedWriter bufferedWriter) {
        try {
            Date date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").parse(header.substring(0, 23));
            List<String> list = new ArrayList<>();
            list.add(header + "\r\n");
            if (mStrings != null) {
                for (String str: mStrings) {
                    list.add(str + "\r\n");
                }
            }
            list.add(control + "\r\n");
            messages.add(new Message(date, list));
            Collections.sort(messages);
            if (messages.get(messages.size() - 1).getMessageDate().getTime() - messages.get(0).getMessageDate().getTime() > 12000) {
                writeMessages(bufferedWriter);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void writeMessages(BufferedWriter bufferedWriter) {
        try {
            Collections.sort(messages);
            for (int i = 0; i < messages.size(); i++) {
                for (int y = 0; y < messages.get(i).getMessageString().size(); y++) {
                    bufferedWriter.write(messages.get(i).getMessageString().get(y));
                }
            }
            messages.clear();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int headerIndex(String header) {
        for (int y = 0; y < logStrings.size(); y++) {
            if (logStrings.get(y).equals(header)) {
                return y;
            }
        }
        return logStrings.size() - 1;
    }

}
