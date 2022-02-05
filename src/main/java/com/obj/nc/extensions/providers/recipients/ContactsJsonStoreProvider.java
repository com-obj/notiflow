package com.obj.nc.extensions.providers.recipients;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.recipients.Recipient;
import com.obj.nc.utils.JsonUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(value="nc.contacts-store.jsonStorePathAndFileName")
@Data
@Slf4j
public class ContactsJsonStoreProvider implements ContactsProvider {
    
    @JsonIgnore
    private String recipientsStorePathAndFileName;

    private List<Recipient> contacts = new ArrayList<>();

    @JsonIgnore
    private HashMap<UUID, Recipient> indexById = new HashMap<>();
    @JsonIgnore
    private HashMap<String, Recipient> indexByName = new HashMap<>();
    @JsonIgnore
    private HashMap<String, Recipient> indexByEndpointId = new HashMap<>();
    @JsonIgnore
    private HashMap<String, ReceivingEndpoint> indexEndpointByEndpointId = new HashMap<>();

    public ContactsJsonStoreProvider(
        @Value("${nc.contacts-store.jsonStorePathAndFileName}") String recipientsStorePathAndFileName) {

        this.recipientsStorePathAndFileName = recipientsStorePathAndFileName;

        loadRepository();
    }
    
    public void loadRepository() {
        Path repoFile = getRepoFileAsPath();
                
        JsonUtils.readObjectFromJSONFileToInstance(repoFile, this);        

        indexRepository();
    }

    private void indexRepository() {
        contacts.forEach(rec -> {
                indexById.put(rec.getId(), rec);
                indexByName.put(rec.getName(),rec);
                
                if (rec.getReceivingEndpoints()== null) {
                    return;
                }

                rec.getReceivingEndpoints().forEach( endP -> {
                    indexByEndpointId.put(endP.getEndpointId(), rec);
                    indexEndpointByEndpointId.put(endP.getEndpointId(), endP);
                });
            }
        );
    }

    public void flushRepository() {
        Path repoFile = getRepoFileAsPath();

        JsonUtils.writeObjectToJSONFile(repoFile, this);
    }

    private Path getRepoFileAsPath() {
        try {
            Path configJsonFile = Paths.get(recipientsStorePathAndFileName);
            if (!configJsonFile.toFile().exists()) {   
                Files.createDirectories(configJsonFile.getParent());
                configJsonFile.toFile().createNewFile();
                Files.writeString(configJsonFile, "{}");
            }

            return configJsonFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRecipients(Recipient ... recipients) {
        contacts.addAll(Arrays.asList(recipients));
    }

    @Override
    public List<Recipient> findRecipients(UUID... recipientsIds) {
        List<Recipient> result = new ArrayList<>();
        for (UUID id: recipientsIds) {
            Recipient recipient = indexById.get(id);
            if (recipient == null)  {
                log.warn("Didn't fine Recipient with id {}", id);
                continue;
            }

            result.add(recipient);
        }

        return result;
    }

    @Override
    public List<Recipient> findRecipientsByName(String... recipientNames) {
        List<Recipient> result = new ArrayList<>();
        for (String name: recipientNames) {
            Recipient recipient = indexByName.get(name);
            if (recipient == null)  {
                log.warn("Didn't fine Recipient with name {}", name);
                continue;
            }

            result.add(recipient);
        }
        
        return result;
    }

    @Override
    public List<ReceivingEndpoint> findEndpoints(UUID forRecipient) {
        List<ReceivingEndpoint> result = new ArrayList<>();

        List<Recipient> recipients = findRecipients(forRecipient);
        for (Recipient recipient: recipients) {
            result.addAll(recipient.getReceivingEndpoints());
        }

        return result;
    }

    @Override
    public ReceivingEndpoint findEndpoint(String forEndpointId) {
        ReceivingEndpoint endpoint = indexEndpointByEndpointId.get(forEndpointId);

        if (endpoint == null)  {
            log.warn("Didn't fine Endpoint with endpointId {}", forEndpointId);
        }
        
        return endpoint;    }

    @Override
    public Recipient findRecipients(String forEndpointId) {        
        Recipient recipient = indexByEndpointId.get(forEndpointId);
        if (recipient == null)  {
            log.warn("Didn't fine Recipient with endpointId {}", forEndpointId);
        }
        
        return recipient;
    }
}
