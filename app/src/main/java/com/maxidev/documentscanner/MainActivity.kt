@file:OptIn(ExperimentalMaterial3Api::class)

package com.maxidev.documentscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil3.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.maxidev.documentscanner.ui.theme.DocumentScannerTheme
import com.maxidev.documentscanner.utils.PAGE_LIMIT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val scanner = GmsDocumentScanning.getClient(documentScannerOptions())
    private val createDocumentContract = ActivityResultContracts.CreateDocument("application/pdf")
    private val intentSenderContract = ActivityResultContracts.StartIntentSenderForResult()
    private val openDocumentContract = ActivityResultContracts.OpenDocument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        setContent {
            DocumentScannerTheme(dynamicColor = false) {
                var imageUris by remember {
                    mutableStateOf<List<Uri>>(emptyList())
                }
                var tempPdfUri by remember { mutableStateOf<Uri?>(null) }

                /**
                 * Launcher that creates and saves the file in PDF format.
                 */
                val createDocumentLauncher = rememberLauncherForActivityResult(
                    contract = createDocumentContract,
                    onResult = { uri ->
                        uri?.let { destinationUri ->
                            tempPdfUri?.let { sourceUri ->
                                try {
                                    // this: context object
                                    this.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                                        this.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                                            inputStream.copyTo(outputStream)
                                        }
                                    }
                                    showToast("PDF saved successfully")
                                } catch (e: Exception) {
                                    showToast("Error saving file: ${e.localizedMessage}")
                                }
                            }
                        }
                    }
                )

                /**
                 * Launcher for the scanner using the ML Kit.
                 */
                val scannerLauncher = rememberLauncherForActivityResult(
                    contract = intentSenderContract,
                    onResult = {
                        when (it.resultCode) {
                            RESULT_OK -> {
                                val result =
                                    GmsDocumentScanningResult.fromActivityResultIntent(it.data)

                                // Get images
                                imageUris = result?.pages?.map { gmsPage ->
                                    gmsPage.imageUri
                                } ?: emptyList()

                                // Get PDF and trigger save
                                result?.pdf?.let { pdf ->
                                    tempPdfUri = pdf.uri
                                    createDocumentLauncher.launch("scan.pdf")
                                }
                            }
                            RESULT_CANCELED -> {
                                showToast("Scanner cancelled")
                            }
                        }
                    }
                )

                /**
                 * Launcher for opening the folder.
                 */
                val openDocument = rememberLauncherForActivityResult(
                    contract = openDocumentContract,
                    onResult = { uri ->
                        uri?.let {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    it,
                                    contentResolver.getType(it)
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            startActivity(intent)
                        }
                    }
                )

                /* Screen content. */

                ScreenContent(
                    scannedImages = imageUris,
                    onScan = {
                        scanner.getStartScanIntent(this@MainActivity)
                            .addOnSuccessListener {
                                scannerLauncher.launch(
                                    IntentSenderRequest.Builder(it).build()
                                )
                            }
                            .addOnFailureListener {
                                showToast(it.message.orEmpty())
                            }
                    },
                    onOpenFolder = {
                        openDocument.launch(arrayOf("application/pdf"))
                    }
                )
            }
        }
    }

    private fun documentScannerOptions(): GmsDocumentScannerOptions {
        val gmsOptions = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(PAGE_LIMIT)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)

        return gmsOptions.build()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    scannedImages: List<Uri>,
    lazyList: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    onScan: () -> Unit,
    onOpenFolder: () -> Unit
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Document Scanner") },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = onScan,
                        text = { Text(text = "Scan") },
                        icon = { Icon(Icons.Default.DocumentScanner, "Scan document.") }
                    )
                },
                actions = {
                    IconButton(onClick = onOpenFolder) {
                        Icon(Icons.Default.Folder, "Open folder.")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (scannedImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Here you will see a list of recently scanned images.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                contentPadding = innerPadding,
                state = lazyList,
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                columns = StaggeredGridCells.Adaptive(160.dp)
            ) {
                items(items = scannedImages) { index ->
                    ImagePdf(image = index)
                }
            }
        }
    }
}

@Composable
private fun ImagePdf(
    modifier: Modifier = Modifier,
    image: Uri
) {
    Box(
        modifier = modifier
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}