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
        // Swing DropTarget を Window の contentPane にアタッチ
        remember(window) {
            val dropTargetListener = object : DropTargetListener {
                override fun dragEnter(dtde: DropTargetDragEvent) {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrag(DnDConstants.ACTION_COPY)
                        viewModel.setDraggingOver(true)
                    } else {
                        dtde.rejectDrag()
                    }
                }

                override fun dragOver(dtde: DropTargetDragEvent) {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrag(DnDConstants.ACTION_COPY)
                        viewModel.setDraggingOver(true)
                    }
                }

                override fun dropActionChanged(dtde: DropTargetDragEvent) {}

                override fun dragExit(dte: DropTargetEvent) {
                    viewModel.setDraggingOver(false)
                }

                override fun drop(dtde: DropTargetDropEvent) {
                    viewModel.setDraggingOver(false)
                    try {
                        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY)
                            val transferable = dtde.transferable
                            @Suppress("UNCHECKED_CAST")
                            val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File>
                            if (!files.isNullOrEmpty()) {
                                viewModel.tossFiles(files)
                                dtde.dropComplete(true)
                                return
                            }
                        }
                    } catch (_: Exception) {
                    }
                    dtde.rejectDrop()
                }
            }
            DropTarget(window.contentPane, DnDConstants.ACTION_COPY, dropTargetListener, true)
        }

        BauhausTheme {
            MainScreen(viewModel = viewModel)
        }
    }
}
