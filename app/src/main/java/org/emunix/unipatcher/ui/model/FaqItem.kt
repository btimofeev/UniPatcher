package org.emunix.unipatcher.ui.model

import androidx.annotation.StringRes

data class FaqItem(
    @StringRes val title: Int,
    @StringRes val body: Int,
)