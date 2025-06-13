import android.os.Parcelable
import com.tencent.mmkv.MMKV

// 扩展函数：简化 Parcelable 读取
inline fun <reified T : Parcelable> MMKV.decodeParcelableCompat(
    key: String,
    defaultValue: T? = null
): T? {
    return decodeParcelable(key, T::class.java, defaultValue)
}