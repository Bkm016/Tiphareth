package ink.ptms.tiphareth.pack

import com.aliyun.oss.OSSClientBuilder
import ink.ptms.tiphareth.Tiphareth
import io.izzel.taboolib.module.db.local.Local
import java.io.File
import java.io.FileInputStream
import java.util.*

object PackUploader {

    var isEnable = getEndPoint() != null
        private set

    fun upload(resourcePack: File): Boolean {
        if (isEnable) {
            Local.get().get("data").set("hash", UUID.randomUUID().toString().replace("-", ""))
            try {
                val ossClient = OSSClientBuilder().build(getEndPoint(), getAccessKeyId(), getAccessKeySecret())
                FileInputStream(resourcePack).use { inputStream ->
                    ossClient.putObject(getBucketName(), getObjectPath()!!.replace("{hash}", Local.get().get("data").getString("hash", "null")!!), inputStream)
                    ossClient.shutdown()
                }
                return true
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        return false
    }

    fun getPackURL(): String = "https://${getBucketName()}.${getEndPoint()}/${getObjectPath()}".replace("{hash}", Local.get().get("data").getString("hash", "null")!!)

    private fun getEndPoint(): String? = Tiphareth.conf.getString("automatically-upload.endpoint")

    private fun getBucketName(): String? = Tiphareth.conf.getString("automatically-upload.bucket-name")

    private fun getObjectPath(): String? = Tiphareth.conf.getString("automatically-upload.object-path")

    private fun getAccessKeyId(): String? = Tiphareth.conf.getString("automatically-upload.access-key-id")

    private fun getAccessKeySecret(): String? = Tiphareth.conf.getString("automatically-upload.access-key-secret")
}