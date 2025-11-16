package com.feooh.data.enums

/**
 * Represents the status of a Luma guest.
 */
enum class LumaGuestStatus(val value: String) {
    APP_REGISTERED("app_registered"),
    WALLET_REGISTERED("wallet_registered"),
    NFC_INITIALIZED("nfc_initialized");

    companion object {
        /**
         * Get the enum from its raw string value (case-insensitive).
         */
        fun fromValue(value: String?): LumaGuestStatus? =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
    }
}
