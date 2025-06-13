package com.example.endtoendencryptionsystem.entiy

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.endtoendencryptionsystem.entiy.database.key.SignalIdentityKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalPreKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSenderKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSession
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSignedPreKey
import com.example.endtoendencryptionsystem.repository.KeyRepository
import kotlinx.coroutines.runBlocking
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.groups.SenderKeyName
import org.whispersystems.libsignal.groups.state.SenderKeyRecord
import org.whispersystems.libsignal.groups.state.SenderKeyStore
import org.whispersystems.libsignal.state.IdentityKeyStore
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.PreKeyStore
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SessionStore
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyStore
import org.whispersystems.libsignal.util.KeyHelper
import java.util.Arrays
import java.util.Base64


@RequiresApi(Build.VERSION_CODES.O)
class PersistentSignalProtocolStore(
    private val keyRepository: KeyRepository,
    private val userId: String
) :  SignalProtocolStore,  SenderKeyStore {
      
    private var identityKeyPair: IdentityKeyPair  
    private var registrationId: Int  
      
    init {  
        // 通过 ChatRepository 初始化密钥  
        runBlocking {  
            val existingKey = keyRepository.getIdentityKey(userId)
            if (existingKey != null) {  
                identityKeyPair = IdentityKeyPair(Base64.getDecoder().decode(existingKey.identityKeyPair))  
                registrationId = existingKey.registrationId  
            } else {  
                identityKeyPair = KeyHelper.generateIdentityKeyPair()  
                registrationId = KeyHelper.generateRegistrationId(false)

                keyRepository.saveIdentityKey(
                    SignalIdentityKey(  
                        userId = userId,  
                        identityKeyPair = Base64.getEncoder().encodeToString(identityKeyPair.serialize()),  
                        registrationId = registrationId  
                    )  
                )  
            }  
        }  
    }  
      
    // IdentityKeyStore 实现  
    override fun getIdentityKeyPair(): IdentityKeyPair = identityKeyPair  
    override fun getLocalRegistrationId(): Int = registrationId
    override fun saveIdentity(
        address: SignalProtocolAddress?,
        identityKey: IdentityKey?
    ): Boolean {
        if (address == null || identityKey == null) return false

        return runBlocking {
            val existing = keyRepository.getIdentityKey("${address.name}:${address.deviceId}")
            keyRepository.saveIdentityKey(
                SignalIdentityKey(
                    userId = "${address.name}:${address.deviceId}",
                    identityKeyPair = Base64.getEncoder().encodeToString(identityKey.serialize()),
                    registrationId = 0 // 对于远程身份密钥，registrationId 不重要
                )
            )
            existing == null || !Arrays.equals(existing.identityKeyPair.toByteArray(), identityKey.serialize())
        }
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress?,
        identityKey: IdentityKey?,
        direction: IdentityKeyStore.Direction?
    ): Boolean {
        if (address == null || identityKey == null) return false

        return runBlocking {
            val trusted = keyRepository.getIdentityKey("${address.name}:${address.deviceId}")
            trusted == null || Arrays.equals(
                Base64.getDecoder().decode(trusted.identityKeyPair),
                identityKey.serialize()
            )
        }
    }

    override fun getIdentity(address: SignalProtocolAddress?): IdentityKey? {
        if (address == null) return null

        return runBlocking {
            val identityKey = keyRepository.getIdentityKey("${address.name}:${address.deviceId}")
            if (identityKey != null) {
                IdentityKey(Base64.getDecoder().decode(identityKey.identityKeyPair), 0)
            } else {
                null
            }
        }
    }

    // PreKeyStore 实现  
    override fun loadPreKey(preKeyId: Int): PreKeyRecord {  
        return runBlocking {  
            val preKey = keyRepository.getPreKey(preKeyId, userId)
            if (preKey != null) {  
                PreKeyRecord(Base64.getDecoder().decode(preKey.keyPair))  
            } else {  
                throw InvalidKeyIdException("No such prekey: $preKeyId")  
            }  
        }  
    }  
      
    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {  
        runBlocking {
            keyRepository.savePreKey(
                SignalPreKey(  
                    keyId = preKeyId,  
                    userId = userId,  
                    keyPair = Base64.getEncoder().encodeToString(record.serialize())  
                )  
            )  
        }  
    }  
      
    override fun containsPreKey(preKeyId: Int): Boolean {  
        return runBlocking {  
            keyRepository.getPreKey(preKeyId, userId) != null
        }  
    }  
      
    override fun removePreKey(preKeyId: Int) {
        Log.e("xxxx","走removePreKey")
        runBlocking {
            keyRepository.deletePreKey(preKeyId, userId)
        }  
    }  
      
    // SessionStore 实现  
    override fun loadSession(address: SignalProtocolAddress): SessionRecord {  
        return runBlocking {  
            val sessionKey = "${address.name}:${address.deviceId}"  
            val session = keyRepository.getSession(sessionKey, userId)
            if (session != null) {  
                SessionRecord(Base64.getDecoder().decode(session.sessionRecord))  
            } else {  
                SessionRecord()  
            }  
        }  
    }

    override fun getSubDeviceSessions(name: String?): List<Int?>? {
        if (name == null) return emptyList()

        return runBlocking {
            // 查询所有以该名称开头的会话
            val sessions = keyRepository.getAllSessions(userId)
            sessions.filter { it.sessionKey.startsWith("$name:") }
                .mapNotNull {
                    val parts = it.sessionKey.split(":")
                    if (parts.size >= 2) parts[1].toIntOrNull() else null
                }
        }
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        Log.e("xxx","走这儿")
        runBlocking {  
            val sessionKey = "${address.name}:${address.deviceId}"
            keyRepository.saveSession(
                SignalSession(  
                    sessionKey = sessionKey,  
                    userId = userId,  
                    sessionRecord = Base64.getEncoder().encodeToString(record.serialize())  
                )  
            )  
        }  
    }

    override fun containsSession(address: SignalProtocolAddress?): Boolean {
        if (address == null) return false

        return runBlocking {
            val sessionKey = "${address.name}:${address.deviceId}"
            keyRepository.getSession(sessionKey, userId) != null
        }
    }

    override fun deleteSession(address: SignalProtocolAddress?) {
        if (address == null) return

        runBlocking {
            val sessionKey = "${address.name}:${address.deviceId}"
            keyRepository.deleteSession(sessionKey, userId)
        }
    }

    override fun deleteAllSessions(name: String?) {
        if (name == null) return

        runBlocking {
            keyRepository.deleteAllSessionsForName(name, userId)
        }
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord? {
        return runBlocking {
            val signedPreKey = keyRepository.getSignedPreKey(signedPreKeyId, userId)
            if (signedPreKey != null) {
                SignedPreKeyRecord(Base64.getDecoder().decode(signedPreKey.keyPair))
            } else {
                throw InvalidKeyIdException("No such signed prekey: $signedPreKeyId")
            }
        }
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord?>? {
        return runBlocking {
            val signedPreKeys = keyRepository.getAllSignedPreKeys(userId)
            signedPreKeys.map {
                SignedPreKeyRecord(Base64.getDecoder().decode(it.keyPair))
            }
        }
    }

    override fun storeSignedPreKey(
        signedPreKeyId: Int,
        record: SignedPreKeyRecord
    ) {
        runBlocking {
            keyRepository.saveSignedPreKey(
                SignalSignedPreKey(
                    keyId = signedPreKeyId,
                    userId = userId,
                    keyPair = Base64.getEncoder().encodeToString(record.serialize()),
                    signature = Base64.getEncoder().encodeToString(record.signature),
                    timestamp = record.timestamp
                )
            )
        }
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return runBlocking {
            keyRepository.getSignedPreKey(signedPreKeyId, userId) != null
        }
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        runBlocking {
            keyRepository.deleteSignedPreKey(signedPreKeyId, userId)
        }
    }

    override fun storeSenderKey(
        senderKeyName: SenderKeyName?,
        record: SenderKeyRecord?
    ) {
        if (senderKeyName == null || record == null) return
        runBlocking {
            keyRepository.saveSenderKey(
                SignalSenderKey(
                    senderKeyName = senderKeyName.serialize(),
                    userId = userId,
                    groupId = senderKeyName.serialize().split("::")[0],
                    senderKeyRecord = Base64.getEncoder().encodeToString(record.serialize())
                )
            )
        }
    }

    override fun loadSenderKey(senderKeyName: SenderKeyName?): SenderKeyRecord? {
        if (senderKeyName == null) return SenderKeyRecord()
        return runBlocking {
            val senderKey = keyRepository.getSenderKey(senderKeyName.serialize(), userId)
            if (senderKey != null) {
                SenderKeyRecord(Base64.getDecoder().decode(senderKey.senderKeyRecord))
            } else {
                SenderKeyRecord()
            }
        }
    }


}