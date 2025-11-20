/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.ai.catalog.domain

import android.Manifest
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.ai.catalog.R
import com.android.ai.theme.extendedColorScheme

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
val sampleCatalog = emptyList<SampleCatalogItem>()

data class SampleCatalogItem(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val route: String,
    val sampleEntryScreen: @Composable () -> Unit,
    val tags: List<SampleTags> = emptyList(),
    val needsFirebase: Boolean = false,
    val isFeatured: Boolean = false,
    @DrawableRes val keyArt: Int? = null,
)

enum class SampleTags(
    val label: String,
    val backgroundColor: Color,
) {
    FIREBASE("Firebase", extendedColorScheme.firebase),
    GEMINI_FLASH("Gemini Flash", extendedColorScheme.geminiProFlash),
    GEMINI_NANO("Gemini Nano", extendedColorScheme.geminiNano),
    IMAGEN("Imagen", extendedColorScheme.imagen),
    MEDIA3("Media3", extendedColorScheme.media3),
    ML_KIT("ML Kit", extendedColorScheme.mLKit),
}
