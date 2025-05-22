package com.example.endtoendencryptionsystem.rsa.group

import android.util.Log
import com.example.endtoendencryptionsystem.ETEApplication.Companion.getInstance
import com.example.endtoendencryptionsystem.entiy.database.SenderKeyEntity
import com.example.endtoendencryptionsystem.repository.ChatRepository
import org.whispersystems.libsignal.groups.SenderKeyName
import org.whispersystems.libsignal.groups.state.SenderKeyRecord
import org.whispersystems.libsignal.groups.state.SenderKeyStore

class MySenderKeyStore : SenderKeyStore {
    // 用于存储SenderKey记录的Map  
    private val senderKeys: MutableMap<String?, SenderKeyRecord?> =
        HashMap<String?, SenderKeyRecord?>()
    private val chatRepository = ChatRepository(getInstance()!!)

    override fun storeSenderKey(senderKeyName: SenderKeyName, record: SenderKeyRecord) {
        val keyId = senderKeyName.getGroupId() + "::" + senderKeyName.getSender().getName()
        val entity = SenderKeyEntity(
            keyId,
            record.serialize(),
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )
        try {
            chatRepository.insertOrUpdateSenderKey(entity)
        }catch (e: Exception){}

    }

    override fun loadSenderKey(senderKeyName: SenderKeyName): SenderKeyRecord {
        val keyId = "${senderKeyName.groupId}::${senderKeyName.sender.name}"
        var record = senderKeys[keyId]
        if (record == null) {
            // 从数据库加载
            try {
                val entity = chatRepository.getSenderKey(keyId)
                if (entity != null) {
                    record = SenderKeyRecord(entity.serializedRecord)
                    senderKeys[keyId] = record
                }
            } catch (e: Exception) {
                Log.e("xxxx", "Error loading sender key", e)
            }
        }

        return record ?: SenderKeyRecord()
    }
}