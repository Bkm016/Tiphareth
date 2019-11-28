package ink.ptms.tiphareth.pack

import com.aliyun.oss.OSSClientBuilder
import ink.ptms.tiphareth.Tiphareth
import ink.ptms.tiphareth.util.Utils
import io.izzel.taboolib.module.db.local.Local
import io.izzel.taboolib.module.db.local.LocalFile
import org.bukkit.Material
import java.io.File
import java.io.FileInputStream

object PackUploader {

    var isEnable = getEndPoint() != null
        private set

    fun upload(resourcePack: File): Boolean {
        if (isEnable) {
            Local.get().get("data").set("hash", Utils.getHashCode(resourcePack))
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

    private fun getEndPoint(): String? = Tiphareth.CONF.getString("automatically-upload.endpoint")

    private fun getBucketName(): String? = Tiphareth.CONF.getString("automatically-upload.bucket-name")

    private fun getObjectPath(): String? = Tiphareth.CONF.getString("automatically-upload.object-path")

    private fun getAccessKeyId(): String? = Tiphareth.CONF.getString("automatically-upload.access-key-id")

    private fun getAccessKeySecret(): String? = Tiphareth.CONF.getString("automatically-upload.access-key-secret")
}