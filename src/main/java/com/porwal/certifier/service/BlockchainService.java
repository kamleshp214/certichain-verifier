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
    
    // YOUR TOKEN
    private final String GITHUB_TOKEN = "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"; 
    private final String REPO_OWNER = "kamleshp214";           
    private final String REPO_NAME = "certichain-verifier";    
    
    public static List<Block> blockchain = new ArrayList<>();
    private final String FILE_PATH = "ledger.json"; 
    private final String API_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + FILE_PATH;
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public BlockchainService() {
        loadChainFromGitHub();
        
        if (blockchain.isEmpty()) {
            // Genesis block with updated fields
            blockchain.add(new Block("Genesis", "Init", "System", "Admin", "0"));
            saveChainToGitHub();
        }
    }

    // Updated to take 4 parameters
    public Block addCertificate(String studentName, String courseName, String organization, String instructor) {
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(studentName, courseName, organization, instructor, previousBlock.hash);
        blockchain.add(newBlock);
        
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
            String jsonContent = mapper.writeValueAsString(blockchain);
            String base64Content = Base64.getEncoder().encodeToString(jsonContent.getBytes(StandardCharsets.UTF_8));
            String sha = getFileSha();

            String jsonBody = "{"
                    + "\"message\": \"New Block Added\","
                    + "\"content\": \"" + base64Content + "\""
                    + (sha != null ? ", \"sha\": \"" + sha + "\"" : "") 
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .header("Content-Type", "application/json")
                    .method("PUT", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(">> SYNC SUCCESS: Blockchain saved to GitHub.");

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
                System.out.println(">> LOAD SUCCESS: Loaded " + blockchain.size() + " blocks.");
            }
        } catch (Exception e) {
            System.out.println(">> STARTING FRESH: No ledger.json found.");
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
