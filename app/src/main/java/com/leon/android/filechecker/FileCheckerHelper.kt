package com.leon.android.filechecker

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.math.BigInteger
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @time:2020/11/19 17:10
 * @author:Leon
 * @description:
 */
object FileCheckerHelper {
    /**
     * FileChannel 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    fun getFileMd52(file: File?): String? {
        val messageDigest: MessageDigest
        var fis: FileInputStream? = null
        var ch: FileChannel? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            if (file == null) {
                return ""
            }
            if (!file.exists()) {
                return ""
            }
            fis = FileInputStream(file)
            ch = fis.channel
            val size = 1024 * 1024 * 8
            val part: Long = file.length() / size + if (file.length() % size > 0) 1 else 0
            System.err.println("文件分片数$part")
            for (j in 0 until part) {
                val byteBuffer: MappedByteBuffer = ch.map(
                    FileChannel.MapMode.READ_ONLY,
                    j * size,
                    if (j == part - 1) file.length() else (j + 1) * size
                )
                messageDigest.update(byteBuffer)
                byteBuffer.clear()
            }
            val bigInt = BigInteger(1, messageDigest.digest())
            var md5: String = bigInt.toString(16)
            while (md5.length < 32) {
                md5 = "0$md5"
            }
            return md5
        } catch (e: NoSuchAlgorithmException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } finally {
            try {
                if (fis != null) {
                    fis.close()
                    fis = null
                }
                if (ch != null) {
                    ch.close()
                    ch = null
                }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
        return ""
    }
}