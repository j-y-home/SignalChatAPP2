import android.util.Log
import com.example.endtoendencryptionsystem.ETEApplication
import com.example.endtoendencryptionsystem.repository.KeyRepository
import com.example.endtoendencryptionsystem.utils.EncryptionUtil


/**
 * 注册密钥管理类
 */
object SignalKeyManager {

    private val keyRepository by lazy {
        KeyRepository(ETEApplication.getInstance()!!)
    }

    /**
     * 判断本地是否有身份密钥
     */
    suspend fun hasLocalIdentityKey(userId: String): Boolean {
        return keyRepository.getIdentityKey(userId) != null
    }

    /**
     * 注册密钥
     */
    suspend fun registerNewKeysIfNecessary(userId: String): String {
        return if (!hasLocalIdentityKey(userId)) {
            Log.d("SignalKeyManager", "未找到本地身份密钥，开始注册")
            EncryptionUtil.registerKey()
        } else {
            Log.d("SignalKeyManager", "已有本地身份密钥，跳过注册")
            ""
        }
    }
}
