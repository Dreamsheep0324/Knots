package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.tang.prm.domain.model.BackupFileInfo

/**
 * SAF（Storage Access Framework）文档操作辅助类。
 * 封装 Uri 查找、文件列表查询、目录名解析等 SAF 操作。
 */
class SafDocumentsHelper(private val context: Context) {

    fun toDocumentUri(treeUri: Uri): Uri {
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
    }

    fun findDocumentUriByName(treeUri: Uri, fileName: String): Uri? {
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)
        try {
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameCol) == fileName) {
                        val docId = cursor.getString(idCol)
                        return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SafDocumentsHelper", "查找文档URI失败: $fileName", e)
        }
        return null
    }

    fun listBackupFiles(dirUri: Uri, backupExtension: String): List<BackupFileInfo> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            dirUri, DocumentsContract.getTreeDocumentId(dirUri)
        )
        val files = mutableListOf<BackupFileInfo>()

        try {
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                ),
                null, null, "${DocumentsContract.Document.COLUMN_LAST_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol)
                    if (name != null && name.endsWith(backupExtension)) {
                        files.add(BackupFileInfo(
                            fileName = name,
                            fileSize = if (cursor.isNull(sizeCol)) 0L else cursor.getLong(sizeCol),
                            timestamp = if (cursor.isNull(modCol)) 0L else cursor.getLong(modCol)
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SafDocumentsHelper", "查询备份文件列表失败", e)
        }

        return files.sortedByDescending { it.timestamp }
    }

    companion object {
        fun getBackupDirNameFromUri(uriStr: String): String {
            val uri = Uri.parse(uriStr)
            val docId = DocumentsContract.getTreeDocumentId(uri)
            return when {
                docId.startsWith("primary:") -> docId.removePrefix("primary:")
                docId.startsWith("home:") -> docId.removePrefix("home:")
                else -> docId
            }
        }
    }
}
