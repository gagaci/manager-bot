package com.company.manager2.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class GoogleSheetService {

  private static final String SHEET_ID = "1evq7Ho5kDfgNr-p4WcdZikG-2Mr-BRfPzFcabkJgvz0";
  private static final String SHEET_URL =
      "https://docs.google.com/spreadsheets/d/" + SHEET_ID + "/gviz/tq?tqx=out:json";

  public  String getWebLink() throws Exception {
    URL url = new URL(SHEET_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");


    Scanner scanner = new Scanner(conn.getInputStream());
    StringBuilder response = new StringBuilder();
    while (scanner.hasNext()) {
      response.append(scanner.nextLine());
    }
    scanner.close();

    String json = response.toString();
    return extractWebinarLink(json);
  }

  private static String extractWebinarLink(String json) {
    Pattern pattern = Pattern.compile("\"v\":\"(https[^\"]+)\""); // Extracts the first HTTPS link
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "No link found!";
  }

}
