package com.example.product.feature.product.di

import com.example.product.feature.product.data.DataSource
import com.example.product.feature.product.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        provideDataSource: DataSource
    ): ProductRepository {
        return ProductRepository(provideDataSource)
    }

    @Provides
    @Singleton
    fun provideDataSource(): DataSource {
        return DataSource()

    }
}