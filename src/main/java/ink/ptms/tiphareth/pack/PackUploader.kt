package ink.ptms.tiphareth.pack

import com.aliyun.oss.OSSClientBuilder
import ink.ptms.tiphareth.Tiphareth
import org.bukkit.Material
import java.io.File
import java.io.FileInputStream

object PackUploader {

    var packURL = "https://${getBucketName()}.${getEndPoint()}/${getObjectPath()}"
        private set

    var isEnable = getEndPoint() != null
        private set

    fun upload(resourcePack: File): Boolean {
        if (isEnable) {
            Material.SPLASH_POTION
            try {
                val ossClient = OSSClientBuilder().build(getEndPoint(), getAccessKeyId(), getAccessKeySecret())
                FileInputStream(resourcePack).use { inputStream ->
                    ossClient.putObject(getBucketName(), getObjectPath(), inputStream)
                    ossClient.shutdown()
                }
                return true
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        return false
    }

    private fun getEndPoint(): String? = Tiphareth.CONF.getString("automatically-upload.endpoint")

    private fun getBucketName(): String? = Tiphareth.CONF.getString("automatically-upload.bucket-name")

    private fun getObjectPath(): String? = Tiphareth.CONF.getString("automatically-upload.object-path")

    private fun getAccessKeyId(): String? = Tiphareth.CONF.getString("automatically-upload.access-key-id")

    private fun getAccessKeySecret(): String? = Tiphareth.CONF.getString("automatically-upload.access-key-secret")
}