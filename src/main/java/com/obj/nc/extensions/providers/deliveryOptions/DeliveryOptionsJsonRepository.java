package com.obj.nc.extensions.providers.deliveryOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.utils.JsonUtils;

import lombok.Data;

@Data
public class DeliveryOptionsJsonRepository {

    public static DeliveryOptionsJsonRepository loadRepository(String optionsRepoPathAndFileName) {
        Path configJsonFile = Paths.get(optionsRepoPathAndFileName);
                
        DeliveryOptionsJsonRepository repo = JsonUtils.readObjectFromJSONFile(configJsonFile, DeliveryOptionsJsonRepository.class);
        return repo;
    }

    public void flushRepository(String optionsRepoPathAndFileName) {
        Path configJsonFile = Paths.get(optionsRepoPathAndFileName);

        JsonUtils.writeObjectToJSONFile(configJsonFile, this);
    }

    private SpamPreventionOption emailGlobal;
    private Map<String,SpamPreventionOption> email = new HashMap<>();

    private SpamPreventionOption slackGlobal;
    private Map<String,SpamPreventionOption> slack = new HashMap<>();

    private SpamPreventionOption smsGlobal;
    private Map<String,SpamPreventionOption> sms = new HashMap<>();

    private SpamPreventionOption teamsGlobal;
    private Map<String,SpamPreventionOption> teams = new HashMap<>();

    private SpamPreventionOption pushGlobal;
    private Map<String,SpamPreventionOption> push = new HashMap<>();

    private SpamPreventionOption mailChimpGlobal;
    private Map<String,SpamPreventionOption> mailChimp = new HashMap<>();

}
