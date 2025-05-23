package com.example.endtoendencryptionsystem.entiy

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.endtoendencryptionsystem.entiy.database.key.SignalIdentityKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalPreKey
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
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyStore
import org.whispersystems.libsignal.util.KeyHelper
import java.util.Base64


@RequiresApi(Build.VERSION_CODES.O)
class PersistentSignalProtocolStore(
    private val keyRepository: KeyRepository,
    private val userId: String  
) : IdentityKeyStore, PreKeyStore, SessionStore, SignedPreKeyStore, SenderKeyStore {  
      
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
        TODO("Not yet implemented")
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress?,
        identityKey: IdentityKey?,
        direction: IdentityKeyStore.Direction?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getIdentity(address: SignalProtocolAddress?): IdentityKey? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {  
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
        TODO("Not yet implemented")
    }

    override fun deleteSession(address: SignalProtocolAddress?) {
        TODO("Not yet implemented")
    }

    override fun deleteAllSessions(name: String?) {
        TODO("Not yet implemented")
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord? {
        TODO("Not yet implemented")
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord?>? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        TODO("Not yet implemented")
    }

    override fun storeSenderKey(
        senderKeyName: SenderKeyName?,
        record: SenderKeyRecord?
    ) {
        TODO("Not yet implemented")
    }

    override fun loadSenderKey(senderKeyName: SenderKeyName?): SenderKeyRecord? {
        TODO("Not yet implemented")
    }

    // 其他方法类似实现...  
}