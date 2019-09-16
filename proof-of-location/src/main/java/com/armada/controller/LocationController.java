package com.armada.controller;

import com.armada.service.Client;
import com.armada.util.JsonUtil;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.FunctionResult;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.contract.ContractId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LocationController {

    @Autowired
    private
    Client client;

    @PostMapping(value = "/push-location-simple")
    public Map pushLocationSimple(@RequestBody(required = true)String jsonStr) throws HederaException, IOException, URISyntaxException {
        com.hedera.hashgraph.sdk.Client hederaClient = client.getHederaClient();
        ContractId contractId = client.getContract();

        JsonObject jsonData = JsonUtil.parseJson(jsonStr);
        String longAddress = jsonData.get("longitude").getAsString();
        String latAddress = jsonData.get("latitude").getAsString();

        Map result = new HashMap();
        new ContractExecuteTransaction(hederaClient)
                .setGas(this.client.getGas())
                .setContractId(contractId)
                .setFunctionParameters(CallParams.function("set_long").addString(longAddress))
                .execute();
        new ContractExecuteTransaction(hederaClient)
                .setGas(this.client.getGas())
                .setContractId(contractId)
                .setFunctionParameters(CallParams.function("set_lat").addString(latAddress))
                .execute();

        result.put("longitude", longAddress);
        result.put("latitude", latAddress);
        return result;
    }

    @PostMapping(value = "/push-location")
    public Map pushLocation(@RequestBody(required = true)String jsonStr) throws HederaException, IOException, URISyntaxException {
        com.hedera.hashgraph.sdk.Client hederaClient = client.getHederaClient();
        ContractId contractId = client.getContract();

        JsonObject jsonData = JsonUtil.parseJson(jsonStr);
        String longAddress = jsonData.get("longitude").getAsString();
        String latAddress = jsonData.get("latitude").getAsString();

        Map result = new HashMap();
        String item = jsonData.get("item").getAsString();

        new ContractExecuteTransaction(hederaClient)
                .setGas(this.client.getGas())
                .setContractId(contractId)
                .setFunctionParameters(
                        CallParams.function("appendItemLoc")
                                .addString(item)
                                .addString(longAddress)
                                .addString(latAddress)
                )
                .execute();
        result.put("item", item);
        result.put("longitude", longAddress);
        result.put("latitude", latAddress);
        return result;
    }

    @PostMapping(value = "/get-location-simple")
    public Map getLocationSimple() throws HederaException, IOException, URISyntaxException {
        com.hedera.hashgraph.sdk.Client hederaClient = client.getHederaClient();
        ContractId contractId = client.getContract();

        Map result = new HashMap();

        FunctionResult longQuery = new ContractCallQuery(hederaClient)
                .setPayment(client.getPayment())
                .setGas(this.client.getGas())
                .setContractId(contractId)
                .setFunctionParameters(CallParams.function("long_return"))
                .execute();
        FunctionResult latQuery = new ContractCallQuery(hederaClient)
                .setPayment(client.getPayment())
                .setGas(this.client.getGas())
                .setContractId(contractId)
                .setFunctionParameters(CallParams.function("lat_return"))
                .execute();

        result.put("longitude", longQuery.getString(0));
        result.put("latitude", latQuery.getString(0));
        return result;
    }

    @PostMapping(value = "/get-location")
    public Map getLocation(@RequestBody(required = true)String jsonStr) throws HederaException, IOException, URISyntaxException {
        com.hedera.hashgraph.sdk.Client hederaClient = client.getHederaClient();
        ContractId contractId = client.getContract();

        Map result = new HashMap();

        JsonObject jsonData = JsonUtil.parseJson(jsonStr);
        String item = jsonData.get("item").getAsString();

        FunctionResult query = new ContractCallQuery(hederaClient)
                .setPayment(client.getPayment())
                .setGas(this.client.getGas())
                .setContractId(contractId)
                .setFunctionParameters(CallParams.function("getAddress").addString(item))
                .execute();
        result.put("longitude", query.getString(0));
        result.put("latitude", query.getString(1));
        return result;
    }

}
