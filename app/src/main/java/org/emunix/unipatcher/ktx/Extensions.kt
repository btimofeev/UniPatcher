package org.emunix.unipatcher.ktx

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Job
import timber.log.Timber
import kotlin.reflect.KFunction1

fun Fragment.registerActivityResult(
    viewModelUri: KFunction1<Uri, Job>
): ActivityResultLauncher<Intent> {
    return this.registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let { uri ->
                Timber.d("$uri")
                uri.data?.let(viewModelUri)
            }
        }
    }
}
