package com.feooh.nfc

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import java.nio.charset.Charset

/**
 * Centralized NFC handler for reading and writing NFC tags
 * Handles validation, formatting, and data protection
 */
class NfcHandler(private val activity: Activity) {

    companion object {
        private const val TAG = "NfcHandler"

        // Our app's custom MIME type for wallet addresses
        private const val WALLET_MIME_TYPE = "application/vnd.com.feooh.wallet"

        // Minimum valid wallet address length (adjust as needed)
        private const val MIN_WALLET_ADDRESS_LENGTH = 20
    }

    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var onTagReadCallback: ((String) -> Unit)? = null
    private var onTagWriteCallback: ((Boolean, String?) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    /**
     * Check if NFC is available and enabled
     */
    fun isNfcAvailable(): Boolean = nfcAdapter != null && nfcAdapter!!.isEnabled

    /**
     * Enable NFC reading mode
     */
    fun enableReading(
        onTagRead: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        onTagReadCallback = onTagRead
        onErrorCallback = onError

        val options = android.os.Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        nfcAdapter?.enableReaderMode(
            activity,
            { tag -> handleTagRead(tag) },
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options
        )
    }

    /**
     * Enable NFC writing mode
     */
    fun enableWriting(
        walletAddress: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        onTagWriteCallback = { success, message ->
            onResult(success, message)
        }

        val options = android.os.Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        nfcAdapter?.enableReaderMode(
            activity,
            { tag -> handleTagWrite(tag, walletAddress) },
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options
        )
    }

    /**
     * Disable NFC mode
     */
    fun disable() {
        nfcAdapter?.disableReaderMode(activity)
        onTagReadCallback = null
        onTagWriteCallback = null
        onErrorCallback = null
    }

    /**
     * Handle reading a tag
     */
    private fun handleTagRead(tag: Tag) {
        try {
            Log.d(TAG, "handleTagRead: Starting read operation")
            Log.d(TAG, "handleTagRead: Tag technologies: ${tag.techList.joinToString()}")

            val walletAddress = readWalletAddress(tag)
            if (walletAddress != null) {
                Log.i(TAG, "handleTagRead: Successfully read wallet address: ${walletAddress.take(10)}...")
                onTagReadCallback?.invoke(walletAddress)
            } else {
                Log.w(TAG, "handleTagRead: No wallet address found on tag")
                onErrorCallback?.invoke("No wallet address found on tag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleTagRead: Exception during read operation", e)
            onErrorCallback?.invoke("Read failed: ${e.message}")
        }
    }

    /**
     * Handle writing to a tag with validation
     */
    private fun handleTagWrite(tag: Tag, walletAddress: String) {
        try {
            Log.d(TAG, "handleTagWrite: Starting write operation for wallet: ${walletAddress.take(10)}...")
            Log.d(TAG, "handleTagWrite: Tag technologies: ${tag.techList.joinToString()}")

            // Step 1: Read existing data from tag
            Log.d(TAG, "handleTagWrite: Step 1 - Reading existing tag data")
            val existingData = readTagData(tag)
            Log.d(TAG, "handleTagWrite: Existing data found: ${existingData != null}")

            // Step 2: Validate - check if tag already has our wallet data
            Log.d(TAG, "handleTagWrite: Step 2 - Checking if tag has our wallet data")
            if (existingData != null && isOurWalletData(existingData)) {
                Log.w(TAG, "handleTagWrite: Tag already contains wallet data - rejecting write")
                onTagWriteCallback?.invoke(false, "Tag already contains wallet data. Use a blank wristband.")
                return
            }

            // Step 3: Check if tag has other data (not our format)
            Log.d(TAG, "handleTagWrite: Step 3 - Checking if tag has unknown data")
            if (existingData != null && !isEmptyOrInvalid(existingData)) {
                Log.w(TAG, "handleTagWrite: Tag contains unknown data - rejecting write")
                onTagWriteCallback?.invoke(false, "Tag contains unknown data. Use a blank wristband.")
                return
            }

            // Step 4: Tag is safe to write - proceed with formatting if needed and writing
            Log.d(TAG, "handleTagWrite: Step 4 - Tag validation passed, proceeding with write")
            val success = writeWalletAddress(tag, walletAddress)
            if (success) {
                Log.i(TAG, "handleTagWrite: Write operation successful!")
                onTagWriteCallback?.invoke(true, "Wristband setup successful!")
            } else {
                Log.e(TAG, "handleTagWrite: Write operation failed")
                onTagWriteCallback?.invoke(false, "Failed to write to tag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleTagWrite: Exception during write operation", e)
            onTagWriteCallback?.invoke(false, "Write failed: ${e.message}")
        }
    }

    /**
     * Enable NFC mode to clear (format) any tag
     * Completely overrides validation and writes a blank NDEF message
     */
    fun enableClearing(onResult: (Boolean, String?) -> Unit) {
        onTagWriteCallback = { success, message ->
            onResult(success, message)
        }

        val options = android.os.Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        nfcAdapter?.enableReaderMode(
            activity,
            { tag -> handleTagClear(tag) },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options
        )
    }

    /**
     * Low-level tag clear handler — forcibly formats or overwrites with empty message
     */
    private fun handleTagClear(tag: Tag) {
        try {
            Log.i(TAG, "handleTagClear: Starting full wipe of NFC tag")

            val emptyRecord = NdefRecord.createMime(
                WALLET_MIME_TYPE,
                ByteArray(0) // blank payload
            )
            val emptyMessage = NdefMessage(arrayOf(emptyRecord))

            // Try normal NDEF write first
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                Log.i(TAG, "handleTagClear: Tag is NDEF formatted — clearing")
                val success = writeToNdefTag(ndef, emptyMessage)
                if (success) {
                    onTagWriteCallback?.invoke(true, "Tag cleared successfully")
                } else {
                    onTagWriteCallback?.invoke(false, "Failed to clear tag")
                }
                return
            }

            // Try to format the tag if it's not NDEF
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                Log.i(TAG, "handleTagClear: Tag is unformatted — formatting blank")
                val success = formatAndWrite(ndefFormatable, emptyMessage)
                if (success) {
                    onTagWriteCallback?.invoke(true, "Tag formatted and cleared successfully")
                } else {
                    onTagWriteCallback?.invoke(false, "Failed to format tag")
                }
                return
            }

            // Not compatible
            Log.e(TAG, "handleTagClear: Tag is not NDEF or formatable")
            onTagWriteCallback?.invoke(false, "Tag is not NDEF compatible")

        } catch (e: Exception) {
            Log.e(TAG, "handleTagClear: Exception while clearing tag", e)
            onTagWriteCallback?.invoke(false, "Clear failed: ${e.message}")
        }
    }


    /**
     * Read raw data from tag
     */
    private fun readTagData(tag: Tag): NdefMessage? {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                Log.d(TAG, "readTagData: NDEF tag found, reading existing data")
                ndef.connect()
                val message = ndef.ndefMessage
                ndef.close()
                Log.d(TAG, "readTagData: Successfully read ${message?.records?.size ?: 0} records")
                return message
            } else {
                Log.d(TAG, "readTagData: Tag is not NDEF formatted (null)")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "readTagData: Exception while reading tag data", e)
            null
        }
    }

    /**
     * Read wallet address from tag
     */
    private fun readWalletAddress(tag: Tag): String? {
        val message = readTagData(tag) ?: return null

        for ((index, record) in message.records.withIndex()) {
            // Check if it's our MIME type
            if (record.tnf == NdefRecord.TNF_MIME_MEDIA) {
                val mimeType = String(record.type, Charset.forName("UTF-8"))
                if (mimeType == WALLET_MIME_TYPE) {
                    val walletAddress = String(record.payload, Charset.forName("UTF-8"))
                    return walletAddress
                }
            }

            // Also check text records as backup
            if (record.tnf == NdefRecord.TNF_WELL_KNOWN) {
                val payload = String(record.payload, Charset.forName("UTF-8"))
                // Remove language code prefix if it's a text record
                val walletAddress = if (payload.length > 3 && payload[0].code < 32) {
                    payload.substring(3)
                } else {
                    payload
                }

                // Validate it looks like a wallet address
                if (walletAddress.length >= MIN_WALLET_ADDRESS_LENGTH) {
                    return walletAddress
                }
            }
        }

        return null
    }

    /**
     * Check if the data matches our wallet format
     */
    private fun isOurWalletData(message: NdefMessage): Boolean {
        for (record in message.records) {
            if (record.tnf == NdefRecord.TNF_MIME_MEDIA) {
                val mimeType = String(record.type, Charset.forName("UTF-8"))
                if (mimeType == WALLET_MIME_TYPE && record.payload.isNotEmpty()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Check if data is empty or invalid (safe to overwrite)
     */
    private fun isEmptyOrInvalid(message: NdefMessage): Boolean {
        if (message.records.isEmpty()) return true

        for (record in message.records) {
            // If any record has non-empty payload, it's not empty
            if (record.payload.isNotEmpty()) {
                return false
            }
        }
        return true
    }

    /**
     * Write wallet address to tag
     * Handles both NDEF formatted and unformatted tags
     */
    private fun writeWalletAddress(tag: Tag, walletAddress: String): Boolean {
        return try {
            Log.d(TAG, "writeWalletAddress: Creating NDEF message")

            // Create NDEF message with wallet address
            // Using only MIME record to minimize size for small capacity tags (137 bytes)
            val mimeRecord = NdefRecord.createMime(
                WALLET_MIME_TYPE,
                walletAddress.toByteArray(Charset.forName("UTF-8"))
            )

            val ndefMessage = NdefMessage(arrayOf(mimeRecord))

            val messageSize = ndefMessage.toByteArray().size

            // Try NDEF formatted tag first
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                writeToNdefTag(ndef, ndefMessage)
            } else {
                // Tag not formatted, try to format it
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    formatAndWrite(ndefFormatable, ndefMessage)
                } else {
                    Log.e(TAG, "writeWalletAddress: Tag is not NDEF compatible")
                    onTagWriteCallback?.invoke(false, "Tag is not NDEF compatible")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "writeWalletAddress: Exception during write", e)
            onTagWriteCallback?.invoke(false, "Write error: ${e.message}")
            false
        }
    }

    /**
     * Write to an NDEF formatted tag
     */
    private fun writeToNdefTag(ndef: Ndef, message: NdefMessage): Boolean {
        return try {
            ndef.connect()

            // Check capacity
            val size = message.toByteArray().size
            val maxSize = ndef.maxSize
            val isWritable = ndef.isWritable

            if (!isWritable) {
                Log.e(TAG, "writeToNdefTag: Tag is not writable!")
                ndef.close()
                return false
            }

            if (maxSize < size) {
                Log.e(TAG, "writeToNdefTag: Tag capacity ($maxSize bytes) is insufficient for message ($size bytes)")
                ndef.close()
                return false
            }

            ndef.writeNdefMessage(message)

            ndef.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "writeToNdefTag: Exception during write operation", e)
            try {
                ndef.close()
            } catch (ex: Exception) {
                Log.e(TAG, "writeToNdefTag: Exception while closing connection", ex)
            }
            false
        }
    }

    /**
     * Format an unformatted tag and write data
     */
    private fun formatAndWrite(ndefFormatable: NdefFormatable, message: NdefMessage): Boolean {
        return try {
            ndefFormatable.connect()

            val messageSize = message.toByteArray().size

            ndefFormatable.format(message)

            ndefFormatable.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "formatAndWrite: Exception during format/write operation", e)
            try {
                ndefFormatable.close()
            } catch (ex: Exception) {
                Log.e(TAG, "formatAndWrite: Exception while closing connection", ex)
            }
            false
        }
    }
}