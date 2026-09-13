package com.filetoss

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.filetoss.data.repository.JsonProfileRepository
import com.filetoss.data.security.WindowsDpapiCredentialStore
import com.filetoss.data.sftp.SftpTransferClient
import com.filetoss.ui.screens.MainScreen
import com.filetoss.ui.theme.BauhausTheme
import com.filetoss.ui.viewmodel.MainViewModel
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import java.io.File

fun main() = application {
    val repository = remember { JsonProfileRepository() }
    val credentialStore = remember { WindowsDpapiCredentialStore() }
    val sftpClient = remember { SftpTransferClient() }
    val viewModel = remember { MainViewModel(repository, credentialStore, sftpClient) }

    Window(
        onCloseRequest = {
            sftpClient.close()
            exitApplication()
        },
        title = "file-toss",
        state = WindowState(width = 980.dp, height = 720.dp)
    ) {
        // Swing DropTarget を Window および contentPane にアタッチ
        remember(window) {
            fun selectBestAction(sourceActions: Int): Int {
                return when {
                    (sourceActions and DnDConstants.ACTION_COPY) != 0 -> DnDConstants.ACTION_COPY
                    (sourceActions and DnDConstants.ACTION_MOVE) != 0 -> DnDConstants.ACTION_MOVE
                    (sourceActions and DnDConstants.ACTION_LINK) != 0 -> DnDConstants.ACTION_LINK
                    else -> DnDConstants.ACTION_NONE
                }
            }

            fun isFileListSupported(dtde: DropTargetDragEvent): Boolean {
                return try {
                    dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                        dtde.currentDataFlavors.any { flavor ->
                            flavor.isFlavorJavaFileListType ||
                                flavor.mimeType.contains("application/x-java-file-list", ignoreCase = true) ||
                                flavor.mimeType.contains("text/uri-list", ignoreCase = true)
                        }
                } catch (_: Exception) {
                    false
                }
            }

            fun extractFiles(transferable: java.awt.datatransfer.Transferable): List<File> {
                try {
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @Suppress("UNCHECKED_CAST")
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File>
                        if (!files.isNullOrEmpty()) return files
                    }
                } catch (_: Exception) {}

                try {
                    val uriListFlavor = DataFlavor("text/uri-list;class=java.lang.String")
                    if (transferable.isDataFlavorSupported(uriListFlavor)) {
                        val uriList = transferable.getTransferData(uriListFlavor) as? String
                        if (!uriList.isNullOrBlank()) {
                            return uriList.lineSequence()
                                .filter { it.isNotBlank() && !it.startsWith("#") }
                                .mapNotNull { uriStr ->
                                    try {
                                        File(java.net.URI(uriStr.trim()))
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                                .toList()
                        }
                    }
                } catch (_: Exception) {}

                return emptyList()
            }

            val dropTargetListener = object : DropTargetListener {
                override fun dragEnter(dtde: DropTargetDragEvent) {
                    val action = selectBestAction(dtde.sourceActions)
                    if (action != DnDConstants.ACTION_NONE && isFileListSupported(dtde)) {
                        dtde.acceptDrag(action)
                        viewModel.setDraggingOver(true)
                    } else {
                        dtde.rejectDrag()
                    }
                }

                override fun dragOver(dtde: DropTargetDragEvent) {
                    val action = selectBestAction(dtde.sourceActions)
                    if (action != DnDConstants.ACTION_NONE && isFileListSupported(dtde)) {
                        dtde.acceptDrag(action)
                        viewModel.setDraggingOver(true)
                    } else {
                        dtde.rejectDrag()
                    }
                }

                override fun dropActionChanged(dtde: DropTargetDragEvent) {
                    val action = selectBestAction(dtde.sourceActions)
                    if (action != DnDConstants.ACTION_NONE && isFileListSupported(dtde)) {
                        dtde.acceptDrag(action)
                    } else {
                        dtde.rejectDrag()
                    }
                }

                override fun dragExit(dte: DropTargetEvent) {
                    viewModel.setDraggingOver(false)
                }

                override fun drop(dtde: DropTargetDropEvent) {
                    viewModel.setDraggingOver(false)
                    try {
                        val action = selectBestAction(dtde.sourceActions)
                        if (action != DnDConstants.ACTION_NONE) {
                            dtde.acceptDrop(action)
                            val files = extractFiles(dtde.transferable)
                            if (files.isNotEmpty()) {
                                viewModel.tossFiles(files)
                                dtde.dropComplete(true)
                                return
                            }
                        }
                    } catch (t: Throwable) {
                        System.err.println("[file-toss] Drop error: ${t.message}")
                        t.printStackTrace()
                    }
                    dtde.rejectDrop()
                }
            }

            val target = DropTarget(window.contentPane, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener, true)
            window.contentPane.dropTarget = target
            window.dropTarget = target
            target
        }

        BauhausTheme {
            MainScreen(viewModel = viewModel)
        }
    }
}
