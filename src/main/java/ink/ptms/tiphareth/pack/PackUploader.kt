package ink.ptms.tiphareth.pack

import com.aliyun.oss.OSSClientBuilder
import ink.ptms.tiphareth.Tiphareth
import taboolib.module.configuration.createLocal
import java.io.File
import java.io.FileInputStream
import java.util.*

object PackUploader {

    val data = createLocal("data.yml")

    var isEnable = getEndPoint() != null
        private set

    fun upload(resourcePack: File): Boolean {
        if (isEnable) {
            data.set("hash", UUID.randomUUID().toString().replace("-", ""))
            try {
                val ossClient = OSSClientBuilder().build(getEndPoint(), getAccessKeyId(), getAccessKeySecret())
                FileInputStream(resourcePack).use { inputStream ->
                    ossClient.putObject(getBucketName(), getObjectPath()!!.replace("{hash}", data.getString("hash", "null")!!), inputStream)
                    ossClient.shutdown()
                }
                return true
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        return false
    }

    fun getPackURL(): String {
        return "https://${getBucketName()}.${getEndPoint()}/${getObjectPath()}".replace("{hash}", data.getString("hash", "null")!!)
    }

    private fun getEndPoint(): String? {
        return Tiphareth.conf.getString("automatically-upload.endpoint")
    }

    private fun getBucketName(): String? {
        return Tiphareth.conf.getString("automatically-upload.bucket-name")
    }

    private fun getObjectPath(): String? {
        return Tiphareth.conf.getString("automatically-upload.object-path")
    }

    private fun getAccessKeyId(): String? {
        return Tiphareth.conf.getString("automatically-upload.access-key-id")
    }

    private fun getAccessKeySecret(): String? {
        return Tiphareth.conf.getString("automatically-upload.access-key-secret")
    }
}