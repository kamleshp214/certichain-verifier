package com.porwal.certifier.model;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
public class Block {
    public String hash;
    public String previousHash;
    private String studentName;
    private String courseName;
    private String issueDate;
    private long timeStamp;
    private int nonce;
    public Block(String studentName, String courseName, String previousHash) {
        this.studentName = studentName;
        this.courseName = courseName;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.issueDate = new Date().toString();
        this.hash = calculateHash();
    }
    public String calculateHash() {
        String dataToHash = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + studentName + courseName;
        return applySha256(dataToHash);
    }
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getStudentName() { return studentName; }
    public String getCourseName() { return courseName; }
    public String getIssueDate() { return issueDate; }
}
