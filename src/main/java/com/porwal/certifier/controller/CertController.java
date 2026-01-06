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
    @PostMapping("/issue")
    public Block issueCertificate(@RequestParam String name, @RequestParam String course) {
        return blockchainService.addCertificate(name, course);
    }
    @GetMapping("/verify")
    public List<Block> getBlockchain() {
        return blockchainService.getChain();
    }
}
