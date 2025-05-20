package com.example.endtoendencryptionsystem.rsa.group;

import java.util.HashMap;
import java.util.Map;  
import java.util.UUID;

/**
 * 为每个群组创建唯一ID
 */
public class DistributionIdManager {  
    private static final Map<String, String> groupDistributionIds = new HashMap<>();  
      
    public static String getOrCreateDistributionId(String groupId) {  
        if (!groupDistributionIds.containsKey(groupId)) {  
            groupDistributionIds.put(groupId, UUID.randomUUID().toString());  
        }  
        return groupDistributionIds.get(groupId);  
    }  
}