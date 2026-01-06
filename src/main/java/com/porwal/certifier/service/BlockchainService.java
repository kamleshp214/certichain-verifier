package com.porwal.certifier.service;
import com.porwal.certifier.model.Block;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
@Service
public class BlockchainService {
    public static List<Block> blockchain = new ArrayList<>();
    public BlockchainService() {
        if (blockchain.isEmpty()) {
            blockchain.add(new Block("Genesis Block", "System Init", "0"));
        }
    }
    public Block addCertificate(String studentName, String courseName) {
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(studentName, courseName, previousBlock.hash);
        blockchain.add(newBlock);
        return newBlock;
    }
    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            Block currentBlock = blockchain.get(i);
            Block previousBlock = blockchain.get(i - 1);
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) return false;
            if (!currentBlock.previousHash.equals(previousBlock.hash)) return false;
        }
        return true;
    }
    public List<Block> getChain() { return blockchain; }
}
