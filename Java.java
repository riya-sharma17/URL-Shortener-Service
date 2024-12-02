import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;

public class URLShortener {

  
    private static final String BASE_URL = "http://short.ly/";  // Base URL for shortening
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_URL_LENGTH = 6;

    private static Map<String, String> urlMap = new HashMap<>();  // Stores short URL to original URL mapping
    private static Map<String, Integer> clickCountMap = new HashMap<>();  // Stores the click counts for each short URL
    private static Map<String, Long> expirationMap = new HashMap<>();  // Stores expiration time for each URL

    private static AtomicInteger urlCount = new AtomicInteger(0);  // Tracks the number of URLs created

    private static String generateShortUrl() {
        StringBuilder shortUrl = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            shortUrl.append(CHARSET.charAt(rand.nextInt(CHARSET.length())));
        }
        return shortUrl.toString();
    }

 
    private static String shortenUrl(String originalUrl, long expirationTime) {
        String shortUrl = generateShortUrl();
        while (urlMap.containsKey(shortUrl)) {  // Ensure uniqueness
            shortUrl = generateShortUrl();
        }

        urlMap.put(shortUrl, originalUrl);
        expirationMap.put(shortUrl, expirationTime);
        clickCountMap.put(shortUrl, 0);  // Initially 0 clicks
        urlCount.incrementAndGet();

        return BASE_URL + shortUrl;
    }

    private static String retrieveOriginalUrl(String shortUrl) {
        String shortUrlKey = shortUrl.replace(BASE_URL, "");
        if (urlMap.containsKey(shortUrlKey)) {
            long expirationTime = expirationMap.get(shortUrlKey);
            if (System.currentTimeMillis() > expirationTime) {
                urlMap.remove(shortUrlKey);
                clickCountMap.remove(shortUrlKey);
                expirationMap.remove(shortUrlKey);
                return "This URL has expired.";
            }
            clickCountMap.put(shortUrlKey, clickCountMap.get(shortUrlKey) + 1);  // Increment click count
            return urlMap.get(shortUrlKey);
        }
        return null;
    }

    private static String getUrlStats(String shortUrl) {
        String shortUrlKey = shortUrl.replace(BASE_URL, "");
        if (clickCountMap.containsKey(shortUrlKey)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return "Short URL: " + shortUrl + "\n" +
                   "Click count: " + clickCountMap.get(shortUrlKey) + "\n" +
                   "Expiration time: " + sdf.format(new Date(expirationMap.get(shortUrlKey)));
        }
        return "No stats available for the given URL.";
    }

    // Get total number of URLs created
    private static int getTotalUrlsCreated() {
        return urlCount.get();
    }


    public static void main(String[] args) {
      
        JFrame frame = new JFrame("URL Shortener");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

       
        panel.add(new JLabel("Enter URL:"));
        JTextField urlField = new JTextField(30);
        panel.add(urlField);
        
        panel.add(new JLabel("Enter Expiration (in hours):"));
        JTextField expirationField = new JTextField(10);
        panel.add(expirationField);
        
        JButton shortenButton = new JButton("Shorten URL");
        panel.add(shortenButton);

     
        JTextArea resultArea = new JTextArea(3, 40);
        resultArea.setEditable(false);
        panel.add(new JScrollPane(resultArea));

    
        panel.add(new JLabel("Enter Short URL:"));
        JTextField shortUrlField = new JTextField(30);
        panel.add(shortUrlField);

        JButton retrieveButton = new JButton("Retrieve Original URL");
        panel.add(retrieveButton);

      
        JTextArea statsArea = new JTextArea(5, 40);
        statsArea.setEditable(false);
        panel.add(new JScrollPane(statsArea));

     
        shortenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String originalUrl = urlField.getText();
                    int expirationHours = Integer.parseInt(expirationField.getText());
                    long expirationTime = System.currentTimeMillis() + expirationHours * 3600000L;  // Expiration in ms
                    String shortUrl = shortenUrl(originalUrl, expirationTime);
                    resultArea.setText("Short URL: " + shortUrl);
                } catch (Exception ex) {
                    resultArea.setText("Error: Invalid input.");
                }
            }
        });

   
        retrieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String shortUrl = shortUrlField.getText();
                String originalUrl = retrieveOriginalUrl(shortUrl);
                if (originalUrl != null) {
                    statsArea.setText("Original URL: " + originalUrl);
                    statsArea.append("\n" + getUrlStats(shortUrl));
                } else {
                    statsArea.setText("Invalid or expired short URL.");
                }
            }
        });

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setSize(600, 400);
        frame.setVisible(true);
    }
}
