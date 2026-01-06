package com.porwal.certifier.controller;

import com.porwal.certifier.model.Block;
import com.porwal.certifier.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") 
@RequestMapping("/api")
public class CertController {

    @Autowired
    BlockchainService blockchainService;

    // Updated Endpoint
    @PostMapping("/issue")
    public Block issueCertificate(
            @RequestParam String name, 
            @RequestParam String course,
            @RequestParam String org,
            @RequestParam String instructor) {
        return blockchainService.addCertificate(name, course, org, instructor);
    }

    @GetMapping("/verify")
    public List<Block> getBlockchain() {
        return blockchainService.getChain();
    }
    
    @GetMapping("/verify/{hash}")
    public Block verifyCertificate(@PathVariable String hash) {
        return blockchainService.findBlock(hash);
    }
}
