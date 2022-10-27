package com.rhea.onscan.di

import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    @Provides
    fun providesClipboardManagerService(@ApplicationContext context: Context): ClipboardManager =
        context.getSystemService(ClipboardManager::class.java)
}