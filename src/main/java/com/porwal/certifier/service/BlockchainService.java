package com.porwal.certifier.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.porwal.certifier.model.Block;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class BlockchainService {
    
    // --- YOUR CREDENTIALS (ALREADY FILLED) ---
    private final String GITHUB_TOKEN = "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"; 
    private final String REPO_OWNER = "kamleshp214";           
    private final String REPO_NAME = "certichain-verifier";    
    // -----------------------------------------

    public static List<Block> blockchain = new ArrayList<>();
    private final String FILE_PATH = "ledger.json"; // The file that will appear in your repo
    private final String API_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + FILE_PATH;
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public BlockchainService() {
        // 1. Try to load existing chain from GitHub on startup
        loadChainFromGitHub();
        
        // 2. If it's a new repo/file doesn't exist, create Genesis Block
        if (blockchain.isEmpty()) {
            blockchain.add(new Block("Genesis Block", "System Init", "0"));
            // Save this initial block to GitHub so the file is created
            saveChainToGitHub();
        }
    }

    public Block addCertificate(String studentName, String courseName) {
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(studentName, courseName, previousBlock.hash);
        blockchain.add(newBlock);
        
        // Save to GitHub immediately
        saveChainToGitHub();
        return newBlock;
    }

    public Block findBlock(String hash) {
        for (Block block : blockchain) {
            if (block.hash.equals(hash)) return block;
        }
        return null;
    }

    public List<Block> getChain() { return blockchain; }

    // --- GITHUB SYNC LOGIC ---

    private void saveChainToGitHub() {
        try {
            // Convert Blockchain to JSON
            String jsonContent = mapper.writeValueAsString(blockchain);
            
            // Base64 Encode (GitHub API requirement)
            String base64Content = Base64.getEncoder().encodeToString(jsonContent.getBytes(StandardCharsets.UTF_8));
            
            // Get SHA of existing file (needed for updates)
            String sha = getFileSha();

            // Create Payload
            String jsonBody = "{"
                    + "\"message\": \"New Block Added\","
                    + "\"content\": \"" + base64Content + "\""
                    + (sha != null ? ", \"sha\": \"" + sha + "\"" : "") 
                    + "}";

            // Send PUT Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .header("Content-Type", "application/json")
                    .method("PUT", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(">> SYNC SUCCESS: Blockchain saved to GitHub ledger.json");

        } catch (Exception e) {
            System.err.println(">> SYNC ERROR: " + e.getMessage());
        }
    }

    private void loadChainFromGitHub() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> map = mapper.readValue(response.body(), Map.class);
                String encodedContent = ((String) map.get("content")).replace("\n", "");
                
                String decodedJson = new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
                
                blockchain = mapper.readValue(decodedJson, new TypeReference<List<Block>>(){});
                System.out.println(">> LOAD SUCCESS: Loaded " + blockchain.size() + " blocks from GitHub.");
            }
        } catch (Exception e) {
            System.out.println(">> STARTING FRESH: No ledger.json found on GitHub.");
        }
    }

    private String getFileSha() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> map = mapper.readValue(response.body(), Map.class);
                return (String) map.get("sha");
            }
        } catch (Exception ignored) {}
        return null;
    }
}
// inside BlockchainService.java...

public Block addCertificate(String studentName, String courseName, String organization, String instructor) {
    Block previousBlock = blockchain.get(blockchain.size() - 1);
    // Pass new fields to Block constructor
    Block newBlock = new Block(studentName, courseName, organization, instructor, previousBlock.hash);
    blockchain.add(newBlock);
    
    saveChainToGitHub(); // Keep your GitHub sync logic
    return newBlock;
}

// Update Genesis block in Constructor to avoid errors
public BlockchainService() {
    loadChainFromGitHub();
    if (blockchain.isEmpty()) {
        // Fix Genesis block arguments
        blockchain.add(new Block("Genesis", "Init", "System", "Admin", "0")); 
        saveChainToGitHub();
    }
}
