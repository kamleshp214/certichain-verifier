package com.porwal.certifier.controller;

import com.porwal.certifier.model.Block;
import com.porwal.certifier.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") // Allow frontend to access this
@RequestMapping("/api")
public class CertController {

    @Autowired
    BlockchainService blockchainService;

    // Issue a new certificate
    @PostMapping("/issue")
    public Block issueCertificate(@RequestParam String name, @RequestParam String course) {
        return blockchainService.addCertificate(name, course);
    }

    // Verify/View all certificates
    @GetMapping("/verify")
    public List<Block> getBlockchain() {
        return blockchainService.getChain();
    }
    
    // NEW: Verify a specific single certificate by Hash (for QR code)
    @GetMapping("/verify/{hash}")
    public Block verifyCertificate(@PathVariable String hash) {
        return blockchainService.findBlock(hash);
    }
}
