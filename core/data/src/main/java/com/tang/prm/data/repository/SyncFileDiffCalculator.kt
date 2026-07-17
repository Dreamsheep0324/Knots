package com.tang.prm.data.repository

import com.tang.prm.domain.model.FileEntry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步文件差异计算器
 *
 * 从 WebDavRepositoryImpl 拆分出的纯函数职责：
 * - computeUploadDiff：本地 vs 远端，计算需要上传和远端需删除的文件
 * - computeDownloadDiff：远端 vs 本地，计算需要下载和本地需删除的文件
 */
@Singleton
class SyncFileDiffCalculator @Inject constructor() {

    /**
     * 计算上传差异
     * @param localFiles 本地文件列表
     * @param remoteEntries 远端文件清单（name → FileEntry）
     * @return (需要上传的本地文件列表, 需要从远端删除的文件名列表)
     */
    fun computeUploadDiff(
        localFiles: List<FileEntry>,
        remoteEntries: Map<String, FileEntry>
    ): Pair<List<FileEntry>, List<String>> {
        val toUpload = mutableListOf<FileEntry>()
        val toDeleteRemote = mutableListOf<String>()

        val localFileNames = localFiles.map { it.name }.toSet()

        // 远端有但本地没有 → 删除远端
        for ((remoteName, _) in remoteEntries) {
            if (remoteName !in localFileNames) {
                toDeleteRemote.add(remoteName)
            }
        }

        // 本地文件与远端对比
        for (localFile in localFiles) {
            val remoteEntry = remoteEntries[localFile.name]
            if (remoteEntry == null) {
                toUpload.add(localFile)
            } else if (localFile.modified > remoteEntry.modified) {
                toUpload.add(localFile)
            }
        }

        return toUpload to toDeleteRemote
    }

    /**
     * 计算下载差异
     * @param localFiles 本地文件列表
     * @param remoteEntries 远端文件清单（name → FileEntry）
     * @return (需要下载的远端文件列表, 需要从本地删除的文件名列表)
     */
    fun computeDownloadDiff(
        localFiles: List<FileEntry>,
        remoteEntries: Map<String, FileEntry>
    ): Pair<List<FileEntry>, List<String>> {
        val toDownload = mutableListOf<FileEntry>()
        val toDeleteLocal = mutableListOf<String>()

        val localFileMap = localFiles.associateBy { it.name }

        // 远端有但本地没有 → 下载
        for ((remoteName, remoteEntry) in remoteEntries) {
            val localEntry = localFileMap[remoteName]
            if (localEntry == null) {
                toDownload.add(remoteEntry)
            } else if (remoteEntry.modified > localEntry.modified) {
                toDownload.add(remoteEntry)
            }
        }

        // 本地有但远端没有 → 删除本地
        for (localFile in localFiles) {
            if (localFile.name !in remoteEntries) {
                toDeleteLocal.add(localFile.name)
            }
        }

        return toDownload to toDeleteLocal
    }
}
