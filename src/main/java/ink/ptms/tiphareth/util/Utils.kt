package ink.ptms.tiphareth.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*

object Utils {

    fun getHashCode(file: File): String? {
        FileInputStream(file).use {
            return try {
                val md = MessageDigest.getInstance("MD5")
                val buffer = ByteArray(1024)
                var length = -1
                while (length != -1) {
                    length = it.read(buffer, 0, 1024)
                    md.update(buffer, 0, length)
                }
                return UUID.nameUUIDFromBytes(md.digest()).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}