package ink.ptms.tiphareth.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * @Author sky
 * @Since 2019-11-28 22:45
 */
object Utils {

    fun getHashCode(file: File): ByteArray? {
        FileInputStream(file).use {
            return try {
                val md = MessageDigest.getInstance("SHA-1")
                val buffer = ByteArray(1024)
                var length = -1
                while (length != -1) {
                    length = it.read(buffer, 0, 1024)
                    md.update(buffer, 0, length)
                }
                md.digest()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}