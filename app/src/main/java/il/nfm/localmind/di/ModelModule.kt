package il.nfm.localmind.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import il.nfm.localmind.ml.DebugModelPathProvider
import il.nfm.localmind.ml.E5SmallEmbedder
import il.nfm.localmind.ml.Embedder
import il.nfm.localmind.ml.LLMEngine
import il.nfm.localmind.ml.LiteRtLLMEngine
import il.nfm.localmind.ml.ModelCoexistenceProbe
import il.nfm.localmind.ml.ModelPathProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModelModule {
    @Provides
    @Singleton
    fun provideModelPathProvider(
        @ApplicationContext context: Context,
    ): ModelPathProvider = DebugModelPathProvider(context)

    @Provides
    @Singleton
    fun provideLlmEngine(modelPathProvider: ModelPathProvider): LLMEngine =
        LiteRtLLMEngine(modelPathProvider.llmModelPath)

    @Provides
    @Singleton
    fun provideEmbedder(modelPathProvider: ModelPathProvider): Embedder =
        E5SmallEmbedder(
            modelPath = modelPathProvider.embeddingModelPath,
            tokenizerPath = modelPathProvider.embeddingTokenizerPath,
        )

    @Provides
    @Singleton
    fun provideModelCoexistenceProbe(
        llmEngine: LLMEngine,
        embedder: Embedder,
    ): ModelCoexistenceProbe = ModelCoexistenceProbe(llmEngine = llmEngine, embedder = embedder)
}
