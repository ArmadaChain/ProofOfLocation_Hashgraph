package com.armada.service;

import com.armada.util.JsonUtil;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

@Service
public class Client {

    private Properties prop;
    private com.hedera.hashgraph.sdk.Client hederaClient;
    private Ed25519PrivateKey key;
    private AccountId operatorId;
    private AccountId accountId;
    private Ed25519PrivateKey operatorKey;
    private String nodeAddress;
    private long maxTransactionFee;
    private ContractId contractId;
    private String contractJson;
    private String mode;
    private long gas;

    private Properties loadConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(inputStream);
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean isNewConfigUpdated() {
        Properties prop = this.loadConfig();
        return !prop.equals(this.prop);
    }

    private void updateProp(Properties prop) {
        this.prop = prop;
        this.key = Ed25519PrivateKey.fromString(prop.getProperty("operator-key"));
        this.operatorId = AccountId.fromString(prop.getProperty("operator-id"));
        this.operatorKey = Ed25519PrivateKey.fromString(prop.getProperty("operator-key"));
        this.accountId = AccountId.fromString(prop.getProperty("node-id"));
        this.nodeAddress = prop.getProperty("node-address");
        this.maxTransactionFee = Long.valueOf(prop.getProperty("max-transaction-fee"));
        this.mode = prop.getProperty("mode");
        this.contractJson = "advance".equals(this.mode) ? "ProofOfLocation.json" : "ProofOfLocationSimple.json";
        this.gas = Long.valueOf(prop.getProperty("gas"));
    }

    private com.hedera.hashgraph.sdk.Client createClient() {
        com.hedera.hashgraph.sdk.Client client = new com.hedera.hashgraph.sdk.Client(Map.of(accountId, nodeAddress));
        client.setOperator(this.operatorId, this.operatorKey);
        client.setMaxTransactionFee(this.maxTransactionFee);
        return client;
    }

    public synchronized com.hedera.hashgraph.sdk.Client getHederaClient() throws HederaException, IOException, URISyntaxException {
        if (this.isNewConfigUpdated() || this.hederaClient == null) {
            this.updateProp(this.loadConfig());
            this.hederaClient = this.createClient();
            this.contractId = this.createContract();
        }
        return hederaClient;
    }

    public synchronized ContractId getContract() throws HederaException, IOException, URISyntaxException {
        if (this.isNewConfigUpdated() || this.contractId == null) {
            this.updateProp(this.loadConfig());
            this.hederaClient = this.createClient();
            this.contractId = this.createContract();
        }
        return contractId;
    }

    public long getGas() {
        return gas;
    }

    public String getMode() {
        return mode;
    }

    public Transaction getPayment() {
        long cost = this.hederaClient.getMaxTransactionFee();
        return new CryptoTransferTransaction(hederaClient)
                .setNodeAccountId(this.accountId)
                .setTransactionId(new TransactionId(this.operatorId))
                .addSender(this.operatorId, cost)
                .addRecipient(this.accountId, cost)
                .build();
    }

    private ContractId createContract() throws URISyntaxException, IOException, HederaException {
        String contractJson = Files.readString(Path.of(getClass().getClassLoader().getResource(this.contractJson).toURI()), StandardCharsets.UTF_8);
        JsonObject jsonData = JsonUtil.parseJson(contractJson);
        Key publicKey = this.operatorKey.getPublicKey();

        FileCreateTransaction fileTx = new FileCreateTransaction(this.hederaClient)
                .setExpirationTime(Instant.now().plus(Duration.ofSeconds(3600)))
                .addKey(publicKey)
                .setContents(jsonData.getAsJsonPrimitive("object").getAsString().getBytes());
        TransactionReceipt fileReceipt = fileTx.executeForReceipt();
        FileId newFileId = fileReceipt.getFileId();

        ContractCreateTransaction contractTx = new ContractCreateTransaction(hederaClient)
                .setAutoRenewPeriod(Duration.ofHours(1))
                .setGas(this.gas)
                .setBytecodeFile(newFileId)
                .setAdminKey(publicKey);
        TransactionReceipt contractReceipt = contractTx.executeForReceipt();
        ContractId contractId = contractReceipt.getContractId();
        return contractId;
    }
}
